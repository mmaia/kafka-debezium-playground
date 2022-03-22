package com.maia.debezium.playground

import java.text.SimpleDateFormat
import java.util.*

class Utils {}

fun isoDateToEpochMilli(isoDate: String): Long? {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val date: Date? = try {
        format.parse(isoDate)
    } catch (e: Exception) {
        null
    }
    return date?.toInstant()?.toEpochMilli()
}