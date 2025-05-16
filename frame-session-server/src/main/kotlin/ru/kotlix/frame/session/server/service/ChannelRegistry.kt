package ru.kotlix.frame.session.server.service

import io.netty.channel.Channel
import ru.kotlix.frame.session.server.service.dto.RegisteredChannel

interface ChannelRegistry {
    fun register(
        id: Long,
        channel: Channel,
    ): RegisteredChannel

    fun remove(registeredChannel: RegisteredChannel)

    fun findRegistered(): List<RegisteredChannel>
}
