package org.openforis.sepal.component.sandboxmanager

import groovy.json.JsonOutput
import groovy.transform.ToString
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.openforis.sepal.SepalConfiguration
import org.openforis.sepal.hostingservice.WorkerInstance
import org.openforis.sepal.util.Clock
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static groovyx.net.http.ContentType.JSON

@ToString
class DockerSandboxSessionProvider implements SandboxSessionProvider {
    private static final Logger LOG = LoggerFactory.getLogger(this)
    private static final int SSH_PORT = 222
    private final SepalConfiguration config
    private final Clock clock

    DockerSandboxSessionProvider(SepalConfiguration config, Clock clock) {
        this.config = config
        this.clock = clock
    }

    SandboxSession deploy(SandboxSession session, WorkerInstance instance) {
        LOG.info("Checking if Docker is initialize on $instance")
        try {
            def containers = deployedContainers(instance)
            if (containers == null) // Docker client not available
                return session.starting(instance)
            removeAlreadyDeployedContainers(instance, containers)
            LOG.info("Deploying $session to $instance")
            createContainer(session, instance)
            startContainer(session, instance)
//            def port = determineContainerPort(session, instance)
            def port = SSH_PORT
            def deployedSession = session.active(instance, port, clock.now())
            waitUntilInitialized(deployedSession, instance)
            LOG.info("Deployed $session to $instance")
            return deployedSession
        } catch (Exception e) {
            try {
                close(session)
            } catch (Exception ex) {
                LOG.error("Failed to rollback deployment. Session: $session, Instance: $instance", ex)
            }
            throw new DockerSandboxSessionProviderException("Failed to deploy sandbox. Session: $session, Instance: $instance", e)
        }
    }

    void removeAlreadyDeployedContainers(WorkerInstance instance, List<Map> containers) {
        if (containers.empty)
            return
        LOG.warn("Containers already present on instance when trying to deploy a new one. Removing them. Instance: $instance")
        try {
            withClient(instance) {
                containers.each {
                    delete(path: "containers/$it.Id", query: [force: true])
                }

            }
        } catch (Exception e) {
            throw new DockerSandboxSessionProviderException("Failed to delete container from instance. Instance: $instance", e)
        }
    }

    @SuppressWarnings("GrDeprecatedAPIUsage")
    private List<Map> deployedContainers(WorkerInstance instance) {
        withClient(instance) {
            client.params.setParameter('http.connection.timeout', new Integer(5 * 1000))
            client.params.setParameter('http.socket.timeout', new Integer(5 * 1000))
            try {
                def response = get(path: 'containers/json')
                return response.data
            } catch (Exception ignore) {
                return null // Not available
            }
        }
    }

    SandboxSession close(SandboxSession session) {
        try {
            if (session.host)
                removeContainer(session, session.host)
            return session.closed(clock.now())
        } catch (Exception e) {
            throw new DockerSandboxSessionProviderException('Failed to close session', e)
        }
    }

    void assertAvailable(SandboxSession session) throws SandboxSessionProvider.NotAvailable {
        def data = loadContainerInfo(session, session.host)
        if (!data.State.Running)
            throw new SandboxSessionProvider.NotAvailable(session.id, "Session not available: $session")
    }

    private void createContainer(SandboxSession session, WorkerInstance instance) {
        def request = new JsonOutput().toJson([
                Image: "$config.dockerImageName",
                Tty: true,
                Cmd: ["/start", session.username, config.ldapHost, config.ldapPassword],
                HostConfig: [
                        Binds: [
                                "$config.mountingHomeDir/$session.username:/home/$session.username",
                                "$config.publicHomeDir:/sepal/public",
                                "/data/sepal/certificates/ldap-ca.crt.pem:/etc/ldap/certificates/ldap-ca.crt.pem"
                        ]
                ],
                ExposedPorts: [
                        '22/tcp': [:],
                        '8787/tcp': [:]
                ]

        ])
        LOG.debug("Deploying session $session to $instance.")
        withClient(instance) {
            def response = post(
                    path: "containers/create",
                    query: [name: containerName(session)],
                    body: request,
                    requestContentType: JSON
            )
            LOG.debug("Deployed session $session to $instance.")
            if (response.data.Warnings)
                LOG.warn("Warning when creating docker container for session $session in $instance: $response.data.Warnings")
        }
    }

    private void startContainer(SandboxSession session, WorkerInstance instance) {
        def request = new JsonOutput().toJson(
                PortBindings: [
                        "22/tcp": [[HostPort: "$SSH_PORT"]],
                        '8787/tcp': [[HostPort: '8787']],
                ])
        withClient(instance) {
            post(
                    path: "containers/${containerName(session)}/start",
                    body: request,
                    requestContentType: JSON
            )
        }
    }

    private void waitUntilInitialized(SandboxSession session, WorkerInstance instance) {
        LOG.debug("Waiting for session to be initialized on ports $config.sandboxPortsToCheck. " +
                "Session: $session, WorkerInstance: $instance")
        withClient(instance) {
            def response = post(
                    path: "containers/${containerName(session)}/exec",
                    body: new JsonOutput().toJson([
                            AttachStdin: false,
                            AttachStdout: true,
                            AttachStderr: true,
                            Tty: false,
                            Cmd: ["/root/wait_until_initialized.sh", config.sandboxPortsToCheck]
                    ]),
                    requestContentType: JSON
            )
            def execId = response.data.Id
            post(
                    path: "exec/$execId/start",
                    body: new JsonOutput().toJson([Detach: false, Tty: true]),
                    requestContentType: JSON
            )
            LOG.debug("Session initialized. Session: $session, WorkerInstance: $instance.")
        }
    }

//    @SuppressWarnings("GroovyAssignabilityCheck")
//    private int determineContainerPort(SandboxSession session, WorkerInstance instance) {
//        def data = loadContainerInfo(session, instance.host)
//        return Integer.parseInt(data.NetworkSettings.Ports["22/tcp"][0].HostPort)
//    }

    private void removeContainer(SandboxSession session, String host) {
        LOG.info("Removing container for session on host $host. Session: $session")
        withClient(host) {
            delete(path: "containers/${containerName(session)}", query: [force: true])
        }
        LOG.info("Removed container for session on host $host. Session: $session")
    }

    private String containerName(SandboxSession session) {
        "sandbox-$session.username-$session.id"
    }

    private Map loadContainerInfo(SandboxSession session, String host) {
        try {
            withClient(host) {
                get(path: "containers/${containerName(session)}/json").data
            }
        } catch (Exception e) {
            throw new DockerSandboxSessionProviderException("Failed to load sandbox info. Host: $host, session: $session", e)
        }
    }

    private <T> T withClient(WorkerInstance instance, @DelegatesTo(RESTClient) Closure<T> callback) {
        withClient(instance.host, callback)
    }

    private <T> T withClient(String host, @DelegatesTo(RESTClient) Closure<T> callback) {
        def client = new RESTClient("http://$host:$config.dockerDaemonPort/$config.dockerRESTEntryPoint/")
        try {
            callback.delegate = client
            return callback.call()
        } finally {
            client.shutdown()
        }
    }

    private static class DockerSandboxSessionProviderException extends RuntimeException {
        DockerSandboxSessionProviderException(String message, Exception e) {
            super(e instanceof HttpResponseException ? message += ": $e.response.data" : message, e)
        }
    }
}