package ru.kotlix.frame.session.server.service

import io.netty.channel.Channel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.kotlix.frame.session.server.mapper.toServerPacketMessageNotify
import ru.kotlix.frame.session.server.mapper.toServerPacketVoiceNotify
import ru.kotlix.frame.session.server.service.dto.MessageNotification
import ru.kotlix.frame.session.server.service.dto.VoiceNotification

@Service
class NotifierServiceImpl(
    private val channelRegistry: ChannelRegistry,
) : NotifierService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun messageNotifyUsers(messageNotification: MessageNotification) {
        logger.debug("MessageNotification = {}.", messageNotification)
        val subscribers = channelRegistry.findRegistered()
        if (subscribers.isEmpty()) {
            logger.debug("Nobody is being served for MessageNotification.")
            return
        }

        subscribers.forEach {
            val communities =
                it.listensCommunities
                    ?: run {
                        logger.debug("{} not provided awaiting communities.", it.channel.remoteAddress())
                        return@forEach
                    }
            if (communities.contains(messageNotification.fromCommunityId)) {
                messageNotifyChannel(it.channel, messageNotification)
            }
        }
    }

    private fun messageNotifyChannel(
        channel: Channel,
        messageNotification: MessageNotification,
    ) {
        logger.debug("{} is going to be notified.", channel.remoteAddress())
        if (!channel.isActive) {
            logger.warn("Unable to notify message to ${channel.remoteAddress()}. Channel is not active.")
            return
        }
        if (!channel.isOpen) {
            logger.warn("Unable to notify message to ${channel.remoteAddress()}. Channel is not open.")
            return
        }

        channel.writeAndFlush(messageNotification.toServerPacketMessageNotify())
        logger.debug("{} notified.", channel.remoteAddress())
    }

    override fun voiceNotifyUsers(voiceNotification: VoiceNotification) {
        logger.debug("VoiceNotification = {}.", voiceNotification)
        val subscribers = channelRegistry.findRegistered()
        if (subscribers.isEmpty()) {
            logger.debug("Nobody is being served for VoiceNotification.")
            return
        }
        val toBeNotified = (voiceNotification.party + voiceNotification.change).map { it.userId }

        subscribers.filter { toBeNotified.contains(it.userId) }.forEach {
            voiceNotifyChannel(it.channel, voiceNotification)
        }
    }

    private fun voiceNotifyChannel(
        channel: Channel,
        voiceNotification: VoiceNotification,
    ) {
        logger.debug("{} is going to be notified.", channel.remoteAddress())
        if (!channel.isActive) {
            logger.warn("Unable to notify voice to ${channel.remoteAddress()}. Channel is not active.")
            return
        }
        if (!channel.isOpen) {
            logger.warn("Unable to notify voice to ${channel.remoteAddress()}. Channel is not open.")
            return
        }

        channel.writeAndFlush(voiceNotification.toServerPacketVoiceNotify())
        logger.debug("{} notified.", channel.remoteAddress())
    }
}
