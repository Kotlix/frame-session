package ru.kotlix.frame.session.server.listener

import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import ru.kotlix.frame.router.api.kafka.VoiceNotification
import ru.kotlix.frame.session.server.mapper.toServiceVoiceNotification
import ru.kotlix.frame.session.server.service.NotifierService

@Component
class VoiceNotifyListener(
    private val notifierService: NotifierService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(
        topics = ["\${session.kafka.consumer.voices-topic}"],
        containerFactory = "voiceNotificationKafkaListenerContainerFactory",
    )
    fun onMessage(value: VoiceNotification) {
        logger.info("Received VoiceNotification from topic.")
        try {
            logger.debug("Notifying clients.")
            notifierService.voiceNotifyUsers(value.toServiceVoiceNotification())
        } catch (ex: RuntimeException) {
            logger.error("Error happened during message handle.")
        }
    }
}
