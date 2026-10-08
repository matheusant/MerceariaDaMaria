package com.heracles.troco.data.local

import androidx.room.TypeConverter
import com.heracles.troco.domain.model.ProductStatus

class Converters {

    @TypeConverter
    fun fromProductStatus(value: ProductStatus): String {
        return value.label
    }

    @TypeConverter
    fun toProductStatus(value: String): ProductStatus {
        return ProductStatus.fromLabel(value)
    }
}