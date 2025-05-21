package ru.kotlix.frame.session.server.mapper

import ru.kotlix.frame.router.api.kafka.UpdateInfo
import ru.kotlix.frame.router.api.kafka.VoiceNotification
import ru.kotlix.frame.session.api.kafka.MessageNotification
import ru.kotlix.frame.session.server.handler.messageNotify
import ru.kotlix.frame.session.server.handler.voiceNotify
import ru.kotlix.frame.session.server.service.dto.Attendant
import ru.kotlix.frame.session.server.service.dto.UserInfo
import ru.kotlix.frame.auth.api.token.dto.UserInfo as AuthUserInfo
import ru.kotlix.frame.session.server.service.dto.MessageNotification as ServiceMessageNotification
import ru.kotlix.frame.session.server.service.dto.VoiceNotification as ServiceVoiceNotification

fun AuthUserInfo.toServiceUserInfo() =
    UserInfo(
        id = id,
    )

fun MessageNotification.toServiceMessageNotification() =
    ServiceMessageNotification(
        fromChatId = chatId,
        fromCommunityId = communityId,
        fromUserId = senderId,
        content = textContent,
    )

fun ServiceMessageNotification.toServerPacketMessageNotify() =
    messageNotify(
        fromChatId = fromChatId,
        fromCommunityId = fromCommunityId,
        fromUserId = fromUserId,
        content = content,
    )

fun VoiceNotification.toServiceVoiceNotification() =
    ServiceVoiceNotification(
        voiceId = voiceInfo.voiceId,
        party =
            voiceInfo.party.map { att ->
                Attendant(
                    userId = att.userId,
                    shadowId = att.shadowId,
                )
            },
        change =
            updateInfo.attendant.let { att ->
                Attendant(
                    userId = att.userId,
                    shadowId = att.shadowId,
                )
            },
        action =
            when (updateInfo.action) {
                UpdateInfo.Action.JOINED -> ru.kotlix.frame.session.server.service.dto.VoiceNotification.Action.JOINED
                UpdateInfo.Action.LEFT -> ru.kotlix.frame.session.server.service.dto.VoiceNotification.Action.LEFT
                null -> throw IllegalStateException("updateInfo.action should never be null.")
            },
    )

fun ServiceVoiceNotification.toServerPacketVoiceNotify() =
    voiceNotify(
        voiceId = voiceId,
        party = party,
        changed = change,
        action = action,
    )
