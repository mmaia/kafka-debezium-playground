package com.maia.debezium.playground.repository

import com.maia.debezium.playground.UserPet
import com.maia.debezium.playground.config.KafkaProps
import com.maia.debezium.playground.config.USERS_TABLE
import com.maia.debezium.playground.config.USER_PETS_TOPIC
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import kafka_connect_studies.kafka_connect_studies.users.Envelope
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.GlobalKTable
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Repository
import java.util.*
import javax.annotation.PostConstruct
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.springframework.kafka.config.StreamsBuilderFactoryBean

@Repository
class UserPetStream (kafkaProps: KafkaProps) {

    private val usersValueSerde = SpecificAvroSerde<Envelope>()
    private lateinit var userView: ReadOnlyKeyValueStore<Long, Envelope>

    private val userPetSerde = SpecificAvroSerde<UserPet>()

    val serdeConfig: MutableMap<String, String> = Collections.singletonMap(
        AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaProps.schemaRegistryUrl
    )

    @PostConstruct
    fun init() {
        userPetSerde.configure(serdeConfig, false)
    }

    @Bean
    fun afterStartUsers(sbfb: StreamsBuilderFactoryBean): StreamsBuilderFactoryBean.Listener {
        val listener: StreamsBuilderFactoryBean.Listener = object : StreamsBuilderFactoryBean.Listener {
            override fun streamsAdded(id: String, streams: KafkaStreams) {
                userView = streams.store<ReadOnlyKeyValueStore<Long, Envelope>>(
                    StoreQueryParameters.fromNameAndType(
                        USERS_TABLE,
                        QueryableStoreTypes.keyValueStore()
                    )
                )
            }
        }

        sbfb.addListener(listener)
        return listener
    }

    @Bean
    fun userByIdGKTable(streamsBuilder: StreamsBuilder): GlobalKTable<Long, Envelope> {
        return streamsBuilder.globalTable(USER_PETS_TOPIC,
         Materialized.`as`<Long, Envelope, KeyValueStore<Bytes, ByteArray>>(USERS_TABLE)
            .withKeySerde(Serdes.Long())
                .withValueSerde(usersValueSerde))
    }

}