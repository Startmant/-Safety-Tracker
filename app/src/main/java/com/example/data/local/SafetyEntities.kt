package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val relation: String,
    val isPrimary: Boolean = false
)

@Entity(tableName = "panic_records")
data class PanicRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val filePath: String,
    val type: String, // "AUDIO" or "VIDEO"
    val durationMs: Long,
    val locationText: String
)

@Entity(tableName = "trigger_events")
data class TriggerEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "SOS", "PANIC", "TRACKING"
    val latitude: Double,
    val longitude: Double,
    val status: String, // "SOS Sent", "Authorities Alerted", "Recording Saved"
    val details: String
)
