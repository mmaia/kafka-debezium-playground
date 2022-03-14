package com.maia.debezium.playground.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "kafka")
class KafkaProps (
    val schemaRegistryUrl: String,
    val bootstrapServers: List<String>,
    val stateDir: String
)