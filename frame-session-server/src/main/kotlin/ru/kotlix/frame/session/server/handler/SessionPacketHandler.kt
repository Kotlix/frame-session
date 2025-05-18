package ru.kotlix.frame.session.server.handler

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import ru.kotlix.frame.session.api.proto.SessionContract
import ru.kotlix.frame.session.server.handler.strategy.ClientHandlerStrategy
import ru.kotlix.frame.session.server.handler.strategy.STRATEGY_STATE_KEY
import ru.kotlix.frame.session.server.handler.strategy.StrategyChangeReason
import ru.kotlix.frame.session.server.handler.strategy.setStrategy
import java.net.SocketException

@Component
@Sharable
class SessionPacketHandler(
    @Qualifier("authStrategy")
    private val authStrategy: ClientHandlerStrategy,
) : SimpleChannelInboundHandler<SessionContract.ClientPacket>() {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val enabled = true

    override fun channelActive(ctx: ChannelHandlerContext?) {
        val context = ctx ?: throw IllegalStateException("Context can not be null.")
        logger.info("${context.name()}=${context.channel().remoteAddress()} : connection established.")

        try {
            setStrategy(context, authStrategy, logger)
        } catch (ex: RuntimeException) {
            logger.error("Error happened during setting strategy", ex)
            context.writeAndFlush(sessionBreak(SessionContract.ServerPacket.SessionBreak.BreakCause.ERROR))
                .addListener {
                    context.close()
                }
        }
    }

    override fun channelRead0(
        ctx: ChannelHandlerContext?,
        pkt: SessionContract.ClientPacket?,
    ) {
        if (!enabled) {
            return
        }
        val context = ctx ?: throw IllegalStateException("Context can not be null.")
        val packet = pkt ?: throw IllegalStateException("Packet can not be null.")
        logger.debug("{}={} : received packet {}.", context.name(), context.channel().remoteAddress(), packet)

        val strategy = context.channel().attr(STRATEGY_STATE_KEY).get().currentStrategy
        if (!strategy.filter(packet)) {
            logger.debug("{}={} : filtered out packet.", context.name(), context.channel().remoteAddress())
            return
        }
        try {
            strategy.onPacket(context, packet)
        } catch (ex: RuntimeException) {
            logger.error("Error happened during packet handling", ex)
            context.writeAndFlush(sessionBreak(SessionContract.ServerPacket.SessionBreak.BreakCause.ERROR))
                .addListener {
                    context.close()
                }
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        val context = ctx ?: throw IllegalStateException("Context can not be null.")
        logger.info("${context.name()}=${context.channel().remoteAddress()} : connection broken.")

        val strategy = context.channel().attr(STRATEGY_STATE_KEY).get().currentStrategy
        try {
            strategy.onStrategyDisabled(context, StrategyChangeReason.CONNECTION_BROKEN)
        } catch (ex: RuntimeException) {
            logger.error("Error happened during strategy disable", ex)
        }
    }

    override fun exceptionCaught(
        ctx: ChannelHandlerContext?,
        cause: Throwable?,
    ) {
        if (cause is SocketException) {
            ctx?.channel()?.close()
        }
    }
}
