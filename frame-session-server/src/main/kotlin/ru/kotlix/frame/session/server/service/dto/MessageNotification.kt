package ru.kotlix.frame.session.server.service.dto

data class MessageNotification(
    val fromChatId: Long,
    val fromCommunityId: Long,
    val fromUserId: Long,
    val content: String,
)
