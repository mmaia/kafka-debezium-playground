package com.maia.debezium.playground.repository

import com.maia.debezium.playground.Pet
import com.maia.debezium.playground.User
import com.maia.debezium.playground.UserPet
import com.maia.debezium.playground.config.*
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.stereotype.Repository
import javax.annotation.PostConstruct


@Repository
class UserPetStream(private val kafkaProps: KafkaProps) {
    private val userSerde = SpecificAvroSerde<User>()
    private val petSerde = SpecificAvroSerde<Pet>()

    private val userPetSerde = SpecificAvroSerde<UserPet>()

    private lateinit var userView: ReadOnlyKeyValueStore<Long, User>


    val serdeConfig: MutableMap<String, String> = hashMapOf(
        AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to this.kafkaProps.schemaRegistryUrl
    )

    @PostConstruct
    fun init() {
        userPetSerde.configure(serdeConfig, false)
        petSerde.configure(serdeConfig, false)
        userSerde.configure(serdeConfig, false)
    }

    @Bean
    fun afterStartUsers(@Qualifier(DEFAULT_STREAM_BEAN) sbfb: StreamsBuilderFactoryBean): StreamsBuilderFactoryBean.Listener {
        val listener: StreamsBuilderFactoryBean.Listener = object : StreamsBuilderFactoryBean.Listener {
            override fun streamsAdded(id: String, streams: KafkaStreams) {
                userView = streams
                    .store<ReadOnlyKeyValueStore<Long, User>>(
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
    fun userByIdGKTable(@Qualifier(DEFAULT_STREAM_BEAN) streamsBuilder: StreamsBuilder): GlobalKTable<Long, User> {
        return streamsBuilder.globalTable(
            USERS_TOPIC, Materialized.`as`<Long, User, KeyValueStore<Bytes, ByteArray>>(USERS_TABLE)
                .withKeySerde(Serdes.Long())
                .withValueSerde(userSerde)
        )
    }

    @Bean
    fun userPetKTable(@Qualifier(DEFAULT_STREAM_BEAN) streamsBuilder: StreamsBuilder): KTable<Long, UserPet>? {
        val petsTopicStream = streamsBuilder.stream(PETS_TOPIC, Consumed.with(Serdes.Long(), petSerde))
        val userPetsKTable = petsTopicStream
            .groupByKey()
            .aggregate(
                UserPet.newBuilder()::build,
                { userId, pet, userPet: UserPet ->
                    userPet.id = userId
                    userPet.pets?.add(pet)
                    userPet
                },
                Materialized.`as`<Long, UserPet, KeyValueStore<Bytes, ByteArray>>(USER_PETS_AGGR_TABLE)
                    .withKeySerde(Serdes.Long())
                    .withValueSerde(userPetSerde)
            )
        userPetsKTable.toStream().to(USER_PETS_AGGR_TOPIC, Produced.with(Serdes.Long(), userPetSerde))
        return userPetsKTable
    }

    @Bean
    fun userPetsStreamProcess(
        @Qualifier(DEFAULT_STREAM_BEAN) streamsBuilder: StreamsBuilder,
        user: GlobalKTable<Long, User>
    ): KStream<Long, UserPet> {
        val userPetStream: KStream<Long, UserPet> =
            streamsBuilder.stream(USER_PETS_AGGR_TOPIC, Consumed.with(Serdes.Long(), userPetSerde))
                .leftJoin(user,
                    { userId, _ -> userId },
                    { userPets, userEntry ->
                        userPets.firstName = userEntry.firstName
                        userPets.lastName = userEntry.lastName
                        userPets.title = userEntry.title
                        userPets.version = userEntry.version
                        userPets.timestamp = userEntry.timestamp
                        userPets
                    }
                )
        userPetStream.to(USER_PETS_TOPIC, Produced.with(Serdes.Long(), userPetSerde))
        return userPetStream
    }

    fun getUserById(id: Long): User {
        return userView.get(id)
    }

}