import React from 'react'
import {connect, dispatch} from 'store'
import {history} from 'route'
import styles from './appLaunchPad.module.css'
import Icon from 'widget/icon'
import {appList, requestedApps, runApp$} from 'apps'
import PropTypes from 'prop-types'

const mapStateToProps = () => ({
    apps: appList(),
    requestedApps: requestedApps()
})

class AppLaunchPad extends React.Component {
    runApp(app) {
        dispatch(history().push('/app' + app.path))
        if (!this.props.requestedApps.includes(app))
            this.props.asyncActionBuilder('RUN_APP',
                runApp$(app.path))
                .dispatch()
    }

    render() {
        const {apps} = this.props
        const items = apps.map(
            (app) => <App key={app.path} app={app} onClick={this.runApp.bind(this)}/>
        )
        return (
            <div className={styles.apps}>
                {items}
            </div>
        )
    }
}

AppLaunchPad.propTypes = {
    apps: PropTypes.arrayOf(PropTypes.object),
    requestedApps: PropTypes.arrayOf(PropTypes.object),
    asyncActionBuilder: PropTypes.func
}

export default AppLaunchPad = connect(mapStateToProps)(AppLaunchPad)

const App = ({app, onClick}) =>
    <button className={styles.app} onClick={() => onClick(app)}>
        <Image src={app.image}/>
        <Icon name={app.icon} alt={app.alt}/>
        <div>
            <div className={styles.title}>{app.label}</div>
            <div className={styles.description}>{app.description}</div>
        </div>
    </button>

App.propTypes = {
    app: PropTypes.object,
    onClick: PropTypes.func
}

const Image = ({src, alt}) => {
    return src 
        ? <img src={src} alt={alt}/>
        : null
}

Image.propTypes = {
    src: PropTypes.string,
    alt: PropTypes.string
}