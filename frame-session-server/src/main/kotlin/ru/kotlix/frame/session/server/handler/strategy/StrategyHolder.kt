package ru.kotlix.frame.session.server.handler.strategy

import io.netty.channel.ChannelHandlerContext
import io.netty.util.AttributeKey
import org.slf4j.Logger
import ru.kotlix.frame.session.server.service.dto.UserInfo

val USER_INFO_KEY = AttributeKey.valueOf<UserInfo>("user-info")

val STRATEGY_STATE_KEY = AttributeKey.valueOf<StrategyState>("strategy")

data class StrategyState(
    var currentStrategy: ClientHandlerStrategy,
)

enum class StrategyChangeReason {
    CONNECTION_BROKEN,
    SWITCHED,
}

fun setStrategy(
    context: ChannelHandlerContext,
    strategy: ClientHandlerStrategy,
    logger: Logger,
) {
    context.channel().attr(STRATEGY_STATE_KEY).set(StrategyState(strategy))
    logger.debug(
        "{}={} : strategy {} enabled.",
        context.name(),
        context.channel().remoteAddress(),
        strategy,
    )
    strategy.onStrategyEnabled(context)
}

fun switchStrategy(
    context: ChannelHandlerContext,
    strategy: ClientHandlerStrategy,
    logger: Logger,
) {
    val state = context.channel().attr(STRATEGY_STATE_KEY).get()
    logger.debug(
        "{}={} : strategy {} disabled.",
        context.name(),
        context.channel().remoteAddress(),
        strategy,
    )
    state.currentStrategy.onStrategyDisabled(context, StrategyChangeReason.SWITCHED)
    state.currentStrategy = strategy
    logger.debug(
        "{}={} : strategy {} enabled.",
        context.name(),
        context.channel().remoteAddress(),
        strategy,
    )
    strategy.onStrategyEnabled(context)
}
