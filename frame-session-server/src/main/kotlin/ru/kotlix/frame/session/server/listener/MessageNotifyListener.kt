package ru.kotlix.frame.session.server.listener

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import ru.kotlix.frame.session.api.kafka.MessageNotification
import ru.kotlix.frame.session.server.mapper.toServiceMessageNotification
import ru.kotlix.frame.session.server.service.NotifierService

@Component
class MessageNotifyListener(
    private val notifierService: NotifierService,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(topics = ["\${session.kafka.consumer.topic}"])
    fun onMessage(value: String) {
        logger.info("Received message from topic.")
        try {
            val message = objectMapper.readValue(value, MessageNotification::class.java)
            logger.debug("Notifying clients.")
            notifierService.messageNotifyUsers(message.toServiceMessageNotification())
        } catch (ex: RuntimeException) {
            logger.error("Error happened during message handle.")
        }
    }
}
