package ru.kotlix.frame.session.server.config

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import ru.kotlix.frame.session.server.config.props.KafkaAuthProperties
import java.util.UUID

@EnableKafka
@Configuration
class KafkaConfig {
    @Bean
    fun consumerFactory(
        kafkaProperties: KafkaProperties,
        kafkaAuthProperties: KafkaAuthProperties,
    ): ConsumerFactory<String, String> =
        DefaultKafkaConsumerFactory(
            HashMap<String, Any>()
                .withEntry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers)
                .withEntry(ConsumerConfig.GROUP_ID_CONFIG to groupId(kafkaProperties.consumer.groupId))
                .withEntry(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java)
                .withEntry(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java)
                .withEntry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SASL_PLAINTEXT")
                .withEntry(SaslConfigs.SASL_MECHANISM to "PLAIN")
                .withEntry(SaslConfigs.SASL_JAAS_CONFIG to saslJaasConfig(kafkaAuthProperties)),
        )

    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<String, String>,
    ): ConcurrentKafkaListenerContainerFactory<String, String> =
        ConcurrentKafkaListenerContainerFactory<String, String>().apply {
            this.consumerFactory = consumerFactory
        }

    private fun saslJaasConfig(kafkaAuth: KafkaAuthProperties) =
        "org.apache.kafka.common.security.plain.PlainLoginModule required " +
            "username=\"${kafkaAuth.username}\" " +
            "password=\"${kafkaAuth.password}\" " +
            "user_${kafkaAuth.username}=\"${kafkaAuth.password}\";"

    private fun groupId(configGroupId: String): String {
        val currentInstance = UUID.randomUUID()
        return "$configGroupId-$currentInstance"
    }

    private fun <K, V> MutableMap<K, V>.withEntry(entry: Pair<K, V>) =
        this.apply {
            this[entry.first] = entry.second
        }
}
