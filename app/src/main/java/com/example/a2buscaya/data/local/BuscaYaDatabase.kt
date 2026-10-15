package com.example.a2buscaya.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ComercioEntity::class, FarmaciaEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class BuscaYaDatabase : RoomDatabase() {
    abstract fun comercioDao(): ComercioDao
    abstract fun farmaciaDao(): FarmaciaDao

    companion object {
        @Volatile
        private var INSTANCE: BuscaYaDatabase? = null

        fun getInstance(context: Context): BuscaYaDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BuscaYaDatabase::class.java,
                    "buscaya_database",
                )
                    // Solo para desarrollo: elimina las tablas si cambia el esquema sin migración.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
