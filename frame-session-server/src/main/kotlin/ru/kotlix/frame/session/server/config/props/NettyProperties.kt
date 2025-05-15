package ru.kotlix.frame.session.server.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "netty.server")
data class NettyProperties(
    var port: Int,
    var bossCount: Int = 1,
    var workerCount: Int = 2,
    var backlogSO: Int = 128,
)
