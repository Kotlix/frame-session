package ru.kotlix.frame.session.server.service

import io.netty.channel.Channel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.kotlix.frame.session.server.mapper.toServerPacketMessageNotify
import ru.kotlix.frame.session.server.service.dto.MessageNotification

@Service
class NotifierServiceImpl(
    private val channelRegistry: ChannelRegistry,
) : NotifierService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun messageNotifyUsers(messageNotification: MessageNotification) {
        channelRegistry.findRegistered().forEach {
            val communities = it.awaitsMessagesFromCommunities ?: return@forEach
            if (communities.contains(messageNotification.fromCommunityId)) {
                messageNotifyChannel(it.channel, messageNotification)
            }
        }
    }

    private fun messageNotifyChannel(
        channel: Channel,
        messageNotification: MessageNotification,
    ) {
        if (!channel.isActive) {
            logger.warn("Unable to notify message to channel=$channel. Channel is not active.")
            return
        }
        if (!channel.isOpen) {
            logger.warn("Unable to notify message to channel=$channel. Channel is not open.")
            return
        }

        channel.writeAndFlush(messageNotification.toServerPacketMessageNotify())
    }
}
