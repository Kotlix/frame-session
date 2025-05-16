package ru.kotlix.frame.session.server.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors

@Configuration
class ExecutorsConfig {
    @Bean("waitersExecutor")
    fun waitersExecutor() = Executors.newCachedThreadPool()
}
