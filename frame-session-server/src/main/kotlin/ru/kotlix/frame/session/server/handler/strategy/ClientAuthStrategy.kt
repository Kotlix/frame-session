package ru.kotlix.frame.session.server.handler.strategy

import feign.FeignException
import io.netty.channel.ChannelHandlerContext
import io.netty.util.AttributeKey
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.kotlix.frame.session.api.SessionContract
import ru.kotlix.frame.session.api.SessionContract.ServerPacket.ServerResponse.PacketStatus
import ru.kotlix.frame.session.server.handler.serverResponse
import ru.kotlix.frame.session.server.handler.sessionBreak
import ru.kotlix.frame.session.server.service.AuthenticationService
import ru.kotlix.frame.state.client.UserStateClient
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

@Component("authStrategy")
class ClientAuthStrategy(
    @Qualifier("waitersExecutor")
    private val executorService: ExecutorService,
    @Value("\${session.login-timeout}")
    private val loginTimeout: Long,
    @Qualifier("servingStrategy")
    private val servingStrategy: ClientHandlerStrategy,
    private val authenticationService: AuthenticationService,
    private val userStateClient: UserStateClient,
) : ClientHandlerStrategy {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val authStateKey = AttributeKey.valueOf<AuthState>("auth-state")

    data class AuthState(
        val awaitingAuth: Future<*>,
        var attempted: Boolean,
    )

    override fun filter(packet: SessionContract.ClientPacket): Boolean = packet.hasAuthReq()

    override fun onStrategyEnabled(ctx: ChannelHandlerContext?) {
        val context = ctx ?: throw IllegalStateException("Context can not be null.")

        val awaitingAuth =
            executorService.submit {
                logger.debug(
                    "{}={} : authentication awaiting for {}ms.",
                    context.name(),
                    context.channel().remoteAddress(),
                    loginTimeout,
                )
                try {
                    Thread.sleep(loginTimeout)
                    logger.info(
                        "{}={} : authentication timed out.",
                        context.name(),
                        context.channel().remoteAddress(),
                    )
                    context.writeAndFlush(sessionBreak(SessionContract.ServerPacket.SessionBreak.BreakCause.TIMED_OUT))
                        .addListener {
                            context.close()
                        }
                } catch (ignored: InterruptedException) {
                    logger.info(
                        "{}={} : authentication passed.",
                        context.name(),
                        context.channel().remoteAddress(),
                    )
                }
            }
        context.channel().attr(authStateKey).set(AuthState(awaitingAuth, false))
    }

    override fun onPacket(
        context: ChannelHandlerContext,
        packet: SessionContract.ClientPacket,
    ) {
        val authPacket = packet.authReq

        val authState = context.channel().attr(authStateKey).get()
        synchronized(authState) {
            if (authState.attempted) {
                return
            }
            authState.attempted = true
        }

        val userInfo =
            authenticationService.authenticate(authPacket.token)
                ?: run {
                    context.writeAndFlush(sessionBreak(SessionContract.ServerPacket.SessionBreak.BreakCause.WRONG_AUTH))
                        .addListener {
                            context.close()
                        }
                    return
                }
        if (authState.awaitingAuth.isDone) {
            logger.warn(
                "{}={} : authentication passed, but timeout happened.",
                context.name(),
                context.channel().remoteAddress(),
            )
            return
        }
        authState.awaitingAuth.cancel(true)
        if (!authState.awaitingAuth.isCancelled) {
            logger.warn(
                "{}={} : authentication passed, but timeout job cannot be cancelled.",
                context.name(),
                context.channel().remoteAddress(),
            )
            return
        }

        val isOnline =
            try {
                userStateClient.getUserStatus(userInfo.id).online
            } catch (ex: FeignException.NotFound) {
                false
            } catch (ex: RuntimeException) {
                logger.error(
                    "${context.name()}=${context.channel().remoteAddress()} : " +
                        "authentication passed, but state request failed.",
                    ex,
                )
                context.writeAndFlush(sessionBreak(SessionContract.ServerPacket.SessionBreak.BreakCause.ERROR))
                return
            }
        if (isOnline) {
            context.writeAndFlush(sessionBreak(SessionContract.ServerPacket.SessionBreak.BreakCause.ALREADY_LOGGED))
            return
        }

        context.channel().attr(USER_INFO_KEY).set(userInfo)
        switchStrategy(context, servingStrategy, logger)
        context.writeAndFlush(serverResponse(PacketStatus.ACK, authPacket.pid))
    }

    override fun onStrategyDisabled(
        context: ChannelHandlerContext,
        reason: StrategyChangeReason,
    ) {
        val state = context.channel().attr(authStateKey).get()
        if (!state.awaitingAuth.isDone && !state.awaitingAuth.isCancelled) {
            state.awaitingAuth.cancel(true)
        }
        context.channel().attr(authStateKey).set(null)
    }
}
