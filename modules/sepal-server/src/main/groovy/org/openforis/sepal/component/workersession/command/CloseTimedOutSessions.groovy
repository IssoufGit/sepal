package org.openforis.sepal.component.workersession.command

import org.openforis.sepal.command.AbstractCommand
import org.openforis.sepal.command.CommandHandler
import org.openforis.sepal.component.workersession.api.InstanceManager
import org.openforis.sepal.component.workersession.api.WorkerSessionRepository
import org.openforis.sepal.component.workersession.event.SessionClosed
import org.openforis.sepal.event.EventDispatcher
import org.openforis.sepal.util.annotation.Data

@Data(callSuper = true)
class CloseTimedOutSessions extends AbstractCommand<Void> {

}

class CloseTimedOutSessionsHandler implements CommandHandler<Void, CloseTimedOutSessions> {
    private final WorkerSessionRepository repository
    private final InstanceManager instanceManager
    private final EventDispatcher eventDispatcher

    CloseTimedOutSessionsHandler(
            WorkerSessionRepository repository,
            InstanceManager instanceManager,
            EventDispatcher eventDispatcher) {
        this.repository = repository
        this.instanceManager = instanceManager
        this.eventDispatcher = eventDispatcher
    }

    Void execute(CloseTimedOutSessions command) {
        def timedOutSessions = repository.timedOutSessions()
        timedOutSessions.each { repository.update(it.close()) }
        timedOutSessions.each { instanceManager.releaseInstance(it.instance.id) }
        timedOutSessions.each { eventDispatcher.publish(new SessionClosed(it.id)) }
        return null
    }
}
