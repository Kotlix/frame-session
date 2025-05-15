package ru.kotlix.frame.session.server.mapper

import ru.kotlix.frame.session.api.kafka.MessageNotification
import ru.kotlix.frame.session.server.handler.messageNotify
import ru.kotlix.frame.session.server.service.dto.UserInfo
import ru.kotlix.frame.auth.api.token.dto.UserInfo as AuthUserInfo
import ru.kotlix.frame.session.server.service.dto.MessageNotification as ServiceMessageNotification

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
