package com.heracles.troco.data.remote

import com.heracles.troco.domain.model.User
import javax.inject.Inject

class FirestoreUserMapper @Inject constructor() {
    fun toUser(data: Map<String, Any>): User {
        return User(
            name = (data.getOrDefault("name", "") as? String) ?: "",
            lastName = (data.getOrDefault("lastname", "") as? String) ?: "",
            phone = (data.getOrDefault("phone", "") as? String) ?: ""
        )
    }

    fun fromUser(user: User): Map<String, Any> {
        return mapOf(
            "name" to user.name,
            "lastname" to user.lastName,
            "phone" to user.phone
        )
    }
}