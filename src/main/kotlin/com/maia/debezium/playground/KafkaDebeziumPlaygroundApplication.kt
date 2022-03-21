package com.maia.debezium.playground

import com.maia.debezium.playground.config.KafkaProps
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(KafkaProps::class)
@SpringBootApplication
class KafkaDebeziumPlaygroundApplication

fun main(args: Array<String>) {
	runApplication<KafkaDebeziumPlaygroundApplication>(*args)
}
