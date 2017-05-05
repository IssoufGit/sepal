package org.openforis.sepal.component.user.command

import org.openforis.sepal.command.AbstractCommand
import org.openforis.sepal.command.CommandHandler
import org.openforis.sepal.component.user.adapter.GoogleOAuthClient
import org.openforis.sepal.component.user.api.UserRepository
import org.openforis.sepal.user.GoogleTokens
import org.openforis.sepal.util.annotation.Data

@Data(callSuper = true)
class RevokeGoogleAccountAccess extends AbstractCommand<Void> {
    GoogleTokens tokens
}

class RevokeGoogleAccountAccessHandler implements CommandHandler<Void, RevokeGoogleAccountAccess> {
    private final GoogleOAuthClient oAuthClient
    private final UserRepository userRepository

    RevokeGoogleAccountAccessHandler(GoogleOAuthClient oAuthClient, UserRepository userRepository) {
        this.oAuthClient = oAuthClient
        this.userRepository = userRepository
    }

    Void execute(RevokeGoogleAccountAccess command) {
        userRepository.updateGoogleTokens(command.username, null)
        oAuthClient.revokeTokens(command.username, command.tokens)
        return null
    }
}