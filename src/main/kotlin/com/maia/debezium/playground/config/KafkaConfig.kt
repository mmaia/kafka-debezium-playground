package com.maia.debezium.playground.config

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.EnableKafkaStreams
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration
import org.springframework.kafka.config.KafkaStreamsConfiguration
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaAdmin.NewTopics

@Configuration
@EnableKafka
@EnableKafkaStreams
class KafkaConfig(val kafkaProps: KafkaProps) {

    @Bean
    fun appTopics(): NewTopics {
        return NewTopics(
            TopicBuilder.name(USER_PETS_TOPIC).compact().build()
        )
    }

    @Bean(name = [KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME])
    fun defaultKafkaStreamsConfig(): KafkaStreamsConfiguration {
        val props: MutableMap<String, Any> = HashMap()
        props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaProps.bootstrapServers
        props[StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG] =
            LogAndContinueExceptionHandler::class.java
        props[AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] = kafkaProps.schemaRegistryUrl
        props[StreamsConfig.APPLICATION_ID_CONFIG] = "user-pets-stream"
        props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = SpecificAvroSerde::class.java
        props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = SpecificAvroSerde::class.java
        props[ConsumerConfig.GROUP_ID_CONFIG] = "user-pets-stream-group"
        return KafkaStreamsConfiguration(props)
    }

    @Bean
    fun configurer(): StreamsBuilderFactoryBeanConfigurer? {
        return StreamsBuilderFactoryBeanConfigurer { fb: StreamsBuilderFactoryBean ->
            fb.setStateListener { newState: KafkaStreams.State, oldState: KafkaStreams.State ->
                println("State transition from $oldState to $newState")
            }
        }
    }
}

const val USER_PETS_TOPIC = "user-pets-topic"
const val USERS_TABLE = "users-table"