import React from 'react'
import {Constraints, ErrorMessage, form, Input} from 'widget/form'
import {resetPassword, validateToken} from 'user'
import Button from './button'
import {Msg, msg} from 'translate'
import {locationReducer} from 'route'

let ResetPassword = ({form, inputs: {username, password, password2}}) =>
    <form>
        <div>
            <label><Msg id='landing.reset-password.username.label'/></label>
            <input
                value={username}
                disabled={true}/>
            <ErrorMessage/>
        </div>
        <div>
            <label><Msg id='landing.reset-password.password.label'/></label>
            <Input
                input={password}
                type='password'
                placeholder={msg('landing.reset-password.password.placeholder')}
                autoFocus='on'
                tabIndex={1}/>
            <ErrorMessage input={password}/>
        </div>
        <div>
            <label><Msg id='landing.reset-password.password2.label'/></label>
            <Input
                input={password2}
                type='password'
                placeholder={msg('landing.reset-password.password2.placeholder')}
                tabIndex={2}/>
            <ErrorMessage input={password2}/>
        </div>

        <Button
            icon='sign-in'
            onSubmit={form.submit}
            disabled={form.hasInvalid()}
            tabIndex={3}>
            <Msg id='landing.reset-password.button'/>
        </Button>
    </form>

export default ResetPassword = form(ResetPassword, {
    reducers:
        [locationReducer],

    componentWillMount:
        ({location}) => validateToken(location.query.token),

    onSubmit:
        ({token, password}) => resetPassword(token, password),

    inputs:
        {
            password: new Constraints()
                .notBlank('landing.reset-password.password.required'),
            password2: new Constraints()
                .notBlank('landing.reset-password.password2.required')
                .predicate((password2, form) => password2 === form.password, 'landing.reset-password.password2.not-matching')
        }
})