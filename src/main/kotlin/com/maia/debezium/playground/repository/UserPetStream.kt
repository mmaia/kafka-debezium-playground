package com.maia.debezium.playground.repository

import com.maia.debezium.playground.UserPet
import com.maia.debezium.playground.config.DEB_USERS_TOPIC
import com.maia.debezium.playground.config.KafkaProps
import com.maia.debezium.playground.config.USERS_TABLE
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import kafka_connect_studies.kafka_connect_studies.users.Envelope
import kafka_connect_studies.kafka_connect_studies.users.Key
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.GlobalKTable
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.stereotype.Repository
import javax.annotation.PostConstruct

@Repository
class UserPetStream (kafkaProps: KafkaProps) {

    private val usersValueSerde = SpecificAvroSerde<Envelope>()
    private val usersKeySerde = SpecificAvroSerde<Key>()

    private lateinit var userView: ReadOnlyKeyValueStore<Key, Envelope>

    private val userPetSerde = SpecificAvroSerde<UserPet>()

    val serdeConfig: MutableMap<String, String> = hashMapOf(
        AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to kafkaProps.schemaRegistryUrl
    )

    @PostConstruct
    fun init() {
        userPetSerde.configure(serdeConfig, false)
        usersKeySerde.configure(serdeConfig, true)
        usersValueSerde.configure(serdeConfig, false)
    }

    @Bean
    fun afterStartUsers(sbfb: StreamsBuilderFactoryBean): StreamsBuilderFactoryBean.Listener {
        val listener: StreamsBuilderFactoryBean.Listener = object : StreamsBuilderFactoryBean.Listener {
            override fun streamsAdded(id: String, streams: KafkaStreams) {
                userView = streams.store<ReadOnlyKeyValueStore<Key, Envelope>>(
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
    fun userByIdGKTable(streamsBuilder: StreamsBuilder): GlobalKTable<Key, Envelope> {
        return streamsBuilder.globalTable(
            DEB_USERS_TOPIC, Materialized.`as`<Key, Envelope, KeyValueStore<Bytes, ByteArray>>(USERS_TABLE)
            .withKeySerde(usersKeySerde)
                .withValueSerde(usersValueSerde))
    }

    fun getUserById(id: Long): Envelope? {
        val userId = Key.newBuilder().setId(id).build()
        return userView.get(userId)
    }
}