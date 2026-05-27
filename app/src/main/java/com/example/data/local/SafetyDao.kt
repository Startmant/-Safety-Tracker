package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SafetyDao {
    // Emergency Contacts
    @Query("SELECT * FROM emergency_contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<EmergencyContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContact)

    @Update
    suspend fun updateContact(contact: EmergencyContact)

    @Delete
    suspend fun deleteContact(contact: EmergencyContact)

    @Query("SELECT * FROM emergency_contacts WHERE isPrimary = 1 LIMIT 1")
    suspend fun getPrimaryContact(): EmergencyContact?

    // Panic Records (Audio/Video evidence)
    @Query("SELECT * FROM panic_records ORDER BY timestamp DESC")
    fun getAllPanicRecords(): Flow<List<PanicRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPanicRecord(record: PanicRecord)

    @Query("DELETE FROM panic_records WHERE id = :id")
    suspend fun deletePanicRecordById(id: Int)

    // Trigger Events (SOS logs)
    @Query("SELECT * FROM trigger_events ORDER BY timestamp DESC")
    fun getAllTriggerEvents(): Flow<List<TriggerEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTriggerEvent(event: TriggerEvent)

    @Query("DELETE FROM trigger_events")
    suspend fun clearAllTriggerEvents()
}
