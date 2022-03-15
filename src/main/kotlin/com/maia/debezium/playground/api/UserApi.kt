package com.maia.debezium.playground.api

import com.maia.debezium.playground.api.dto.UserDto
import com.maia.debezium.playground.repository.UserPetStream
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/api/user")
class UserApi(private val userPetStream: UserPetStream) {

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long) {
        val envelope = userPetStream.getUserById(id)
        var user = envelope?.after
        val uDto = UserDto(user!!.id, user!!.firstName.toString(), user.lastName.toString(), user.title.toString(),  user.version)
        println(uDto)
    }

}