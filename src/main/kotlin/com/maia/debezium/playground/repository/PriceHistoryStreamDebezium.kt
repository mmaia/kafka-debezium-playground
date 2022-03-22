package com.maia.debezium.playground.repository

import com.maia.debezium.playground.config.KafkaProps
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import javax.annotation.PostConstruct

class PriceHistoryStreamDebezium(val kafkaProps: KafkaProps) {

    private val phValueSerde = SpecificAvroSerde<kafka_connect_studies.kafka_connect_studies.price_history.Envelope>()
    private val phKeySerde = SpecificAvroSerde<kafka_connect_studies.kafka_connect_studies.price_history.Key>()

    val serdeConfig: MutableMap<String, String> = hashMapOf(
        AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to this.kafkaProps.schemaRegistryUrl
    )

    @PostConstruct
    fun init() {
        phKeySerde.configure(serdeConfig, true)
        phValueSerde.configure(serdeConfig, false)
    }



}