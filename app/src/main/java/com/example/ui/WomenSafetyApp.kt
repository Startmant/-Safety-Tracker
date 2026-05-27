package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.EmergencyContact
import com.example.data.local.PanicRecord
import com.example.data.local.TriggerEvent
import com.example.ui.components.TrackerMapCanvas
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WomenSafetyApp(viewModel: SafetyViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    val isSosActive by viewModel.isSosActive.collectAsStateWithLifecycle()
    val isLocationSharing by viewModel.isLocationSharing.collectAsStateWithLifecycle()
    val recordingType by viewModel.recordingType.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Shield Logo",
                                tint = EmergencyRuby,
                                modifier = Modifier.size(28.dp).padding(end = 8.dp)
                            )
                            Text(
                                text = "AURORA SAFETY",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SoftWhite
                            )
                        }

                        // Top right rapid trigger status banner
                        if (isSosActive) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmergencyRuby.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRuby)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(EmergencyRuby)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SOS DISTRESS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmergencyRuby
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightBase,
                    titleContentColor = SoftWhite
                ),
                modifier = Modifier.testTag("app_top_bar")
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MidnightBase,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("app_bottom_nav").windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(imageVector = Icons.Default.Shield, contentDescription = "Distress Panel") },
                    label = { Text("SOS Trigger") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmergencyRuby,
                        selectedTextColor = EmergencyRuby,
                        indicatorColor = EmergencyRuby.copy(alpha = 0.15f),
                        unselectedIconColor = SoftGray,
                        unselectedTextColor = SoftGray
                    ),
                    modifier = Modifier.testTag("nav_tab_sos")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Emergency Responders") },
                    label = { Text("Responders") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SoftAqua,
                        selectedTextColor = SoftAqua,
                        indicatorColor = SoftAqua.copy(alpha = 0.15f),
                        unselectedIconColor = SoftGray,
                        unselectedTextColor = SoftGray
                    ),
                    modifier = Modifier.testTag("nav_tab_responders")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(imageVector = Icons.Default.History, contentDescription = "Evidence logs") },
                    label = { Text("Evidence Logs") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SecureEmerald,
                        selectedTextColor = SecureEmerald,
                        indicatorColor = SecureEmerald.copy(alpha = 0.15f),
                        unselectedIconColor = SoftGray,
                        unselectedTextColor = SoftGray
                    ),
                    modifier = Modifier.testTag("nav_tab_logs")
                )
            }
        },
        containerColor = MidnightBase
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> SosDashboardScreen(viewModel)
                1 -> RespondersScreen(viewModel)
                2 -> EvidenceLogsScreen(viewModel)
            }

            // Global hovering alert banner if automatic audio/video recording is active
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Red.copy(alpha = 0.9f), Color.Transparent)
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")
                        val scaleRec by infiniteTransition.animateFloat(
                            initialValue = 0.8f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(550, easing = EaseInOutSine),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse"
                        )
                        Box(
                            modifier = Modifier
                                .scale(scaleRec)
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SECURE EMERGENCY ${recordingType ?: "MEDIA"} STREAMING ACTIVE...",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 1: SOS TRIGGER & LIVE SECURITY TELEMETRY
// ----------------------------------------------------
@Composable
fun SosDashboardScreen(viewModel: SafetyViewModel) {
    val latitude by viewModel.latitude.collectAsStateWithLifecycle()
    val longitude by viewModel.longitude.collectAsStateWithLifecycle()
    val isSosActive by viewModel.isSosActive.collectAsStateWithLifecycle()
    val isLocationSharing by viewModel.isLocationSharing.collectAsStateWithLifecycle()
    val authorityStatus by viewModel.authorityNotificationStatus.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val recordingType by viewModel.recordingType.collectAsStateWithLifecycle()

    val caseId by viewModel.assignedCaseId.collectAsStateWithLifecycle()
    val officerName by viewModel.assignedOfficer.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("sos_dashboard_scroll"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Telemetry custom vector map widget
        item {
            Text(
                text = "Live Dispatch Telemetry Node",
                style = MaterialTheme.typography.titleSmall,
                color = SoftGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            TrackerMapCanvas(
                latitude = latitude,
                longitude = longitude,
                isSharingActive = isLocationSharing
            )
        }

        // Location sharing transmitter panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSlate)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Continuous Live GPS Broadcast",
                            style = MaterialTheme.typography.titleMedium,
                            color = SoftWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isLocationSharing) "Broadcasting real-time safety link to responders." else "Location sharing standby.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftGray
                        )
                    }
                    Switch(
                        checked = isLocationSharing,
                        onCheckedChange = { viewModel.toggleLocationSharing() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MidnightBase,
                            checkedTrackColor = SoftAqua,
                            uncheckedThumbColor = SoftGray,
                            uncheckedTrackColor = DepthSurface
                        ),
                        modifier = Modifier.testTag("location_sharing_switch")
                    )
                }
            }
        }

        // Gigantic animated primary SOS button core
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background radial glow effect under the SOS active state
                if (isSosActive) {
                    val infiniteTransition = rememberInfiniteTransition(label = "glow")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 1.9f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = EaseOutSine),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "pulse"
                    )
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = EaseOutSine),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "alpha"
                    )
                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(EmergencyRuby.copy(alpha = pulseAlpha))
                    )
                }

                Surface(
                    onClick = { viewModel.triggerSosAlert() },
                    shape = CircleShape,
                    color = if (isSosActive) EmergencyRubyHover else EmergencyRuby,
                    tonalElevation = 12.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(170.dp)
                        .testTag("sos_distress_button")
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (isSosActive) Icons.Default.Warning else Icons.Default.Shield,
                            contentDescription = "Trigger emergency",
                            tint = Color.White,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(bottom = 6.dp)
                        )
                        Text(
                            text = if (isSosActive) "SOS ACTIVE" else "TRIGGER SOS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (isSosActive) "Tap to Stand Down" else "Instant Alarm",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftWhite.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Authorities dispatch / Secure contact dispatch logs card
        item {
            AnimatedVisibility(
                visible = isSosActive || caseId != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DepthSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRuby.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().testTag("authorities_dispatch_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isSosActive) EmergencyRuby else SecureEmerald)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OFFICIAL HELPLINE DISPATCH GRID",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SoftWhite
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = authorityStatus,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSosActive) EmergencyRuby else SecureEmerald,
                            fontWeight = FontWeight.Bold
                        )

                        if (caseId != null) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = OutlineSlate)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("CASE REGISTRATION", style = MaterialTheme.typography.labelSmall, color = SoftGray)
                                    Text(caseId ?: "", style = MaterialTheme.typography.bodyMedium, color = SoftWhite, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("ASSIGNED CONTROLLER", style = MaterialTheme.typography.labelSmall, color = SoftGray)
                                    Text(officerName ?: "Searching...", style = MaterialTheme.typography.bodyMedium, color = SoftWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Automatic Panic Audio/Video Evidence Sensor Recording buttons
        item {
            Text(
                text = "Secure Contextual Panic Evidence",
                style = MaterialTheme.typography.titleSmall,
                color = SoftGray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Audio Panic button
                Button(
                    onClick = { viewModel.togglePanicRecording("AUDIO") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording && recordingType == "AUDIO") EmergencyRuby else CardSurface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .testTag("panic_mic_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSlate)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Securing Audio",
                            tint = if (isRecording && recordingType == "AUDIO") Color.White else SoftAqua,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isRecording && recordingType == "AUDIO") "STOP MIC" else "PANIC AUDIO",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isRecording && recordingType == "AUDIO") Color.White else SoftWhite
                        )
                    }
                }

                // Video Panic button
                Button(
                    onClick = { viewModel.togglePanicRecording("VIDEO") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording && recordingType == "VIDEO") EmergencyRuby else CardSurface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .testTag("panic_cam_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSlate)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Securing Video",
                            tint = if (isRecording && recordingType == "VIDEO") Color.White else EmergencyRuby,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isRecording && recordingType == "VIDEO") "STOP VIDEO" else "PANIC VIDEO",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isRecording && recordingType == "VIDEO") Color.White else SoftWhite
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 2: EMERGENCY CONTACTS & SYNCED RESPONDERS
// ----------------------------------------------------
@Composable
fun RespondersScreen(viewModel: SafetyViewModel) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    var isPrimary by remember { mutableStateOf(false) }

    var expandedForm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("responders_scroll"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Emergency Responders",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SoftWhite
                    )
                    Text(
                        text = "These contacts are synchronized instantly when SOS alerts are activated.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftGray
                    )
                }

                IconButton(
                    onClick = { expandedForm = !expandedForm },
                    modifier = Modifier
                        .background(CardSurface, CircleShape)
                        .testTag("toggle_add_form_btn")
                ) {
                    Icon(
                        imageVector = if (expandedForm) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Toggle Contact inputs",
                        tint = SoftAqua
                    )
                }
            }
        }

        // Contact registry registration card form
        item {
            AnimatedVisibility(
                visible = expandedForm,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("add_responder_card"),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSlate)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Register Secure Responder",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SoftWhite
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name", color = SoftGray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SoftWhite,
                                unfocusedTextColor = SoftWhite,
                                focusedBorderColor = SoftAqua,
                                unfocusedBorderColor = OutlineSlate
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("input_responder_name")
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Contact Phone Line", color = SoftGray) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SoftWhite,
                                unfocusedTextColor = SoftWhite,
                                focusedBorderColor = SoftAqua,
                                unfocusedBorderColor = OutlineSlate
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("input_responder_phone")
                        )

                        OutlinedTextField(
                            value = relation,
                            onValueChange = { relation = it },
                            label = { Text("Relation (e.g. Spouse, Mother, Police)", color = SoftGray) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SoftWhite,
                                unfocusedTextColor = SoftWhite,
                                focusedBorderColor = SoftAqua,
                                unfocusedBorderColor = OutlineSlate
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("input_responder_relation")
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isPrimary = !isPrimary }
                        ) {
                            Checkbox(
                                checked = isPrimary,
                                onCheckedChange = { isPrimary = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = SoftAqua,
                                    uncheckedColor = OutlineSlate
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Designate as Lead Priority Responder",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SoftWhite
                            )
                        }

                        Button(
                            onClick = {
                                if (name.isNotBlank() && phone.isNotBlank()) {
                                    viewModel.addContact(name, phone, relation, isPrimary)
                                    name = ""
                                    phone = ""
                                    relation = ""
                                    isPrimary = false
                                    expandedForm = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftAqua),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_responder_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "SYNCHRONIZE TO SAFE CLOUD",
                                color = MidnightBase,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Responders directory listing
        if (contacts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardSurface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContactPhone,
                            contentDescription = "Empty Responders directory",
                            tint = SoftGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Secure Responder List Empty",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Synchronize at least one trusted friend, family member, or local patrol node to notify.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(contacts, key = { it.id }) { contact ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (contact.isPrimary) SoftAqua.copy(alpha = 0.5f) else OutlineSlate
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("responder_item_${contact.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (contact.isPrimary) SoftAqua.copy(alpha = 0.15f) else OutlineSlate.copy(alpha = 0.3f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (contact.phoneNumber == "911") Icons.Default.LocalPhone else Icons.Default.Person,
                                contentDescription = "Active Responder Icon",
                                tint = if (contact.isPrimary) SoftAqua else SoftWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = contact.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (contact.isPrimary) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = SoftAqua.copy(alpha = 0.2f),
                                    ) {
                                        Text(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            text = "PRIMARY",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SoftAqua,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${contact.relation}  •  ${contact.phoneNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftGray
                            )
                        }

                        // Contact control tasks
                        if (!contact.isPrimary) {
                            IconButton(
                                onClick = { viewModel.makePrimary(contact) },
                                modifier = Modifier.testTag("primary_responder_btn_${contact.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Designate primary",
                                    tint = DimGray
                                )
                            }
                        }

                        if (contact.phoneNumber != "911") {
                            IconButton(
                                onClick = { viewModel.removeContact(contact) },
                                modifier = Modifier.testTag("delete_responder_btn_${contact.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Purge responder reference",
                                    tint = EmergencyRuby
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 3: EVIDENCE RECORDS LOOPS & ACTIVITY LOGS
// ----------------------------------------------------
@Composable
fun EvidenceLogsScreen(viewModel: SafetyViewModel) {
    val panicRecords by viewModel.panicRecords.collectAsStateWithLifecycle()
    val logs by viewModel.triggerEvents.collectAsStateWithLifecycle()
    val playbackId by viewModel.playbackId.collectAsStateWithLifecycle()

    var activeLogsTab by remember { mutableStateOf(0) } // 0 = Saved Records, 1 = Audit Path

    val timeFormatter = remember { SimpleDateFormat("MMM dd, yyyy  •  HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("evidence_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Design visual header
        Column {
            Text(
                text = "Secure Incident Deck",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SoftWhite
            )
            Text(
                text = "On-Device evidence storage ledger and network audit logs.",
                style = MaterialTheme.typography.bodySmall,
                color = SoftGray
            )
        }

        // Sub tab options
        TabRow(
            selectedTabIndex = activeLogsTab,
            containerColor = DepthSurface,
            contentColor = SoftWhite,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeLogsTab]),
                    color = if (activeLogsTab == 0) EmergencyRuby else SecureEmerald
                )
            },
            modifier = Modifier.testTag("logs_sub_tab")
        ) {
            Tab(
                selected = activeLogsTab == 0,
                onClick = { activeLogsTab = 0 },
                text = { Text("Secured Evidence (${panicRecords.size})", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_evidence")
            )
            Tab(
                selected = activeLogsTab == 1,
                onClick = { activeLogsTab = 1 },
                text = { Text("Audit Path Logs", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_audit")
            )
        }

        if (activeLogsTab == 0) {
            // SECURED EVIDENCE LIST
            if (panicRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Hearing,
                            contentDescription = "No secure recordings",
                            tint = SoftGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No Panic Evidence Captured",
                            style = MaterialTheme.typography.titleSmall,
                            color = SoftWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Automatic video and audio streams when panic levels are triggered populate here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("records_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(panicRecords) { record ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSlate)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (record.type == "VIDEO") EmergencyRuby.copy(alpha = 0.15f) else SoftAqua.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (record.type == "VIDEO") Icons.Default.Videocam else Icons.Default.Mic,
                                        contentDescription = "Evidence Record Type Logo",
                                        tint = if (record.type == "VIDEO") EmergencyRuby else SoftAqua,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${record.type} EVIDENCE CAPTURE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (record.type == "VIDEO") EmergencyRuby else SoftAqua
                                    )

                                    val dateStr = try {
                                        timeFormatter.format(Date(record.timestamp))
                                    } catch (e: Exception) {
                                        "Saved Recording Token"
                                    }
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SoftWhite,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = "Duration: ${record.durationMs / 1000}s  •  ${record.locationText}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftGray
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.togglePlayRecord(record) },
                                        modifier = Modifier.testTag("playback_evidence_btn_${record.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (playbackId == record.id) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = "Access playback",
                                            tint = SecureEmerald
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deletePanicRecord(record) },
                                        modifier = Modifier.testTag("delete_evidence_btn_${record.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Purge recording storage",
                                            tint = EmergencyRuby
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // AUDIT LOGS LIST
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.HistoryToggleOff,
                            contentDescription = "Audit Trail Clean",
                            tint = SoftGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Audit Trail Clean",
                            style = MaterialTheme.typography.titleSmall,
                            color = SoftWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Safety actions, tracking nodes updates, and active dispatch handshakes appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { viewModel.clearLogs() },
                        colors = ButtonDefaults.buttonColors(containerColor = DepthSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSlate),
                        modifier = Modifier.align(Alignment.End).testTag("clear_logs_btn")
                    ) {
                        Text("Reset Logs", color = EmergencyRuby, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("audit_list"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(logs) { log ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DepthSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineSlate)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val badgeColor = when (log.type) {
                                                "SOS" -> EmergencyRuby
                                                "PANIC" -> WarnAmber
                                                else -> SoftAqua
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(badgeColor)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = log.type,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = badgeColor,
                                                fontWeight = FontWeight.Black
                                            )
                                        }

                                        val timestampStr = try {
                                            timeFormatter.format(Date(log.timestamp))
                                        } catch (e: Exception) {
                                            "Recent"
                                        }
                                        Text(
                                            text = timestampStr,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = DimGray
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = log.status,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SoftWhite,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = log.details,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
