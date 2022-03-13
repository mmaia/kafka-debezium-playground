package com.maia.debezium.playground

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaDebeziumPlaygroundApplication

fun main(args: Array<String>) {
	runApplication<KafkaDebeziumPlaygroundApplication>(*args)
}
