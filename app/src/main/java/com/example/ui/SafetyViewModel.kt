package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.SafetyBackendClient
import com.example.audio.AudioSirenManager
import com.example.data.local.EmergencyContact
import com.example.data.local.PanicRecord
import com.example.data.local.TriggerEvent
import com.example.data.repository.SafetyRepository
import com.example.media.PanicMediaRecorder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SafetyViewModel(
    application: Application,
    private val repository: SafetyRepository
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sirenManager = AudioSirenManager()
    private val mediaRecorder = PanicMediaRecorder(context)
    private val backendClient = SafetyBackendClient()

    // ----------------------------------------------------
    // Database Reactive Flows
    // ----------------------------------------------------
    val contacts: StateFlow<List<EmergencyContact>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val panicRecords: StateFlow<List<PanicRecord>> = repository.allPanicRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val triggerEvents: StateFlow<List<TriggerEvent>> = repository.allTriggerEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ----------------------------------------------------
    // UI Transient States
    // ----------------------------------------------------
    private val _isSosActive = MutableStateFlow(false)
    val isSosActive: StateFlow<Boolean> = _isSosActive.asStateFlow()

    private val _isLocationSharing = MutableStateFlow(false)
    val isLocationSharing: StateFlow<Boolean> = _isLocationSharing.asStateFlow()

    private val _latitude = MutableStateFlow(37.7749) // Default Downtown Base
    val latitude: StateFlow<Double> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow(-122.4194)
    val longitude: StateFlow<Double> = _longitude.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingType = MutableStateFlow<String?>(null) // "AUDIO" or "VIDEO"
    val recordingType: StateFlow<String?> = _recordingType.asStateFlow()

    private val _playbackId = MutableStateFlow<Int?>(null)
    val playbackId: StateFlow<Int?> = _playbackId.asStateFlow()

    private val _authorityNotificationStatus = MutableStateFlow<String>("IDLE - Monitoring")
    val authorityNotificationStatus: StateFlow<String> = _authorityNotificationStatus.asStateFlow()

    private val _assignedCaseId = MutableStateFlow<String?>(null)
    val assignedCaseId: StateFlow<String?> = _assignedCaseId.asStateFlow()

    private val _assignedOfficer = MutableStateFlow<String?>(null)
    val assignedOfficer: StateFlow<String?> = _assignedOfficer.asStateFlow()

    private var locationJob: Job? = null
    private var activeRecordFilePath: String? = null

    init {
        // Pre-populate database with helpful mock contacts if empty
        viewModelScope.launch {
            delay(100)
            contacts.value.let {
                if (it.isEmpty()) {
                    repository.insertContact(
                        EmergencyContact(
                            name = "National Helpline",
                            phoneNumber = "911",
                            relation = "Official Authorities",
                            isPrimary = true
                        )
                    )
                    repository.insertContact(
                        EmergencyContact(
                            name = "Mom",
                            phoneNumber = "+15550199",
                            relation = "Family",
                            isPrimary = false
                        )
                    )
                    repository.insertContact(
                        EmergencyContact(
                            name = "Best Friend",
                            phoneNumber = "+15550188",
                            relation = "Friend",
                            isPrimary = false
                        )
                    )
                }
            }
        }
    }

    // ----------------------------------------------------
    // Location Management & Real-Time Sharing
    // ----------------------------------------------------
    fun toggleLocationSharing() {
        val newState = !_isLocationSharing.value
        _isLocationSharing.value = newState
        
        if (newState) {
            startLiveLocationBroadcast()
            viewModelScope.launch {
                repository.insertTriggerEvent(
                    TriggerEvent(
                        type = "TRACKING",
                        latitude = _latitude.value,
                        longitude = _longitude.value,
                        status = "Live sharing enabled",
                        details = "Broadcasted active location share link to security grid."
                    )
                )
                Toast.makeText(context, "Real-time location sharing ACTIVE", Toast.LENGTH_SHORT).show()
            }
        } else {
            stopLiveLocationBroadcast()
            viewModelScope.launch {
                repository.insertTriggerEvent(
                    TriggerEvent(
                        type = "TRACKING",
                        latitude = _latitude.value,
                        longitude = _longitude.value,
                        status = "Live sharing paused",
                        details = "Security broadcast paused by user."
                    )
                )
                Toast.makeText(context, "Location sharing suspended", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startLiveLocationBroadcast() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            while (_isLocationSharing.value) {
                // Simulate slow continuous movement (walking speed)
                val noiseLat = (Math.random() - 0.5) * 0.0003
                val noiseLng = (Math.random() - 0.5) * 0.0003
                _latitude.value += noiseLat
                _longitude.value += noiseLng

                // Send live coordinates sync to simulated security server backend
                try {
                    val response = backendClient.sendLiveLocation(
                        authToken = "SESSION_SECURE_TOKEN",
                        latitude = _latitude.value,
                        longitude = _longitude.value
                    )
                    Log.d("SafetyViewModel", "Server Synced: ${response.message}")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                delay(3000) // update frequency
            }
        }
    }

    private fun stopLiveLocationBroadcast() {
        locationJob?.cancel()
        locationJob = null
    }

    // ----------------------------------------------------
    // SOS Alert Button Activation
    // ----------------------------------------------------
    fun triggerSosAlert() {
        val isFirstActivation = !_isSosActive.value
        _isSosActive.value = isFirstActivation

        if (isFirstActivation) {
            // 1. Play wailing physical audio siren
            sirenManager.startSiren(viewModelScope)

            // 2. Transmit Rest SOS Alerts to Authorities/Police Backend
            _authorityNotificationStatus.value = "CONNECTING to Police Dispatcher..."
            viewModelScope.launch {
                try {
                    val primaryContact = repository.getPrimaryContact()
                    val response = backendClient.sendSosAlert(
                        latitude = _latitude.value,
                        longitude = _longitude.value,
                        primaryContact = primaryContact?.name ?: "No Primary Contact Set"
                    )
                    
                    _authorityNotificationStatus.value = "ALERT TRANSMITTED: Patrolling cruiser guided!"
                    _assignedCaseId.value = response.caseId
                    _assignedOfficer.value = response.assignedDispatcher

                    // Log in database
                    repository.insertTriggerEvent(
                        TriggerEvent(
                            type = "SOS",
                            latitude = _latitude.value,
                            longitude = _longitude.value,
                            status = "SOS Sent - Dispatch Complete",
                            details = "Sent live location. Case: ${response.caseId}. Commander: ${response.assignedDispatcher}. ${response.responseStatus}"
                        )
                    )

                    // 3. Simulate SMS broadcasting
                    var smsLoggedMsg = "DURGENT! SOS: I'm in danger. Track my coordinates: https://maps.google.com/?q=${_latitude.value},${_longitude.value}"
                    contacts.value.filter { it.phoneNumber != "911" }.forEach { contact ->
                        // In actual application, use SmsManager:
                        // val sms = SmsManager.getDefault()
                        // sms.sendTextMessage(contact.phoneNumber, null, smsLoggedMsg, null, null)
                        Log.d("SafetySOS", "Sending Panic SMS to ${contact.name} at ${contact.phoneNumber}")
                    }
                    Toast.makeText(context, "SOS Alerts broadcasted immediately!", Toast.LENGTH_LONG).show()

                } catch (e: Exception) {
                    _authorityNotificationStatus.value = "Offline failover active."
                }
            }
        } else {
            // Cancel and disable Siren
            sirenManager.stopSiren()
            _authorityNotificationStatus.value = "IDLE - Patrol stood down."
            _assignedCaseId.value = null
            _assignedOfficer.value = null

            viewModelScope.launch {
                repository.insertTriggerEvent(
                    TriggerEvent(
                        type = "SOS",
                        latitude = _latitude.value,
                        longitude = _longitude.value,
                        status = "SOS Suspended",
                        details = "Distress status deactivated by user safely."
                    )
                )
            }
        }
    }

    // ----------------------------------------------------
    // Panic Recording (Automatic Cam/Mic Action)
    // ----------------------------------------------------
    fun togglePanicRecording(type: String) {
        if (_isRecording.value) {
            // Stop
            val duration = mediaRecorder.stopRecording()
            _isRecording.value = false
            val finalizedType = _recordingType.value ?: "AUDIO"
            _recordingType.value = null

            val filePath = activeRecordFilePath ?: ""
            activeRecordFilePath = null

            val locString = "${String.format(Locale.getDefault(), "%.4f", _latitude.value)}, ${String.format(Locale.getDefault(), "%.4f", _longitude.value)}"

            viewModelScope.launch {
                repository.insertPanicRecord(
                    PanicRecord(
                        filePath = filePath,
                        type = finalizedType,
                        durationMs = duration,
                        locationText = "Near coordinate $locString"
                    )
                )

                repository.insertTriggerEvent(
                    TriggerEvent(
                        type = "PANIC",
                        latitude = _latitude.value,
                        longitude = _longitude.value,
                        status = "Evidence saved locally",
                        details = "Completed $finalizedType recording of physical context."
                    )
                )
                Toast.makeText(context, "$finalizedType evidence saved securely", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Start
            _isRecording.value = true
            _recordingType.value = type
            
            viewModelScope.launch {
                activeRecordFilePath = mediaRecorder.startRecording(type)
                repository.insertTriggerEvent(
                    TriggerEvent(
                        type = "PANIC",
                        latitude = _latitude.value,
                        longitude = _longitude.value,
                        status = "Silent recording initiated",
                        details = "Activated $type automatic recording sensor."
                    )
                )
                Toast.makeText(context, "Recording emergency $type...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ----------------------------------------------------
    // Sound Playback
    // ----------------------------------------------------
    fun togglePlayRecord(record: PanicRecord) {
        if (_playbackId.value == record.id) {
            mediaRecorder.stopPlayback()
            _playbackId.value = null
        } else {
            _playbackId.value = record.id
            mediaRecorder.playRecord(record.filePath) {
                _playbackId.value = null
            }
        }
    }

    // ----------------------------------------------------
    // Contacts Actions
    // ----------------------------------------------------
    fun addContact(name: String, phone: String, relation: String, isPrimary: Boolean) {
        viewModelScope.launch {
            val contact = EmergencyContact(
                name = name,
                phoneNumber = phone,
                relation = relation,
                isPrimary = isPrimary
            )
            repository.insertContact(contact)
            Toast.makeText(context, "Contact added successfully", Toast.LENGTH_SHORT).show()
        }
    }

    fun makePrimary(contact: EmergencyContact) {
        viewModelScope.launch {
            // Clear existing primary first
            contacts.value.forEach {
                if (it.isPrimary) {
                    repository.updateContact(it.copy(isPrimary = false))
                }
            }
            repository.updateContact(contact.copy(isPrimary = true))
            Toast.makeText(context, "${contact.name} is now primary responder", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeContact(contact: EmergencyContact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
            Toast.makeText(context, "Contact removed", Toast.LENGTH_SHORT).show()
        }
    }

    fun deletePanicRecord(record: PanicRecord) {
        viewModelScope.launch {
            repository.deletePanicRecordById(record.id)
            val file = File(record.filePath)
            if (file.exists()) {
                file.delete()
            }
            Toast.makeText(context, "Evidence record purged", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearAllTriggerEvents()
            Toast.makeText(context, "Audit logs cleared", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        sirenManager.stopSiren()
        mediaRecorder.stopPlayback()
        locationJob?.cancel()
    }
}

// Factory configuration for Room architecture injection
class SafetyViewModelFactory(
    private val application: Application,
    private val repository: SafetyRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SafetyViewModel::class.java)) {
            return SafetyViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
