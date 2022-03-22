package com.maia.debezium.playground.repository

import com.maia.debezium.playground.avro.PriceHistory
import com.maia.debezium.playground.config.DEB_PRICE_HISTORY_TOPIC
import com.maia.debezium.playground.config.KafkaProps
import com.maia.debezium.playground.config.PRICE_HISTORY_STREAM_BEAN
import com.maia.debezium.playground.config.PRICE_HIST_AGGR_TOPIC
import com.maia.debezium.playground.isoDateToEpochMilli
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import kafka_connect_studies.kafka_connect_studies.price_history.Envelope
import kafka_connect_studies.kafka_connect_studies.price_history.Key
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Repository
import java.time.Duration
import java.util.function.BiFunction
import javax.annotation.PostConstruct

@Repository
class PriceHistoryStreamDebezium(private val kafkaProps: KafkaProps) {

    private val phValueSerde = SpecificAvroSerde<kafka_connect_studies.kafka_connect_studies.price_history.Envelope>()
    private val phKeySerde = SpecificAvroSerde<kafka_connect_studies.kafka_connect_studies.price_history.Key>()

    private val phSerde = SpecificAvroSerde<PriceHistory>()

    val serdeConfig: MutableMap<String, String> = hashMapOf(
        AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to this.kafkaProps.schemaRegistryUrl
    )

//    val priceHistoryMapper: BiFunction<kafka_connect_studies.kafka_connect_studies.price_history.Key, kafka_connect_studies.kafka_connect_studies.price_history.Envelope, KeyValue<String, PriceHistory>> =
//        BiFunction<kafka_connect_studies.kafka_connect_studies.price_history.Key, kafka_connect_studies.kafka_connect_studies.price_history.Envelope, KeyValue<String, PriceHistory>> {
//            key, envelope ->
//            val priceHistory = PriceHistory(
//                key.id,
//                envelope.after.asset,
//                envelope.after.price,
//                isoDateToEpochMilli(envelope.after.dateTime)
//            )
//            KeyValue<String, PriceHistory>(priceHistory.asset, priceHistory)
//        }

    @PostConstruct
    fun init() {
        phKeySerde.configure(serdeConfig, true)
        phValueSerde.configure(serdeConfig, false)
        phSerde.configure(serdeConfig, false)
    }

    @Bean
    fun priceHistoryStreamProcessor(@Qualifier(PRICE_HISTORY_STREAM_BEAN) streamsBuilder: StreamsBuilder): KStream<String, PriceHistory>? {
        val debStream: KStream<Key, Envelope> =
            streamsBuilder.stream(DEB_PRICE_HISTORY_TOPIC, Consumed.with(phKeySerde, phValueSerde))

        val to = debStream.groupByKey()
            .windowedBy(SlidingWindows.ofTimeDifferenceWithNoGrace(Duration.ofHours(1)))
            .aggregate(
                PriceHistory.newBuilder()::build,
                { assetKey: Key, ph: Envelope, priceHistory: PriceHistory ->
                    priceHistory
                },
                Materialized.with(Serdes.String(), phSerde)
            ).toStream()
            .map { key: Windowed<String>, ph: PriceHistory ->
                KeyValue<String, PriceHistory>(key.key(), ph)
            }
            .to(PRICE_HIST_AGGR_TOPIC, Produced.with(Serdes.String(), phSerde))

//        val phStream: KStream<String, PriceHistory> =
//            debStream.map(priceHistoryMapper::apply)

//        phStream.groupByKey()
//            .windowedBy(SlidingWindows.ofTimeDifferenceAndGrace(Duration.ofHours(1),
//                Duration.ofMinutes(10)))
//            .aggregate(
//                PriceHistory.newBuilder()::build,
//                {  assetKey, ph, priceHistory: PriceHistory ->
//                    priceHistory.asset = assetKey
//                    priceHistory.id = ph.id
//                    priceHistory.price = ph.price
//                    priceHistory
//                },
//                Materialized.with(Serdes.String(), phSerde)).toStream()
//            .map { key: Windowed<String>, ph: PriceHistory ->
//                KeyValue<String, PriceHistory>(key.key(), ph)
//            }
//            .to(PRICE_HIST_AGGR_TOPIC, Produced.with(Serdes.String(), phSerde))

        return null
    }


}