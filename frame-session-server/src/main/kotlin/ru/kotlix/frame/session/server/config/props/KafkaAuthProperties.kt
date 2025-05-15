package ru.kotlix.frame.session.server.config.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "session.kafka.authentication")
data class KafkaAuthProperties(
    var username: String,
    var password: String,
)
