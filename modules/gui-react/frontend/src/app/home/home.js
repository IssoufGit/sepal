import React from 'react'
import {connect, select} from 'store'
import PropTypes from 'prop-types'
import styles from './home.module.css'
import Sections from './sections/sections'
import Footer from './footer/footer'
import Body from './body/body'

const mapStateToProps = () => ({
    fixedMenu: select('menu.locked') || false
})

const Home = ({user, fixedMenu}) => {
    return (
        <div className={[styles.home, fixedMenu && styles.fixedMenu].join(' ')}>
            <Sections/>
            <Footer className={styles.footer} user={user}/>
            <Body/>
        </div>
    )
}
export default connect(mapStateToProps)(Home)

Home.propTypes = {
    user: PropTypes.object.isRequired,
    fixedMenu: PropTypes.bool.isRequired
}
