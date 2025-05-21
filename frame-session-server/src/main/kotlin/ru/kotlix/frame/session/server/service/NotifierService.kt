package ru.kotlix.frame.session.server.service

import ru.kotlix.frame.session.server.service.dto.MessageNotification
import ru.kotlix.frame.session.server.service.dto.VoiceNotification

interface NotifierService {
    fun messageNotifyUsers(messageNotification: MessageNotification)

    fun voiceNotifyUsers(voiceNotification: VoiceNotification)
}
