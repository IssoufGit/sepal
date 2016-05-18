/**
 * @author Mino Togna
 */
require( './nav-menu.css' )

var EventBus  = require( '../event/event-bus' )
var Events    = require( '../event/events' )
var Animation = require( '../animation/animation' )

var template = require( './nav-menu.html' )
var html     = $( template( {} ) )

var btnSearch   = html.find( 'a.bg-search' )
var btnBrowse   = html.find( 'a.bg-browse' )
var btnProcess  = html.find( 'a.bg-process' )
var btnTerminal = html.find( 'a.bg-terminal' )

var show = function () {

    $( '.app' ).append( html )

    var showSection = function ( e ) {
        var btn = $( this )
        collapseMenu( btn )
        EventBus.dispatch( Events.SECTION.SHOW, null, btn.data('section-target') )
    }

    // init style
    btnSearch.addClass( 'expanded' ).click( showSection )
    btnBrowse.addClass( 'expanded' ).css( 'opacity', '0' ).click( showSection )
    btnProcess.addClass( 'expanded' ).css( 'opacity', '0' ).click( showSection )
    btnTerminal.addClass( 'expanded' ).css( 'opacity', '0' ).click( showSection )

    btnSearch.empty().append( '<i class="fa fa-globe" aria-hidden="true"></i> Search' )
    btnBrowse.empty().append( '<i class="fa fa-folder-open" aria-hidden="true"></i> Browse' )
    btnProcess.empty().append( '<i class="fa fa-space-shuttle" aria-hidden="true"></i> Process' )
    btnTerminal.empty().append( '<i class="fa fa-terminal" aria-hidden="true"></i> Terminal' )

    Animation.animateIn( btnSearch )
    Animation.animateIn( btnBrowse )
    Animation.animateIn( btnProcess )
    Animation.animateIn( btnTerminal )

}

var collapseMenu = function ( button ) {
    if ( button.hasClass( 'expanded' ) ) {

        // Animation.removeAnimation( btnSearch )
        // Animation.removeAnimation( btnBrowse )
        // Animation.removeAnimation( btnProcess )
        // Animation.removeAnimation( btnTerminal )

        Animation.animateOut( button )

        var delay = 100
        $.each( button.siblings(), function ( i, btnSibling ) {

            delay += 150
            btnSibling = $( btnSibling )

            setTimeout( function () {
                Animation.animateOut( btnSibling )
            }, delay )

        } )

        delay += 900
        setTimeout( function () {
            $( '#nav-menu' ).addClass( 'collapsed' )

            btnSearch.empty().removeClass( 'expanded' ).append( '<i class="fa fa-globe" aria-hidden="true"></i>' )
            btnBrowse.empty().removeClass( 'expanded' ).append( '<i class="fa fa-folder-open" aria-hidden="true"></i>' )
            btnProcess.empty().removeClass( 'expanded' ).append( '<i class="fa fa-space-shuttle" aria-hidden="true"></i>' )
            btnTerminal.empty().removeClass( 'expanded' ).append( '<i class="fa fa-terminal" aria-hidden="true"></i>' )

            setTimeout( function () {
                Animation.animateIn( btnSearch )
                Animation.animateIn( btnBrowse )
                Animation.animateIn( btnProcess )
                Animation.animateIn( btnTerminal )
            }, 250 )


            // $( '#sepal-logo' ).velocity( { 'left': '42%' }, {
            $( '#sepal-logo' ).velocity( { 'top': '0%', 'left': '0%', 'opacity': '0.7' }, {
                duration: 1500,
                easing: 'swing',
                delay : 1500,
                queue: false
            } )

        }, delay )

    }
}

EventBus.addEventListener( Events.APP.LOAD, show )