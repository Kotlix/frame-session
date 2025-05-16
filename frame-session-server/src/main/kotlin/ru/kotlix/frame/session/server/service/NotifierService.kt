package ru.kotlix.frame.session.server.service

import ru.kotlix.frame.session.server.service.dto.MessageNotification

interface NotifierService {
    fun messageNotifyUsers(messageNotification: MessageNotification)
}
