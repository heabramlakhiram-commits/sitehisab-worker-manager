package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.Attendance
import com.example.data.entity.Payment
import com.example.data.entity.Worker
import com.example.ui.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SiteHisabApp(viewModel: SiteHisabViewModel) {
    val navState by viewModel.navigationState.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = navState,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "ScreenTransition"
        ) { state ->
            when (state) {
                is NavigationState.Login -> LoginScreen(viewModel)
                is NavigationState.ManagerDashboard -> ManagerDashboardScreen(viewModel)
                is NavigationState.AddWorker -> AddWorkerScreen(viewModel)
                is NavigationState.WorkerDetails -> WorkerDetailsScreen(viewModel, state.workerId)
                is NavigationState.WorkerDashboard -> WorkerDashboardScreen(viewModel, state.workerId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: SiteHisabViewModel) {
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Construction Logo visual
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Construction,
                        contentDescription = "Builder Tool Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(45.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SiteHisab",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Construction Worker Manager",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = SoftLabelText,
                    modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                )

                // Error State Alert Banner
                if (authState is AuthState.Error) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AlertRed.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error Logo",
                                tint = AlertRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = (authState as AuthState.Error).message,
                                color = LightAccentText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Phone Input Field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { 
                        if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                            phone = it
                            viewModel.clearAuthError()
                        }
                    },
                    label = { Text("Mobile Number") },
                    placeholder = { Text("10-digit phone number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = SoftLabelText,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = SoftLabelText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // OTP Box showing after Send request triggers
                AnimatedVisibility(visible = otpSent) {
                    Column {
                        OutlinedTextField(
                            value = otp,
                            onValueChange = { 
                                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                    otp = it
                                    viewModel.clearAuthError()
                                }
                            },
                            label = { Text("OTP code") },
                            placeholder = { Text("6-digit safety code") },
                            leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = "OTP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = SoftLabelText
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (phone.length < 10) {
                            // Quick simulation of invalid length locally
                        } else if (!otpSent) {
                            otpSent = true
                        } else {
                            viewModel.login(phone, otp)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (!otpSent) {
                        Text(
                            "REQUEST OTP",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                    } else {
                        Text(
                            "VERIFY & LOGIN",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Help/Onboarding hint card to help testing two-way feature easily
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Role Helper icon",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Easy Testing Role Credentials",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Admin/Manager Roll:\n  Phone: 9876543210  |  OTP: 123456",
                            style = MaterialTheme.typography.bodySmall,
                            color = LightAccentText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Worker Portal:\n  Phone: 9811111111 (Ramesh)  |  OTP: 123456\n  (Or phone of any new worker that you register below!)",
                            style = MaterialTheme.typography.bodySmall,
                            color = LightAccentText
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerDashboardScreen(viewModel: SiteHisabViewModel) {
    val workers by viewModel.allWorkers.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val showOnlyActive by viewModel.showOnlyActive.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val attendanceMap by viewModel.attendanceMap.collectAsStateWithLifecycle()
    
    // Compute quick dashboard statistics for active workers today
    val filteredWorkers = workers.filter { worker ->
        val matchesSearch = worker.name.contains(searchQuery, ignoreCase = true) || worker.phone.contains(searchQuery)
        val matchesStatus = if (showOnlyActive) worker.status == "Active" else worker.status == "Left"
        matchesSearch && matchesStatus
    }

    // Attendance stats for today
    val activeCount = workers.count { it.status == "Active" }
    val presentCountOfToday = attendanceMap.values.count { it.status == "Present" }
    val halfDayCountOfToday = attendanceMap.values.count { it.status == "Half-Day" }
    val absentCountOfToday = attendanceMap.values.count { it.status == "Absent" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Engineering,
                            contentDescription = "Builder helmet icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Manager Workspace", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("SiteHisab", style = MaterialTheme.typography.bodySmall.copy(color = SoftLabelText))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", tint = AlertRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.navigateTo(NavigationState.AddWorker) },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Add worker") },
                text = { Text("Add Worker") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            // Stats Row Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Active Force", style = MaterialTheme.typography.bodySmall, color = SoftLabelText)
                        Text("$activeCount Workers", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("On Duty Today", style = MaterialTheme.typography.bodySmall, color = SoftLabelText)
                        Text("${presentCountOfToday + halfDayCountOfToday} Active", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = AlertGreen)
                    }
                }
            }

            // Attendance Date Controller Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        val cal = parseDateString(selectedDate)
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                        viewModel.setDate(formatCalendarDate(cal))
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            // Reset back to today
                            viewModel.setDate(getTodayDateString())
                        }
                    ) {
                        Icon(Icons.Default.Today, contentDescription = "Today icon", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatDateForDisplay(selectedDate),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    IconButton(onClick = {
                        val cal = parseDateString(selectedDate)
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        viewModel.setDate(formatCalendarDate(cal))
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                    }
                }
            }

            // Quick search and active/inactive status filter tabs
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by worker name or phone...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search bar") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surface
                )
            )

            // Status Filter Tab Bar (Active / Left Toggle Switches)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val activeColors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                val inactiveColors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = SoftLabelText)

                Button(
                    onClick = { viewModel.setShowOnlyActive(true) },
                    colors = if (showOnlyActive) activeColors else inactiveColors,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Active Filter Logo", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ACTIVE (" + workers.count { it.status == "Active" } + ")", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }

                Button(
                    onClick = { viewModel.setShowOnlyActive(false) },
                    colors = if (!showOnlyActive) activeColors else inactiveColors,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.History, contentDescription = "Left Workers history filter key", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("LEFT/LEFT SITE (" + workers.count { it.status == "Left" } + ")", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
            }

            // Active Workers Attendance Roster Sheet
            Text(
                text = if (showOnlyActive) "Daily Attendance Roll-Call" else "Auditing Inactive / Workers Left",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                color = SoftLabelText,
                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
            )

            if (filteredWorkers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty Box",
                            tint = SoftLabelText,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (showOnlyActive) "No active workers found.\nClick '+' bottom-right to add some!" else "No legacy workers files archived.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftLabelText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredWorkers, key = { it.workerId }) { worker ->
                        val attendance = attendanceMap[worker.workerId]
                        WorkerAttendanceCard(
                            worker = worker,
                            attendance = attendance,
                            isAttendanceEditable = showOnlyActive,
                            onAttendanceChange = { selectedStatus ->
                                viewModel.setAttendance(worker.workerId, selectedStatus)
                            },
                            onCardClick = {
                                viewModel.navigateTo(NavigationState.WorkerDetails(worker.workerId))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkerAttendanceCard(
    worker: Worker,
    attendance: Attendance?,
    isAttendanceEditable: Boolean,
    onAttendanceChange: (String) -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = worker.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = LightAccentText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (worker.status == "Left") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = AlertRed.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "LEFT",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AlertRed)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Phone: ${worker.phone}  |  Wage: ₹${worker.dailyWage}/day",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftLabelText
                    )
                }
                
                // Info Arrow
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Details logo",
                    tint = SoftLabelText
                )
            }

            if (isAttendanceEditable) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Mark Today:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = SoftLabelText
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AttendancePill(
                            label = "Present",
                            isSelected = attendance?.status == "Present",
                            selectedColor = AlertGreen,
                            onClick = { onAttendanceChange("Present") }
                        )

                        AttendancePill(
                            label = "Half-Day",
                            isSelected = attendance?.status == "Half-Day",
                            selectedColor = AlertPending,
                            onClick = { onAttendanceChange("Half-Day") }
                        )

                        AttendancePill(
                            label = "Absent",
                            isSelected = attendance?.status == "Absent",
                            selectedColor = AlertRed,
                            onClick = { onAttendanceChange("Absent") }
                        )
                    }
                }
            } else {
                // If displaying Left workers - briefly display last known details
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Historical status is archived for auditing. Toggle active to resume tracking.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftLabelText
                )
            }
        }
    }
}

