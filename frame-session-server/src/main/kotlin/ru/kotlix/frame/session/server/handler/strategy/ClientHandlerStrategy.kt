package ru.kotlix.frame.session.server.handler.strategy

import io.netty.channel.ChannelHandlerContext
import ru.kotlix.frame.session.api.SessionContract

interface ClientHandlerStrategy {
    fun onStrategyEnabled(ctx: ChannelHandlerContext?)

    fun onStrategyDisabled(
        context: ChannelHandlerContext,
        reason: StrategyChangeReason,
    )

    fun filter(packet: SessionContract.ClientPacket): Boolean

    fun onPacket(
        context: ChannelHandlerContext,
        packet: SessionContract.ClientPacket,
    )
}
