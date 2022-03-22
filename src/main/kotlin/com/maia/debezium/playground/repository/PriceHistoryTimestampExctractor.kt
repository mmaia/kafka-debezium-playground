package com.maia.debezium.playground.repository

import com.maia.debezium.playground.avro.PriceHistory
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.streams.processor.TimestampExtractor

class PriceHistoryTimestampExctractor: TimestampExtractor {
    @Override
    override fun extract(record: ConsumerRecord<Any, Any>, previousTimestamp: Long): Long {
        return (record.value() as PriceHistory).timestamp ?: previousTimestamp
    }
}