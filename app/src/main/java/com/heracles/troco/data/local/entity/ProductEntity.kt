package com.heracles.troco.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.heracles.troco.domain.model.ProductStatus
import java.util.UUID

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: String,
    val quantity: Int,
    val status: ProductStatus
)