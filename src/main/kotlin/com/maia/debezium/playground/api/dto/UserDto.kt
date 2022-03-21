package com.maia.debezium.playground.api.dto


data class UserDto(val id: Long,
                   val firstName: String,
                   val lastName: String? = null,
                   val title: String? = null,
                   val version: Int? = null)