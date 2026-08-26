package com.heracles.troco.data.remote

import com.heracles.troco.domain.model.User
import org.junit.Test
import org.junit.Assert.assertEquals

class FirestoreUserMapperTest {

    private val mapper = FirestoreUserMapper()

    private fun userValidFromFirestore() = mapOf<String, Any>(
        "name" to "Matheus",
        "lastname" to "Santos",
        "phone" to "11949827165"
    )

    private fun userValidFromApp() = User(
        name = "Matheus",
        lastName = "Santos",
        phone = "11949827165"
    )

    @Test
    fun `documento valido vira User com dados completos`() {
        val config = mapper.toUser(userValidFromFirestore())

        assertEquals(userValidFromApp(), config)
    }

    @Test
    fun `User valido vira json para Firestore`() {
        val config = mapper.fromUser(userValidFromApp())

        assertEquals(userValidFromFirestore(), config)
    }

    @Test
    fun `toUser deve usar valores padrao quando campos estiverem ausentes no Firestore`() {
        val rawDataMissingFields = mapOf<String, Any>(
            "name" to "Matheus"
            // "lastname" e "phone" estão ausentes
        )

        val result = mapper.toUser(rawDataMissingFields)

        val expectedUser = User(
            name = "Matheus",
            lastName = "", // assumiu o fallback
            phone = ""     // assumiu o fallback
        )

        assertEquals(expectedUser, result)
    }

    @Test
    fun `toUser deve usar valores padrao quando o tipo do dado estiver incorreto`() {
        val rawDataWrongTypes = mapOf<String, Any>(
            "name" to "Matheus",
            "lastname" to "Santos",
            "phone" to 11949827165L // Long em vez de String
        )

        val result = mapper.toUser(rawDataWrongTypes)

        val expectedUser = User(
            name = "Matheus",
            lastName = "Santos",
            phone = "" // ignorou o Long com segurança e aplicou o fallback
        )

        assertEquals(expectedUser, result)
    }
}