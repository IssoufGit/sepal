/**
 * @author Mino Togna
 */
var EventBus = require( '../event/event-bus' )
var Events   = require( '../event/events' )

// html
var template = require( './search.html' )
var html     = $( template( {} ) )
// ui components
var section  = null
var Form     = require( './search-form' )

var init = function () {
    var appSection = $( '#app-section' ).find( '.search' )
    if ( appSection.children().length <= 0 ) {
        appSection.append( html )

        section = appSection.find( '#search' )

        Form.init( section.find( 'form' ) )
        Form.startDate().show()
        Form.endDate().hide()
        Form.targetDay().hide()

        Form.find( '.from-label' ).click( function () {
            Form.endDate().hide()
            Form.targetDay().hide()
            Form.startDate().show()
        } )
        Form.find( '.to-label' ).click( function () {
            Form.startDate().hide()
            Form.targetDay().hide()
            Form.endDate().show()
        } )
        Form.find( '.target-day-label' ).click( function () {
            Form.startDate().hide()
            Form.endDate().hide()
            Form.targetDay().show()
        } )

    }
}

module.exports = {
    init  : init
    , Form: Form
    // , startDate  : Form.startDate.value
    // , endDate    : Form.endDate.value
    // , targetDay  : Form.targetDay.value
    // , countryCode: Form.countryCode
}