package ru.kotlix.frame.session.server.handler.strategy

import io.netty.channel.ChannelHandlerContext
import io.netty.util.AttributeKey
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import ru.kotlix.frame.session.api.SessionContract
import ru.kotlix.frame.session.server.handler.serverResponse
import ru.kotlix.frame.session.server.handler.sessionBreak
import ru.kotlix.frame.session.server.service.ChannelRegistry
import ru.kotlix.frame.session.server.service.MessagePreferencesValidator
import ru.kotlix.frame.session.server.service.dto.RegisteredChannel
import ru.kotlix.frame.state.api.dto.UpdateUserStatusRequest
import ru.kotlix.frame.state.client.UserStateClient
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

@Component("servingStrategy")
class ClientServingStrategy(
    @Qualifier("waitersExecutor")
    private val executorService: ExecutorService,
    @Value("\${session.heartbeat-timeout}")
    private val heartbeatTimeout: Long,
    private val channelRegistry: ChannelRegistry,
    private val messagePreferencesValidator: MessagePreferencesValidator,
    private val userStateClient: UserStateClient,
) : ClientHandlerStrategy {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val servingStateKey = AttributeKey.valueOf<ServingState>("serving-state")

    data class ServingState(
        var awaitingHeartbeat: Future<*>,
        var registeredChannel: RegisteredChannel,
    )

    override fun onStrategyEnabled(ctx: ChannelHandlerContext?) {
        val context = ctx ?: throw IllegalStateException("Context required.")
        val userInfo =
            context.channel().attr(USER_INFO_KEY).get()
                ?: throw IllegalStateException("UserInfo attribute required.")

        try {
            userStateClient.updateUserStatus(userInfo.id, UpdateUserStatusRequest(true))
        } catch (ex: RuntimeException) {
            logger.error(
                "${context.name()}=${context.channel().remoteAddress()} : unable to set user online.",
                ex,
            )
            context.writeAndFlush(sessionBreak(SessionContract.ServerPacket.SessionBreak.BreakCause.ERROR))
            return
        }

        val registeredChannel = channelRegistry.register(userInfo.id, context.channel())
        val state =
            ServingState(
                awaitingHeartbeat = submitHeartbeatAwait(context, registeredChannel),
                registeredChannel = registeredChannel,
            )
        context.channel().attr(servingStateKey).set(state)
    }

    override fun filter(packet: SessionContract.ClientPacket): Boolean = packet.hasHeartbeat() || packet.hasMessageNotifyPrefs()

    override fun onPacket(
        context: ChannelHandlerContext,
        packet: SessionContract.ClientPacket,
    ) {
        val state = context.channel().attr(servingStateKey).get() ?: return
        when {
            packet.hasHeartbeat() -> onHeartbeat(context, state)
            packet.hasMessageNotifyPrefs() -> onMessageNotifyPreferences(context, state, packet.messageNotifyPrefs)
        }
    }

    private fun onHeartbeat(
        context: ChannelHandlerContext,
        state: ServingState,
    ) {
        synchronized(state) {
            if (state.awaitingHeartbeat.isDone) {
                logger.warn(
                    "{}={} : heartbeat present, but timeout happened.",
                    context.name(),
                    context.channel().remoteAddress(),
                )
                return
            }
            state.awaitingHeartbeat.cancel(true)
            if (!state.awaitingHeartbeat.isCancelled) {
                logger.warn(
                    "{}={} : heartbeat present, but timeout job cannot be cancelled.",
                    context.name(),
                    context.channel().remoteAddress(),
                )
                return
            }
            state.awaitingHeartbeat = submitHeartbeatAwait(context, state.registeredChannel)
        }
    }

    private fun onMessageNotifyPreferences(
        context: ChannelHandlerContext,
        state: ServingState,
        prPacket: SessionContract.ClientPacket.MessageNotifyPreferences,
    ) {
        val regChannel = state.registeredChannel
        val listensCommunities = prPacket.communityIdList

        if (messagePreferencesValidator.canBeNotifiedBy(regChannel.userId, listensCommunities)) {
            regChannel.awaitsMessagesFromCommunities = listensCommunities

            context.writeAndFlush(
                serverResponse(
                    SessionContract.ServerPacket.ServerResponse.PacketStatus.ACK,
                    prPacket.pid,
                ),
            ).addListener {
                context.close()
            }
        } else {
            context.writeAndFlush(
                serverResponse(
                    SessionContract.ServerPacket.ServerResponse.PacketStatus.NACK,
                    prPacket.pid,
                ),
            ).addListener {
                context.close()
            }
        }
    }

    private fun submitHeartbeatAwait(
        context: ChannelHandlerContext,
        registeredChannel: RegisteredChannel,
    ) = executorService.submit {
        logger.debug(
            "{}={} : heartbeat awaiting for {}ms.",
            context.name(),
            context.channel().remoteAddress(),
            heartbeatTimeout,
        )
        try {
            Thread.sleep(heartbeatTimeout)
            logger.info(
                "{}={} : heartbeat timed out.",
                context.name(),
                context.channel().remoteAddress(),
            )
            channelRegistry.remove(registeredChannel)
            context.writeAndFlush(sessionBreak(SessionContract.ServerPacket.SessionBreak.BreakCause.TIMED_OUT))
                .addListener {
                    context.close()
                }
        } catch (ignored: InterruptedException) {
            logger.debug(
                "{}={} : heartbeat passed.",
                context.name(),
                context.channel().remoteAddress(),
            )
        }
    }

    override fun onStrategyDisabled(
        context: ChannelHandlerContext,
        reason: StrategyChangeReason,
    ) {
        val state = context.channel().attr(servingStateKey).get() ?: return
        if (!state.awaitingHeartbeat.isDone && !state.awaitingHeartbeat.isCancelled) {
            state.awaitingHeartbeat.cancel(true)
        }

        channelRegistry.remove(state.registeredChannel)
        try {
            userStateClient.updateUserStatus(state.registeredChannel.userId, UpdateUserStatusRequest(false))
        } catch (ex: RuntimeException) {
            logger.error(
                "${context.name()}=${context.channel().remoteAddress()} : unable to set user offline.",
                ex,
            )
            return
        }
    }
}
