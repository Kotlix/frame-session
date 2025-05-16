package ru.kotlix.frame.session.server.listener

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import ru.kotlix.frame.session.api.kafka.MessageNotification
import ru.kotlix.frame.session.server.mapper.toServiceMessageNotification
import ru.kotlix.frame.session.server.service.NotifierService

@Component
class MessageNotifyListener(
    private val notifierService: NotifierService,
) {
    @KafkaListener(topics = ["message"])
    fun onMessage(value: MessageNotification) {
        notifierService.messageNotifyUsers(value.toServiceMessageNotification())
    }
}
