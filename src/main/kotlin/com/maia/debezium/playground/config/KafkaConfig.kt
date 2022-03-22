package com.maia.debezium.playground.config

import com.maia.debezium.playground.repository.PriceHistoryTimestampExtractor
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaAdmin.NewTopics
import java.util.*


@Configuration
@EnableKafka
class KafkaConfig(val kafkaProps: KafkaProps) {

    @Bean
    fun appTopics(): NewTopics {
        return NewTopics(
            TopicBuilder.name(USERS_TOPIC).compact().build(),
            TopicBuilder.name(USER_PETS_TOPIC).compact().build(),
            TopicBuilder.name(PETS_TOPIC).build(),
            TopicBuilder.name(USER_PETS_AGGR_TOPIC).compact().build(),
            TopicBuilder.name(PRICE_HIST_AGGR_TOPIC).compact().build()
        )
    }

    @Bean(name = [DEFAULT_STREAM_BEAN])
    fun streamsBuilderFactoryBean(): StreamsBuilderFactoryBean {
        val props = defaultStreamsConfig()
        props.putAll(defaultStreamBeanConfig())
        val factory = StreamsBuilderFactoryBean()
        factory.setStreamsConfiguration(props)
        return factory
    }

    @Bean(name = [DEB_STREAM_BEAN])
    fun debStreamBean(): StreamsBuilderFactoryBean {
        val props = defaultStreamsConfig()
        props.putAll(debeziumStreamBeanConfig())
        val factory = StreamsBuilderFactoryBean()
        factory.setStreamsConfiguration(props)
        return factory
    }

    @Bean(name = [PRICE_HISTORY_STREAM_BEAN])
    fun priceHistoryStreamBean(): StreamsBuilderFactoryBean {
        val props = defaultStreamsConfig()
        props.putAll(priceHistoryStreamBeanConfig())
        val factory = StreamsBuilderFactoryBean()
        factory.setStreamsConfiguration(props)
        return factory
    }

    fun defaultStreamsConfig(): Properties {
        val props = Properties()
        props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaProps.bootstrapServers
        props[StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG] =
            LogAndContinueExceptionHandler::class.java
        props[AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG] = kafkaProps.schemaRegistryUrl
        props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = SpecificAvroSerde::class.java
        return props
    }

    fun debeziumStreamBeanConfig(): MutableMap<String, Any> {
        val props: MutableMap<String, Any> = HashMap()
        props[StreamsConfig.APPLICATION_ID_CONFIG] = "debezium-user-pets-stream"
       return props
    }

    fun defaultStreamBeanConfig(): Properties {
        val props = Properties()
        props[StreamsConfig.APPLICATION_ID_CONFIG] = "default-user-pets-stream"
        return props
    }

    fun priceHistoryStreamBeanConfig(): Properties {
        val props = Properties()
        props[StreamsConfig.APPLICATION_ID_CONFIG] = "price-history-stream"
        props[StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG] = PriceHistoryTimestampExtractor::class.java.name
        props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass.name
        return props
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

const val PRICE_HISTORY_STREAM_BEAN = "priceHistoryStream"
const val DEB_STREAM_BEAN = "debStream"
const val DEFAULT_STREAM_BEAN = "defaultStream"

const val DEB_PRICE_HISTORY_TOPIC = "kafka_connect_studies.kafka_connect_studies.price_history"
const val DEB_USERS_TOPIC = "kafka_connect_studies.kafka_connect_studies.users"
const val DEB_PETS_TOPIC = "kafka_connect_studies.kafka_connect_studies.pets"

const val PETS_TOPIC = "pets-topic"
const val USERS_TOPIC = "users-topic"
const val USER_PETS_TOPIC = "user-pets-topic"
const val USER_PETS_AGGR_TOPIC = "user-pets-aggr-topic"
const val PRICE_HIST_AGGR_TOPIC = "price-history-aggr-topic"

const val USERS_TABLE = "users-table"
const val USER_PETS_AGGR_TABLE = "user-pets-aggr-table"