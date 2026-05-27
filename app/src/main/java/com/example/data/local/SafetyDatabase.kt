package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [EmergencyContact::class, PanicRecord::class, TriggerEvent::class],
    version = 1,
    exportSchema = false
)
abstract class SafetyDatabase : RoomDatabase() {
    abstract fun safetyDao(): SafetyDao

    companion object {
        @Volatile
        private var INSTANCE: SafetyDatabase? = null

        fun getDatabase(context: Context): SafetyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SafetyDatabase::class.java,
                    "safety_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
