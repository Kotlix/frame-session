package ru.kotlix.frame.session.server.service.dto

import io.netty.channel.Channel

data class RegisteredChannel(
    val userId: Long,
    val channel: Channel,
    var awaitsMessagesFromCommunities: List<Long>? = null,
)
