package com.heracles.troco.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.heracles.troco.data.local.dao.ProductDao
import com.heracles.troco.data.local.entity.ProductEntity

@TypeConverters(Converters::class)
@Database(
    entities = [
        ProductEntity::class
    ],
    version = 1
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun productDao(): ProductDao
}