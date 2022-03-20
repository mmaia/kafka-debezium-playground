package com.maia.debezium.playground.api.dto

data class UserPetDto (val user: UserDto, val pets: List<PetDto>)