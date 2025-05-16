package ru.kotlix.frame.session.server.service

import ru.kotlix.frame.session.server.service.dto.UserInfo

interface AuthenticationService {
    fun authenticate(token: String): UserInfo?
}
