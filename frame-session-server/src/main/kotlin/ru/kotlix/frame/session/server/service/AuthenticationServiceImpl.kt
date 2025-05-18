package ru.kotlix.frame.session.server.service

import com.fasterxml.jackson.databind.ObjectMapper
import feign.FeignException
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val jwsDecoder = JWSTokenDecoder(objectMapper)

    override fun authenticate(token: String): UserInfo? {
        try {
            logger.debug("Checking token $token")
            authClient.checkAuth(token)
            logger.debug("Check passed.")
            return jwsDecoder.getPayload(token).toServiceUserInfo()
        } catch (exception: FeignException.BadRequest) {
            return null
        } catch (exception: FeignException) {
            logger.debug("Check failed with error $exception")
            throw RuntimeException(exception)
        }
    }
}
