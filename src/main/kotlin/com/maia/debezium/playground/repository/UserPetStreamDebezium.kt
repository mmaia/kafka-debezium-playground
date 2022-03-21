package com.maia.debezium.playground.repository

import com.maia.debezium.playground.Pet
import com.maia.debezium.playground.User
import com.maia.debezium.playground.config.*
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import kafka_connect_studies.kafka_connect_studies.pets.Envelope
import kafka_connect_studies.kafka_connect_studies.pets.Key
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Repository
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.BiFunction
import javax.annotation.PostConstruct

@Repository
class UserPetStreamDebezium(val kafkaProps: KafkaProps) {

    private val usersValueSerde = SpecificAvroSerde<kafka_connect_studies.kafka_connect_studies.users.Envelope>()
    private val usersKeySerde = SpecificAvroSerde<kafka_connect_studies.kafka_connect_studies.users.Key>()
    private val userSerde = SpecificAvroSerde<User>()

    private val petsValueSerde = SpecificAvroSerde<kafka_connect_studies.kafka_connect_studies.pets.Envelope>()
    private val petsKeySerde = SpecificAvroSerde<kafka_connect_studies.kafka_connect_studies.pets.Key>()
    private val petSerde = SpecificAvroSerde<Pet>()


    val serdeConfig: MutableMap<String, String> = hashMapOf(
        AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to this.kafkaProps.schemaRegistryUrl
    )

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

    @PostConstruct
    fun init() {

        petsKeySerde.configure(serdeConfig, true)
        petsValueSerde.configure(serdeConfig, false)
        petSerde.configure(serdeConfig, false)

        usersKeySerde.configure(serdeConfig, true)
        usersValueSerde.configure(serdeConfig, false)
        userSerde.configure(serdeConfig, false)
    }

    @Bean
    fun userStreamDebToUser(@Qualifier(DEB_STREAM_BEAN) streamsBuilder: StreamsBuilder): KStream<kafka_connect_studies.kafka_connect_studies.users.Key, kafka_connect_studies.kafka_connect_studies.users.Envelope> {
        val userDebStream = streamsBuilder.stream(DEB_USERS_TOPIC, Consumed.with(usersKeySerde, usersValueSerde))
        userDebStream.map(userMapper::apply).to(USERS_TOPIC, Produced.with(Serdes.Long(), userSerde))
        return userDebStream
    }

    @Bean
    fun petsStreamDebToPets(
        @Qualifier(DEB_STREAM_BEAN) streamsBuilder: StreamsBuilder): KStream<Long, Pet>? {
        val stream: KStream<Key, Envelope> =
            streamsBuilder.stream(DEB_PETS_TOPIC, Consumed.with(petsKeySerde, petsValueSerde))

        val resStream: KStream<Long, Pet> =
            stream
                .map(petMapper::apply)

        resStream.to(PETS_TOPIC, Produced.with(Serdes.Long(), petSerde))

        return resStream
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

}