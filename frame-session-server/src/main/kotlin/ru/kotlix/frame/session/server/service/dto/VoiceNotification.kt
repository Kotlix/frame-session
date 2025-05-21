package ru.kotlix.frame.session.server.service.dto

data class VoiceNotification(
    val voiceId: Long,
    val party: List<Attendant>,
    val change: Attendant,
    val action: Action,
) {
    enum class Action {
        JOINED,
        LEFT,
    }
}
