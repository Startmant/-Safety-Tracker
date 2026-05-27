package com.example.data.repository

import com.example.data.local.EmergencyContact
import com.example.data.local.PanicRecord
import com.example.data.local.SafetyDao
import com.example.data.local.TriggerEvent
import kotlinx.coroutines.flow.Flow

class SafetyRepository(private val safetyDao: SafetyDao) {

    val allContacts: Flow<List<EmergencyContact>> = safetyDao.getAllContacts()
    val allPanicRecords: Flow<List<PanicRecord>> = safetyDao.getAllPanicRecords()
    val allTriggerEvents: Flow<List<TriggerEvent>> = safetyDao.getAllTriggerEvents()

    suspend fun insertContact(contact: EmergencyContact) {
        safetyDao.insertContact(contact)
    }

    suspend fun updateContact(contact: EmergencyContact) {
        safetyDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: EmergencyContact) {
        safetyDao.deleteContact(contact)
    }

    suspend fun getPrimaryContact(): EmergencyContact? {
        return safetyDao.getPrimaryContact()
    }

    suspend fun insertPanicRecord(record: PanicRecord) {
        safetyDao.insertPanicRecord(record)
    }

    suspend fun deletePanicRecordById(id: Int) {
        safetyDao.deletePanicRecordById(id)
    }

    suspend fun insertTriggerEvent(event: TriggerEvent) {
        safetyDao.insertTriggerEvent(event)
    }

    suspend fun clearAllTriggerEvents() {
        safetyDao.clearAllTriggerEvents()
    }
}
