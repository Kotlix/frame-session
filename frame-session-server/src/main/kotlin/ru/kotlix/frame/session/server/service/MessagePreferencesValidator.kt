package ru.kotlix.frame.session.server.service

interface MessagePreferencesValidator {
    fun canBeNotifiedBy(
        userId: Long,
        community: List<Long>,
    ): Boolean
}
