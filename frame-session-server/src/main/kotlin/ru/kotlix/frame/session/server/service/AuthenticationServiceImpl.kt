package ru.kotlix.frame.session.server.service

import com.fasterxml.jackson.databind.ObjectMapper
import feign.FeignException
import org.springframework.stereotype.Service
import ru.kotlix.frame.auth.client.AuthClient
import ru.kotlix.frame.auth.token.JWSTokenDecoder
import ru.kotlix.frame.session.server.mapper.toServiceUserInfo
import ru.kotlix.frame.session.server.service.dto.UserInfo

@Service
class AuthenticationServiceImpl(
    private val authClient: AuthClient,
    objectMapper: ObjectMapper,
) : AuthenticationService {
    private val jwsDecoder = JWSTokenDecoder(objectMapper)

    override fun authenticate(token: String): UserInfo? {
        try {
            authClient.checkAuth(token)
            return jwsDecoder.getPayload(token).toServiceUserInfo()
        } catch (exception: FeignException) {
            return null
        }
    }
}
