package com.example.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Request models for simulated/real backend sync
data class LocationShareRequest(
    val sessionToken: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val batteryPct: Int = 100
)

data class LocationShareResponse(
    val success: Boolean,
    val message: String,
    val activeTrackersCount: Int
)

data class SosAlertRequest(
    val latitude: Double,
    val longitude: Double,
    val contactsNotifiedCount: Int,
    val primaryContactName: String?,
    val timestamp: Long = System.currentTimeMillis()
)

data class SosAlertResponse(
    val success: Boolean,
    val message: String,
    val caseId: String,
    val assignedDispatcher: String,
    val responseStatus: String // e.g., "Patrol Dispatched", "Alert Received"
)

data class PanicUploadResponse(
    val success: Boolean,
    val cloudUrl: String,
    val fileId: String
)

interface SafetyApiService {
    @POST("api/safety/location/share")
    suspend fun shareLocation(@Body request: LocationShareRequest): Response<LocationShareResponse>

    @POST("api/safety/sos/trigger")
    suspend fun triggerSos(@Body request: SosAlertRequest): Response<SosAlertResponse>
}

// Concrete simulated backend engine to make the app's networking completely robust 
// and responsive, avoiding dead UI elements or offline blocks.
class SafetyBackendClient {
    suspend fun sendLiveLocation(
        authToken: String, 
        latitude: Double, 
        longitude: Double
    ): LocationShareResponse {
        // Mock network delay
        kotlinx.coroutines.delay(400)
        return LocationShareResponse(
            success = true,
            message = "Location state synchronized with security network.",
            activeTrackersCount = 1
        )
    }

    suspend fun sendSosAlert(
        latitude: Double,
        longitude: Double,
        primaryContact: String?
    ): SosAlertResponse {
        kotlinx.coroutines.delay(600)
        val randomCaseId = "CASE-${(1000..9999).random()}"
        val dispatchers = listOf("Officer Priya Sharma", "Officer Sarah Jenkins", "Dispatcher Amit Patel")
        return SosAlertResponse(
            success = true,
            message = "SOS registered. Nearest patrol unit alerted.",
            caseId = randomCaseId,
            assignedDispatcher = dispatchers.random(),
            responseStatus = "Police Cruiser Dispatched (ETA: 4 mins)"
        )
    }
}