@Composable
fun AttendancePill(
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) selectedColor.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.background
            )
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (isSelected) selectedColor else MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = if (isSelected) selectedColor else SoftLabelText
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkerScreen(viewModel: SiteHisabViewModel) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var wage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Construction Worker", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Register a new member to the workspace ledger. They will be immediately available in the attendance list and can view their personal account via mobile numbers.",
                style = MaterialTheme.typography.bodyMedium,
                color = SoftLabelText
            )

            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AlertRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        color = AlertRed,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMessage = null },
                label = { Text("Worker Full Name") },
                placeholder = { Text("E.g., Ramesh Kumar") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name logo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = SoftLabelText
                )
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { 
                    if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                        phone = it
                        errorMessage = null 
                    }
                },
                label = { Text("Mobile Number (Private Login ID)") },
                placeholder = { Text("E.g., 9811111111") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone logo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = SoftLabelText
                )
            )

            OutlinedTextField(
                value = wage,
                onValueChange = { 
                    if (it.all { char -> char.isDigit() || char == '.' }) {
                        wage = it
                        errorMessage = null
                    }
                },
                label = { Text("Daily Wage Rate (₹ / Day)") },
                placeholder = { Text("E.g., 450") },
                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = "Wage logo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = SoftLabelText
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val wageDouble = wage.toDoubleOrNull()
                    if (name.trim().isEmpty() || phone.length < 10 || wageDouble == null || wageDouble <= 0) {
                        errorMessage = "Please enter valid fields. Code requires a valid Name, 10-digit Phone, and valid Daily Wage rate."
                    } else {
                        viewModel.addWorker(name.trim(), phone, wageDouble)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.HowToReg, contentDescription = "Submit worker creation logo")
                Spacer(modifier = Modifier.width(8.dp))
                Text("ONBOARD WORKER", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDetailsScreen(viewModel: SiteHisabViewModel, workerId: Int) {
    val workerOption by viewModel.getWorkerFlow(workerId).collectAsStateWithLifecycle(initialValue = null)
    val attendanceList by viewModel.getAttendanceForWorker(workerId).collectAsStateWithLifecycle(initialValue = emptyList())
    val paymentsList by viewModel.getPaymentsForWorker(workerId).collectAsStateWithLifecycle(initialValue = emptyList())

    // Modal adding payment inputs
    var showPaymentDialog by remember { mutableStateOf(false) }
    var paymentAmount by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf("Advance") } // "Advance" or "Final Salary"

    var isEditMode by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editWage by remember { mutableStateOf("") }

    if (workerOption == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val worker = workerOption!!
    
    // Initialize editing values once
    LaunchedEffect(worker, isEditMode) {
        if (isEditMode) {
            editName = worker.name
            editPhone = worker.phone
            editWage = worker.dailyWage.toString()
        }
    }

    // Financial ledger calculations
    val summary = viewModel.getFinancialSummary(worker, attendanceList, paymentsList)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(worker.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isEditMode = !isEditMode }) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = "Edit toggler",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        if (isEditMode) {
            // EDIT WORKER BOX SCREEN
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Edit Worker Profile", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = editPhone,
                    onValueChange = { editPhone = it },
                    label = { Text("Mobile Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = editWage,
                    onValueChange = { editWage = it },
                    label = { Text("Daily Wage rate") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Worker Left switch status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Worker Left status indicator", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Toggling archives them from daily lists.", style = MaterialTheme.typography.bodySmall, color = SoftLabelText)
                        }
                        Switch(
                            checked = worker.status == "Left",
                            onCheckedChange = { isChecked ->
                                viewModel.toggleWorkerStatus(worker.workerId, isChecked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AlertRed,
                                checkedTrackColor = AlertRed.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        val parsedWage = editWage.toDoubleOrNull() ?: worker.dailyWage
                        viewModel.updateWorkerDetails(
                            worker.copy(name = editName, phone = editPhone, dailyWage = parsedWage)
                        )
                        isEditMode = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Done editing")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SAVE CHANGES")
                }
            }
        } else {
            // SECURE DETAIL SCREEN
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Header Bio Row
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "WORKER FILE SUMMARY",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (worker.status == "Active") AlertGreen.copy(alpha = 0.15f) else AlertRed.copy(alpha = 0.15f)
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = worker.status.uppercase(Locale.getDefault()),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = if (worker.status == "Active") AlertGreen else AlertRed)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(worker.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                            Text("Registered Phone: ${worker.phone}", style = MaterialTheme.typography.bodyMedium, color = SoftLabelText)
                            Text("Onboard Date: ${worker.joinDate}", style = MaterialTheme.typography.bodySmall, color = SoftLabelText)
                            Text("Standard Rate: ₹${worker.dailyWage} / day", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }

                // Mathematical Ledger Display Panel
                item {
                    Text("Financial Ledger", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = SoftLabelText)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Total Present Days:", style = MaterialTheme.typography.bodyMedium, color = SoftLabelText)
                            Text("${summary.totalPresentDays} Days (₹${summary.totalPresentDays * worker.dailyWage})", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Total Half Days:", style = MaterialTheme.typography.bodyMedium, color = SoftLabelText)
                            Text("${summary.totalHalfDays} Days (₹${summary.totalHalfDays * (worker.dailyWage / 2.0)})", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Total Absents Registered:", style = MaterialTheme.typography.bodyMedium, color = SoftLabelText)
                            Text("${summary.totalAbsentDays} Days", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = AlertRed)
                        }

                        Divider(color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Total Gross Earnings:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), color = LightAccentText)
                            Text("₹${summary.totalEarnings}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = LightAccentText)
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Total Advance Deductions:", style = MaterialTheme.typography.bodyMedium, color = AlertPending)
                            Text("-₹${summary.totalAdvanceTaken}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = AlertPending)
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Total Salary Disbursed:", style = MaterialTheme.typography.bodyMedium, color = AlertGreen)
                            Text("-₹${summary.totalFinalPaid}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = AlertGreen)
                        }

                        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Net Payable Balance:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "₹${summary.netPayable}",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Button adding payments ledger transaction inside details
                        Button(
                            onClick = { showPaymentDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add pay icon", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RECORD ADVANCE OR SALARY DISBURSEMENT", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                // Payments History ledger list item
                item {
                    Text(
                        text = "Recent Transactions history",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = SoftLabelText,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (paymentsList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("No payouts recorded yet for this worker.", color = SoftLabelText, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                } else {
                    items(paymentsList, key = { it.transactionId }) { payment ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (payment.type == "Advance") AlertPending.copy(alpha = 0.15f) else AlertGreen.copy(alpha = 0.15f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (payment.type == "Advance") Icons.Default.SwapCalls else Icons.Default.DoneAll,
                                            contentDescription = "Tx Type",
                                            tint = if (payment.type == "Advance") AlertPending else AlertGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(payment.type, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(payment.date, style = MaterialTheme.typography.bodySmall, color = SoftLabelText)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("₹${payment.amount}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (payment.status == "Success") AlertGreen.copy(alpha = 0.12f) else AlertPending.copy(alpha = 0.12f)
                                            ),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = payment.status.uppercase(Locale.getDefault()),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = if (payment.status == "Success") AlertGreen else AlertPending)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    // Let manager Toggle Status success/pending or Delete transaction
                                    IconButton(onClick = {
                                        if (payment.status == "Pending") {
                                            viewModel.updatePaymentStatus(payment, "Success")
                                        } else {
                                            viewModel.deletePayment(payment.transactionId)
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if (payment.status == "Pending") Icons.Default.CheckCircleOutline else Icons.Default.DeleteOutline,
                                            contentDescription = "Action payout",
                                            tint = if (payment.status == "Pending") AlertGreen else AlertRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Attendance snapshot logs
                item {
                    Text(
                        text = "Attendance Logs History",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = SoftLabelText,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (attendanceList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("No attendance logged yet.", color = SoftLabelText, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                } else {
                    items(attendanceList) { att ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "date icon",
                                        tint = SoftLabelText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(att.date, style = MaterialTheme.typography.bodyMedium)
                                }
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = when(att.status) {
                                            "Present" -> AlertGreen.copy(alpha = 0.15f)
                                            "Half-Day" -> AlertPending.copy(alpha = 0.15f)
                                            else -> AlertRed.copy(alpha = 0.15f)
                                        }
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = att.status.uppercase(Locale.getDefault()),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = when(att.status) {
                                                "Present" -> AlertGreen
                                                "Half-Day" -> AlertPending
                                                else -> AlertRed
                                            }
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialogue container for Payments Onboarding
    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Log New Payment Transaction", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select payment classification type and amount below:", style = MaterialTheme.typography.bodyMedium, color = SoftLabelText)
                    
                    // Toggle Payment Classification Type
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { paymentType = "Advance" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (paymentType == "Advance") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                                contentColor = if (paymentType == "Advance") MaterialTheme.colorScheme.onPrimary else LightAccentText
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ADVANCE")
                        }

                        Button(
                            onClick = { paymentType = "Final Salary" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (paymentType == "Final Salary") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                                contentColor = if (paymentType == "Final Salary") MaterialTheme.colorScheme.onPrimary else LightAccentText
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("SALARY")
                        }
                    }

                    OutlinedTextField(
                        value = paymentAmount,
                        onValueChange = { paymentAmount = it },
                        label = { Text("Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = paymentAmount.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            // Defaulting as success/pending or dynamic success
                            viewModel.recordPayment(worker.workerId, amt, paymentType, "Success")
                            showPaymentDialog = false
                            paymentAmount = ""
                        }
                    }
                ) {
                    Text("DISBURSE PAY")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDashboardScreen(viewModel: SiteHisabViewModel, workerId: Int) {
    // Current worker session
    val workerOption by viewModel.getWorkerFlow(workerId).collectAsStateWithLifecycle(initialValue = null)
    val attendanceList by viewModel.getAttendanceForWorker(workerId).collectAsStateWithLifecycle(initialValue = emptyList())
    val paymentsList by viewModel.getPaymentsForWorker(workerId).collectAsStateWithLifecycle(initialValue = emptyList())

    if (workerOption == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val worker = workerOption!!
    val summary = viewModel.getFinancialSummary(worker, attendanceList, paymentsList)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Cabin,
                            contentDescription = "Builder lodge icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Workers Cabin", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("SiteHisab Secure Portal", style = MaterialTheme.typography.bodySmall.copy(color = SoftLabelText))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Exit log", tint = AlertRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Bio greeting
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = worker.name.take(1).uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Namaste,", style = MaterialTheme.typography.bodySmall, color = SoftLabelText)
                            Text(worker.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = LightAccentText)
                            Text("Registered Phone ID: ${worker.phone}", style = MaterialTheme.typography.bodySmall, color = SoftLabelText)
                        }
                    }
                }
            }

            // 1. My Wallet High-Contrast Widget Area
            item {
                Text(
                    text = "My Wallet Summary",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = SoftLabelText
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("NET PAYABLE TO YOU", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = MaterialTheme.colorScheme.primary)
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet Balance Icon", tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "₹${summary.netPayable}",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Work Earnings", style = MaterialTheme.typography.bodySmall, color = SoftLabelText)
                                Text("₹${summary.totalEarnings}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = LightAccentText)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Advances Deducted", style = MaterialTheme.typography.bodySmall, color = SoftLabelText)
                                Text("₹${summary.totalAdvanceTaken}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = AlertPending)
                            }
                        }
                    }
                }
            }

            // 2. Personal Calendar view representation matching requested database records
            item {
                Text(
                    text = "My Attendance Ledger",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = SoftLabelText
                )
                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current Month Summary (May 2026)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AttendanceStatusIndicator(label = "P", color = AlertGreen)
                                AttendanceStatusIndicator(label = "H", color = AlertPending)
                                AttendanceStatusIndicator(label = "A", color = AlertRed)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Render dynamic grid items for construction attendance representation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AttendanceProgressWidget(label = "Present", count = summary.totalPresentDays, color = AlertGreen)
                            AttendanceProgressWidget(label = "Half-Day", count = summary.totalHalfDays, color = AlertPending)
                            AttendanceProgressWidget(label = "Absent", count = summary.totalAbsentDays, color = AlertRed)
                        }

                        // Listing detailed calendars dates inside scroll
                        if (attendanceList.isEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No duty dates logged yet by manager.", color = SoftLabelText, style = MaterialTheme.typography.bodySmall)
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Display inline calendar grid representation (last 7 logs)
                            Text("Recent Attendance Logs Archive:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = SoftLabelText)
                            Spacer(modifier = Modifier.height(6.dp))
                            attendanceList.take(7).forEach { att ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(att.date, style = MaterialTheme.typography.bodySmall)
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = when(att.status) {
                                                "Present" -> AlertGreen.copy(alpha = 0.15f)
                                                "Half-Day" -> AlertPending.copy(alpha = 0.15f)
                                                else -> AlertRed.copy(alpha = 0.15f)
                                            }
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = att.status,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = when(att.status) {
                                                    "Present" -> AlertGreen
                                                    "Half-Day" -> AlertPending
                                                    else -> AlertRed
                                                }
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Payment status list of payouts with color-coded success badges
            item {
                Text(
                    text = "My Payments Status Logs",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = SoftLabelText
                )
            }

            if (paymentsList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No legacy payouts recorded by manager yet.", color = SoftLabelText, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                items(paymentsList) { payment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = payment.type,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (payment.type == "Advance") AlertPending else AlertGreen
                                )
                                Text("Date: ${payment.date}", style = MaterialTheme.typography.bodySmall, color = SoftLabelText)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${payment.amount}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (payment.status == "Success") AlertGreen.copy(alpha = 0.15f) else AlertPending.copy(alpha = 0.15f)
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = payment.status.uppercase(Locale.getDefault()),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (payment.status == "Success") AlertGreen else AlertPending
                                        )
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

@Composable
fun AttendanceStatusIndicator(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.background)
        }
    }
}

@Composable
fun AttendanceProgressWidget(label: String, count: Int, color: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = SoftLabelText)
        Text(text = "$count Days", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color)
    }
}

// Global Calendar Dates Parsers & Formatters
private fun parseDateString(dateStr: String): Calendar {
    val cal = Calendar.getInstance()
    try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
        if (date != null) {
            cal.time = date
        }
    } catch (e: Exception) {
        // default fallback
    }
    return cal
}

private fun formatCalendarDate(cal: Calendar): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
}

private fun formatDateForDisplay(dateStr: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
        if (date != null) {
            SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(date)
        } else {
            dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}

private fun getTodayDateString(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
}
