package ru.kotlix.frame.session.server.service

import io.netty.channel.Channel
import org.springframework.stereotype.Service
import ru.kotlix.frame.session.server.service.dto.RegisteredChannel
import java.util.concurrent.ConcurrentHashMap

@Service
class ChannelRegistryImpl : ChannelRegistry {
    private val registered = ConcurrentHashMap<Long, RegisteredChannel>()

    override fun register(
        id: Long,
        channel: Channel,
    ): RegisteredChannel {
        val rc =
            RegisteredChannel(
                userId = id,
                channel = channel,
            )
        registered[id] = rc
        return rc
    }

    override fun remove(registeredChannel: RegisteredChannel) {
        registered.remove(registeredChannel.userId)
    }

    override fun findRegistered(): List<RegisteredChannel> = registered.values.toList()
}
