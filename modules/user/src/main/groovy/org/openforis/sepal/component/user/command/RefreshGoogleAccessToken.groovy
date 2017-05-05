package org.openforis.sepal.component.user.command

import org.openforis.sepal.command.AbstractCommand
import org.openforis.sepal.command.CommandHandler
import org.openforis.sepal.component.user.adapter.GoogleOAuthClient
import org.openforis.sepal.component.user.api.UserRepository
import org.openforis.sepal.user.GoogleTokens
import org.openforis.sepal.util.annotation.Data

@Data(callSuper = true)
class RefreshGoogleAccessToken extends AbstractCommand<GoogleTokens> {
    GoogleTokens tokens
}

class RefreshGoogleAccessTokenHandler implements CommandHandler<GoogleTokens, RefreshGoogleAccessToken> {
    private final GoogleOAuthClient oAuthClient
    private final UserRepository userRepository

    RefreshGoogleAccessTokenHandler(GoogleOAuthClient oAuthClient, UserRepository userRepository) {
        this.oAuthClient = oAuthClient
        this.userRepository = userRepository
    }

    GoogleTokens execute(RefreshGoogleAccessToken command) {
        def tokens = oAuthClient.refreshAccessToken(command.username, command.tokens)
        userRepository.updateGoogleTokens(command.username, tokens)
        return tokens
    }
}
