package com.maia.debezium.playground.repository

import com.maia.debezium.playground.Pet
import com.maia.debezium.playground.User
import com.maia.debezium.playground.UserPet
import com.maia.debezium.playground.config.*
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import kafka_connect_studies.kafka_connect_studies.pets.Envelope
import kafka_connect_studies.kafka_connect_studies.pets.Key
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
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
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.BiFunction
import javax.annotation.PostConstruct

@Repository
class UserPetStream(kafkaProps: KafkaProps) {

    private val usersValueSerde = SpecificAvroSerde<kafka_connect_studies.kafka_connect_studies.users.Envelope>()
    private val usersKeySerde = SpecificAvroSerde<kafka_connect_studies.kafka_connect_studies.users.Key>()
    private val userSerde = SpecificAvroSerde<User>()

    private lateinit var userView: ReadOnlyKeyValueStore<Long, User>

    private val petsValueSerde = SpecificAvroSerde<kafka_connect_studies.kafka_connect_studies.pets.Envelope>()
    private val petsKeySerde = SpecificAvroSerde<kafka_connect_studies.kafka_connect_studies.pets.Key>()

    private val userPetSerde = SpecificAvroSerde<UserPet>()

    val serdeConfig: MutableMap<String, String> = hashMapOf(
        AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to kafkaProps.schemaRegistryUrl
    )

    @PostConstruct
    fun init() {
        userPetSerde.configure(serdeConfig, false)

        petsKeySerde.configure(serdeConfig, true)
        petsValueSerde.configure(serdeConfig, false)

        usersKeySerde.configure(serdeConfig, true)
        usersValueSerde.configure(serdeConfig, false)
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

    val userMapper: BiFunction<kafka_connect_studies.kafka_connect_studies.users.Key, kafka_connect_studies.kafka_connect_studies.users.Envelope, KeyValue<Long, User>> =
        BiFunction<kafka_connect_studies.kafka_connect_studies.users.Key, kafka_connect_studies.kafka_connect_studies.users.Envelope, KeyValue<Long, User>>
        { key, envelope ->
            val user = User(
                key.id,
                envelope.after.firstName,
                envelope.after.lastName,
                parseDate(envelope.after.timestamp),
                envelope.after.title,
                envelope.after.version
            )
            KeyValue<Long, User>(user.id, user)
        }

    val petMapper: BiFunction<kafka_connect_studies.kafka_connect_studies.pets.Key, kafka_connect_studies.kafka_connect_studies.pets.Envelope, KeyValue<Long, Pet>> =
        BiFunction<kafka_connect_studies.kafka_connect_studies.pets.Key, kafka_connect_studies.kafka_connect_studies.pets.Envelope, KeyValue<Long, Pet>>
        { key, envelope ->
            val pet = Pet(
                key.id,
                envelope.after.name,
                envelope.after.breed,
                envelope.after.userId
            )
            KeyValue<Long, Pet>(pet.userId, pet)
        }

    fun parseDate(isoDate: String): Long? {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val date: Date? = try {
            format.parse(isoDate)
        } catch (e: Exception) {
            null
        }
        return date?.toInstant()?.toEpochMilli()
    }

    @Bean
    fun userDebStream(@Qualifier(DEFAULT_STREAM_BEAN) streamsBuilder: StreamsBuilder): KStream<kafka_connect_studies.kafka_connect_studies.users.Key, kafka_connect_studies.kafka_connect_studies.users.Envelope> {
        val userDebStream = streamsBuilder.stream(DEB_USERS_TOPIC, Consumed.with(usersKeySerde, usersValueSerde))
        userDebStream.map(userMapper::apply).to(USERS_TOPIC, Produced.with(Serdes.Long(), userSerde))
        return userDebStream
    }

    @Bean
    fun userByIdGKTable(@Qualifier(DEFAULT_STREAM_BEAN) streamsBuilder: StreamsBuilder): GlobalKTable<Long, User> {
        return streamsBuilder.globalTable(
            USERS_TOPIC, Materialized.`as`<Long, User, KeyValueStore<Bytes, ByteArray>>(USERS_TABLE)
                .withKeySerde(Serdes.Long())
                .withValueSerde(userSerde)
        )
    }

    fun getUserById(id: Long): User {
        return userView.get(id)
    }

    @Bean
    fun petsStream(
        @Qualifier(DEFAULT_STREAM_BEAN) streamsBuilder: StreamsBuilder,
        userByIdGKTable: GlobalKTable<Long, User>
    ): KStream<Long, UserPet>? {

        val stream: KStream<Key, Envelope> =
            streamsBuilder.stream(DEB_PETS_TOPIC, Consumed.with(petsKeySerde, petsValueSerde))

        val resStream: KStream<Long, UserPet> =
            stream
                .map(petMapper::apply)
                .leftJoin(userByIdGKTable,
                    { _, pet -> pet.userId },
                    fun(pet, user): UserPet {
                        val uPet =
                            UserPet(user?.firstName, user?.lastName, user?.timestamp, user?.title, user?.version, null)
                        if(pet != null) {
                            val pet = Pet(pet?.id, pet?.name, pet?.breed, pet?.userId)
                            uPet.pets = ArrayList<Pet>()
                            uPet.pets.add(pet)
                        }
                        return uPet
                    }
                )

        resStream.to(USER_PETS_TOPIC, Produced.with(Serdes.Long(), userPetSerde))

        return resStream
    }

}