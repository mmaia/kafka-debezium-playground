package com.maia.debezium.playground.repository

import com.maia.debezium.playground.isoDateToEpochMilli
import kafka_connect_studies.kafka_connect_studies.price_history.Envelope
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.streams.processor.TimestampExtractor

class PriceHistoryTimestampExtractor: TimestampExtractor {
    @Override
    override fun extract(record: ConsumerRecord<Any, Any>, previousTimestamp: Long): Long {
        return isoDateToEpochMilli((record.value() as Envelope).after.dateTime) ?: previousTimestamp
    }
}