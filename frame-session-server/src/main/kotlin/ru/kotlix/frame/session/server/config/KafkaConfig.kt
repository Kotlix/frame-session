package ru.kotlix.frame.session.server.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.VoidDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import ru.kotlix.frame.router.api.kafka.VoiceNotification
import ru.kotlix.frame.session.api.kafka.MessageNotification
import ru.kotlix.frame.session.server.config.props.KafkaAuthProperties
import java.util.UUID

@EnableKafka
@Configuration
class KafkaConfig {
    @Bean
    fun voiceNotificationConsumerFactory(
        kafkaProperties: KafkaProperties,
        kafkaAuthProperties: KafkaAuthProperties,
        objectMapper: ObjectMapper,
    ): ConsumerFactory<Void, VoiceNotification> =
        DefaultKafkaConsumerFactory<Void, VoiceNotification>(
            HashMap<String, Any>()
                .withEntry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers)
                .withEntry(ConsumerConfig.GROUP_ID_CONFIG to groupId(kafkaProperties.consumer.groupId))
                .withEntry(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to VoidDeserializer::class.java)
                .withEntry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SASL_PLAINTEXT")
                .withEntry(SaslConfigs.SASL_MECHANISM to "PLAIN")
                .withEntry(SaslConfigs.SASL_JAAS_CONFIG to saslJaasConfig(kafkaAuthProperties)),
        ).apply {
            valueDeserializer = JsonDeserializer(VoiceNotification::class.java, objectMapper)
        }

    @Bean
    fun voiceNotificationKafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<Void, VoiceNotification>,
    ): ConcurrentKafkaListenerContainerFactory<Void, VoiceNotification> =
        ConcurrentKafkaListenerContainerFactory<Void, VoiceNotification>().apply {
            this.consumerFactory = consumerFactory
        }

    @Bean
    fun messageNotificationConsumerFactory(
        kafkaProperties: KafkaProperties,
        kafkaAuthProperties: KafkaAuthProperties,
        objectMapper: ObjectMapper,
    ): ConsumerFactory<Void, MessageNotification> =
        DefaultKafkaConsumerFactory<Void, MessageNotification>(
            HashMap<String, Any>()
                .withEntry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers)
                .withEntry(ConsumerConfig.GROUP_ID_CONFIG to groupId(kafkaProperties.consumer.groupId))
                .withEntry(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to VoidDeserializer::class.java)
                .withEntry(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SASL_PLAINTEXT")
                .withEntry(SaslConfigs.SASL_MECHANISM to "PLAIN")
                .withEntry(SaslConfigs.SASL_JAAS_CONFIG to saslJaasConfig(kafkaAuthProperties)),
        ).apply {
            valueDeserializer = JsonDeserializer(MessageNotification::class.java, objectMapper)
        }

    @Bean
    fun messageNotificationKafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<Void, MessageNotification>,
    ): ConcurrentKafkaListenerContainerFactory<Void, MessageNotification> =
        ConcurrentKafkaListenerContainerFactory<Void, MessageNotification>().apply {
            this.consumerFactory = consumerFactory
        }

    private fun saslJaasConfig(kafkaAuth: KafkaAuthProperties) =
        "org.apache.kafka.common.security.plain.PlainLoginModule required " +
            "username=\"${kafkaAuth.username}\" " +
            "password=\"${kafkaAuth.password}\" " +
            "user_${kafkaAuth.username}=\"${kafkaAuth.password}\";"

    private val randomListenerSuffix: String = UUID.randomUUID().toString()

    private fun groupId(configGroupId: String): String {
        return "$configGroupId-$randomListenerSuffix"
    }

    private fun <K, V> MutableMap<K, V>.withEntry(entry: Pair<K, V>) =
        this.apply {
            this[entry.first] = entry.second
        }
}
