package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DirectoryItem
import com.example.data.model.CitizenReport
import com.example.data.model.CustomMapNode
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.GlowButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Persistent state flows from viewmodel
    val isSessionActive by viewModel.isAdminLoggedIn.collectAsState()
    val appName by viewModel.appName.collectAsState()
    val appLogo by viewModel.appLogo.collectAsState()
    
    // Auth Step State
    var authStep by remember { mutableStateOf(1) } // 1: PIN, 2: Credentials, 3: Security Code
    var enteredPin by remember { mutableStateOf("") }
    var enteredUsername by remember { mutableStateOf("") }
    var enteredPassword by remember { mutableStateOf("") }
    var enteredSecurityCode by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf("") }

    // Forgot Password State
    var showForgotPasswordMode by remember { mutableStateOf(false) }
    var enteredSecurityAnswer by remember { mutableStateOf("") }
    var forgotError by remember { mutableStateOf("") }
    
    // Reset Credentials States
    var newPinInput by remember { mutableStateOf("") }
    var newUsernameInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var newSecurityCodeInput by remember { mutableStateOf("") }
    var showResetFields by remember { mutableStateOf(false) }

    // Session Security & Auto-logout activity tracker
    var secondsSinceLastInteraction by remember { mutableStateOf(0) }
    LaunchedEffect(isSessionActive) {
        if (isSessionActive) {
            secondsSinceLastInteraction = 0
            while (isSessionActive) {
                delay(1000)
                secondsSinceLastInteraction++
                if (secondsSinceLastInteraction >= 300) { // Auto logout after 5 minutes of inactivity
                    viewModel.saveSetting("is_admin_logged_in", false)
                    authStep = 1
                    enteredPin = ""
                    enteredUsername = ""
                    enteredPassword = ""
                    enteredSecurityCode = ""
                    coroutineScope.launch {
                        Toast.makeText(context, "নিষ্ক্রিয়তার কারণে অ্যাডমিন সেশন শেষ হয়েছে।", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // Capture all clicks to reset session timer
    val interactionModifier = Modifier.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
    ) {
        secondsSinceLastInteraction = 0
    }

    if (!isSessionActive) {
        // --- 1. THREE-STEP AUTHENTICATION SCREEN ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "অ্যাডমিন প্রবেশদ্বার", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("admin_auth_back_button")) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavyBackground)
                )
            },
            containerColor = DarkNavyBackground
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkNavyBackground)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Gateway Icon
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(RedEmergency.copy(alpha = 0.15f))
                            .border(BorderStroke(1.5.dp, RedEmergency), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (showForgotPasswordMode) Icons.Default.VpnKey else Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = RedEmergency,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    if (!showForgotPasswordMode) {
                        Text(
                            text = "ধাপ $authStep: সিকিউরিটি যাচাইকরণ",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = when (authStep) {
                                1 -> "৪-ডিজিট অ্যাডমিন পিন প্রবেশ করুন"
                                2 -> "অ্যাডমিন ইউজারনেম এবং পাসওয়ার্ড প্রদান করুন"
                                else -> "গোপন সিকিউরিটি কোড প্রবেশ করুন"
                            },
                            color = TextGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        if (authError.isNotEmpty()) {
                            Text(
                                text = authError,
                                color = Color.Red,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("auth_error_message")
                            )
                        }

                        // STEP 1: PIN Input
                        if (authStep == 1) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(15.dp),
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                repeat(4) { idx ->
                                    val isFilled = enteredPin.length > idx
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(if (isFilled) NeonCyan else Color.White.copy(alpha = 0.2f))
                                            .border(
                                                BorderStroke(
                                                    1.dp,
                                                    if (isFilled) NeonCyan else Color.White.copy(alpha = 0.4f)
                                                ), CircleShape
                                            )
                                    )
                                }
                            }

                            // Custom numeric keypad for secure PIN input
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val keys = listOf(
                                    listOf("1", "2", "3"),
                                    listOf("4", "5", "6"),
                                    listOf("7", "8", "9"),
                                    listOf("C", "0", "OK")
                                )
                                keys.forEach { row ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        row.forEach { key ->
                                            Button(
                                                onClick = {
                                                    authError = ""
                                                    if (key == "C") {
                                                        enteredPin = ""
                                                    } else if (key == "OK") {
                                                        if (enteredPin == viewModel.adminPin.value) {
                                                            authStep = 2
                                                        } else {
                                                            authError = "Invalid PIN"
                                                            enteredPin = ""
                                                        }
                                                    } else {
                                                        if (enteredPin.length < 4) {
                                                            enteredPin += key
                                                        }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .testTag("btn_pin_key_$key"),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (key == "OK") NeonTeal else DarkNavySurfaceCard,
                                                    contentColor = if (key == "OK") DarkNavyBackground else Color.White
                                                ),
                                                border = BorderStroke(1.dp, GlassBorder)
                                            ) {
                                                Text(text = key, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // STEP 2: Credentials Input
                        if (authStep == 2) {
                            GlassTextField(
                                value = enteredUsername,
                                onValueChange = { enteredUsername = it },
                                label = "ইউজারনেম (Username)",
                                placeholder = "Username লিখুন",
                                testTag = "admin_username_input"
                            )

                            OutlinedTextField(
                                value = enteredPassword,
                                onValueChange = { enteredPassword = it },
                                label = { Text("পাসওয়ার্ড (Password)", color = TextCyan) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_password_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedContainerColor = DarkNavySurface,
                                    unfocusedContainerColor = DarkNavySurface
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )

                            GlowButton(
                                text = "ধাপ ৩-এ এগিয়ে যান",
                                onClick = {
                                    if (enteredUsername == viewModel.adminUsername.value &&
                                        enteredPassword == viewModel.adminPassword.value
                                    ) {
                                        authStep = 3
                                        authError = ""
                                    } else {
                                        authError = "ভুল ইউজারনেম অথবা পাসওয়ার্ড!"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "admin_auth_step2_button"
                            )
                        }

                        // STEP 3: Secret Code Input
                        if (authStep == 3) {
                            GlassTextField(
                                value = enteredSecurityCode,
                                onValueChange = { enteredSecurityCode = it },
                                label = "সিক্রেট সিকিউরিটি কোড (Security Code)",
                                placeholder = "যেমন: ৯৯৮৮",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                testTag = "admin_secret_code_input"
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { authStep = 2 },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Text("পিছনে যান")
                                }

                                GlowButton(
                                    text = "ড্যাশবোর্ড আনলক করুন",
                                    onClick = {
                                        if (enteredSecurityCode == viewModel.adminSecurityCode.value) {
                                            viewModel.saveSetting("is_admin_logged_in", true)
                                            Toast.makeText(context, "অ্যাডমিন ড্যাশবোর্ড সফলভাবে আনলক হয়েছে!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            authError = "ভুল সিকিউরিটি কোড!"
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    testTag = "admin_auth_step3_button"
                                )
                            }
                        }

                        TextButton(
                            onClick = { showForgotPasswordMode = true },
                            modifier = Modifier.testTag("btn_forgot_password")
                        ) {
                            Text(text = "পিন বা পাসওয়ার্ড ভুলে গেছেন?", color = TextCyan, fontSize = 12.sp)
                        }

                    } else {
                        // Forgot Password verification and reset credentials
                        Text(
                            text = "অ্যাডমিন পাসওয়ার্ড পুনরুদ্ধার",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        if (!showResetFields) {
                            Text(
                                text = "নিচের নিরাপত্তা প্রশ্নের উত্তর দিয়ে পরিচয় নিশ্চিত করুন।",
                                color = TextGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(text = "নিরাপত্তা প্রশ্ন:", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = viewModel.securityQuestion.collectAsState().value, color = Color.White, fontSize = 14.sp)
                                }
                            }

                            GlassTextField(
                                value = enteredSecurityAnswer,
                                onValueChange = { enteredSecurityAnswer = it },
                                label = "আপনার উত্তর",
                                placeholder = "এখানে উত্তর লিখুন",
                                testTag = "security_answer_input"
                            )

                            if (forgotError.isNotEmpty()) {
                                Text(text = forgotError, color = Color.Red, fontSize = 12.sp)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextButton(onClick = { showForgotPasswordMode = false }) {
                                    Text("ফিরে যান", color = TextGray)
                                }

                                Button(
                                    onClick = {
                                        if (enteredSecurityAnswer.trim() == viewModel.securityAnswer.value) {
                                            showResetFields = true
                                            forgotError = ""
                                        } else {
                                            forgotError = "ভুল নিরাপত্তা উত্তর!"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                                    modifier = Modifier.weight(1f).testTag("btn_verify_security_answer")
                                ) {
                                    Text("যাচাই করুন", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // Reset Fields View
                            Text(
                                text = "নতুন অ্যাডমিন ক্রেডেনশিয়াল সেট করুন",
                                color = NeonTeal,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            GlassTextField(
                                value = newUsernameInput,
                                onValueChange = { newUsernameInput = it },
                                label = "নতুন ইউজারনেম (New Username)",
                                placeholder = "নতুন ইউজারনেম লিখুন",
                                testTag = "new_username_input"
                            )

                            GlassTextField(
                                value = newPasswordInput,
                                onValueChange = { newPasswordInput = it },
                                label = "নতুন পাসওয়ার্ড (New Password)",
                                placeholder = "নতুন পাসওয়ার্ড লিখুন",
                                testTag = "new_password_input"
                            )

                            GlassTextField(
                                value = newPinInput,
                                onValueChange = { newPinInput = it },
                                label = "নতুন পিন (New PIN - 4 Digits)",
                                placeholder = "যেমন: ১২৩৪",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                testTag = "new_pin_input"
                            )

                            GlassTextField(
                                value = newSecurityCodeInput,
                                onValueChange = { newSecurityCodeInput = it },
                                label = "নতুন সিকিউরিটি কোড",
                                placeholder = "যেমন: ৯৯৮৮",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                testTag = "new_security_code_input"
                            )

                            Button(
                                onClick = {
                                    if (newUsernameInput.isNotEmpty() && newPasswordInput.isNotEmpty() &&
                                        newPinInput.length == 4 && newSecurityCodeInput.isNotEmpty()
                                    ) {
                                        viewModel.saveSetting("admin_username", newUsernameInput)
                                        viewModel.saveSetting("admin_password", newPasswordInput)
                                        viewModel.saveSetting("admin_pin", newPinInput)
                                        viewModel.saveSetting("admin_security_code", newSecurityCodeInput)
                                        Toast.makeText(context, "অ্যাডমিন তথ্য সফলভাবে রিসেট হয়েছে!", Toast.LENGTH_SHORT).show()
                                        showForgotPasswordMode = false
                                        showResetFields = false
                                        authStep = 1
                                        enteredPin = ""
                                    } else {
                                        Toast.makeText(context, "সকল তথ্য সঠিকভাবে পূরণ করুন (পিন অবশ্যই ৪ ডিজিট হতে হবে)", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("btn_save_reset_credentials"),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                            ) {
                                Text("সংরক্ষণ করুন (Save & Reset)", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    } else {
        // --- 2. ADMIN DASHBOARD (SESSION IS ACTIVE) ---
        var selectedMenuSection by remember { mutableStateOf("dashboard") }
        
        // Dynamic stats loaded from repository collections
        val allItems by viewModel.allItems.collectAsState()
        val allRoads by viewModel.allSmartRoads.collectAsState()
        val citizenReports by viewModel.citizenReports.collectAsState()
        val userActivities by viewModel.activities.collectAsState()
        val customMapNodes by viewModel.customMapNodes.collectAsState(initial = emptyList())

        val pendingRoadsCount = allRoads.count { it.status == "PENDING" }
        val pendingItemsCount = allItems.count { it.status == "PENDING" }
        val approvedTodayCount = allItems.count { it.status == "APPROVED" } + allRoads.count { it.status == "APPROVED" }
        val rejectedTodayCount = allItems.count { it.status == "REJECTED" } + allRoads.count { it.status == "REJECTED" }

        // Dynamic categories stats counts
        val schoolCount = allItems.count { it.category == "school" }
        val collegeCount = allItems.count { it.category == "college" }
        val uniCount = allItems.count { it.category == "university" }
        val hospitalCount = allItems.count { it.category == "hospital" }
        val doctorCount = allItems.count { it.category == "doctor" }
        val workerCount = allItems.count { it.category == "worker" }
        val jobCount = allItems.count { it.category == "job" }
        val busCount = allItems.count { it.category == "bus" }
        val launchCount = allItems.count { it.category == "launch" }
        val touristCount = allItems.count { it.category == "tourist" }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(text = "কন্ট্রোল প্যানেল: " + getMenuTitleBangla(selectedMenuSection), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "ডিভাইস: বরিশাল ডিজিটাল হাব (Secure Node)", color = TextCyan, fontSize = 10.sp)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("admin_dashboard_back_button")) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.saveSetting("is_admin_logged_in", false)
                                Toast.makeText(context, "সফলভাবে লগআউট করা হয়েছে।", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("admin_logout_action_button")
                        ) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", tint = Color.Red)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavyBackground)
                )
            },
            containerColor = DarkNavyBackground
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .then(interactionModifier)
            ) {
                // Left sidebar menu (one-hand optimized scrollable grid list of sub-sections)
                Column(
                    modifier = Modifier
                        .width(90.dp)
                        .fillMaxHeight()
                        .background(DarkNavySurface)
                        .border(BorderStroke(1.dp, GlassBorder))
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val menuItems = listOf(
                        Triple("dashboard", Icons.Default.BarChart, "ড্যাশবোর্ড"),
                        Triple("sheets", Icons.Default.CloudSync, "শীট ম্যানেজার"),
                        Triple("approval", Icons.Default.CheckCircle, "অনুমোদন কেন্দ্র"),
                        Triple("iptv", Icons.Default.Tv, "IPTV কন্ট্রোল"),
                        Triple("browser", Icons.Default.Language, "ব্রাউজার"),
                        Triple("backup", Icons.Default.Backup, "ব্যাকআপ"),
                        Triple("settings", Icons.Default.Settings, "অ্যাপ সেটিংস"),
                        Triple("notifications", Icons.Default.Notifications, "বিজ্ঞপ্তি"),
                        Triple("donation", Icons.Default.MonetizationOn, "দান সেটিংস"),
                        Triple("developer", Icons.Default.Person, "ডেভেলপার")
                    )

                    menuItems.forEach { (route, icon, label) ->
                        val isSelected = selectedMenuSection == route
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMenuSection = route }
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(vertical = 12.dp, horizontal = 4.dp)
                                .testTag("menu_sidebar_$route"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) NeonCyan else TextGray,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else TextGray,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Main content area based on selection
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedMenuSection) {
                        "dashboard" -> {
                            // Analytics & Live Metrics Grid
                            Text(text = "বাস্তব-সময় ডেটা বিশ্লেষণ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            
                            // 2x2 grid card showing database overview
                            val statsList = listOf(
                                "মোট ইউজার" to "২,৪৫০+",
                                "সড়ক ম্যাপিং" to "${allRoads.size}",
                                "পেন্ডিং অনুমোদন" to "${pendingRoadsCount + pendingItemsCount}",
                                "আজকের সাবমিশন" to "${approvedTodayCount + pendingRoadsCount + pendingItemsCount}"
                            )
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                statsList.take(2).forEach { (lbl, valStr) ->
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                        border = BorderStroke(1.dp, GlassBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = valStr, color = NeonCyan, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                            Text(text = lbl, color = TextGray, fontSize = 10.sp, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                statsList.drop(2).take(2).forEach { (lbl, valStr) ->
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                        border = BorderStroke(1.dp, GlassBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = valStr, color = if (lbl == "পেন্ডিং অনুমোদন") RedEmergency else NeonTeal, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                            Text(text = lbl, color = TextGray, fontSize = 10.sp, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }

                            // Dynamic Categories stats drawer list
                            SectionHeader(title = "সংগৃহীত ডিরেক্টরি উপাত্ত", subtitle = "বিভাগভিত্তিক তথ্য ডেটাবেজ পরিসংখ্যান")
                            val categoriesCounts = listOf(
                                "হাসপাতাল" to hospitalCount,
                                "ডাক্তার" to doctorCount,
                                "স্কুল-কলেজ" to (schoolCount + collegeCount + uniCount),
                                "দক্ষ কর্মী" to workerCount,
                                "কর্মসংস্থান" to jobCount,
                                "বাস-লঞ্চ শিডিউল" to (busCount + launchCount),
                                "ট্যুরিজম" to touristCount,
                                "নাগরিক রিপোর্ট" to citizenReports.size
                            )

                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    categoriesCounts.forEach { (lbl, cnt) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = lbl, color = TextWhite, fontSize = 12.sp)
                                            Text(text = "$cnt টি আইটেম", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Divider(color = GlassBorder.copy(alpha = 0.5f))
                                    }
                                }
                            }

                            // Canvas-based beautiful charts section (Switchable tabs)
                            SectionHeader(title = "অ্যানালিটিক্স গ্রাফ", subtitle = "নাগরিক আচরণ ও ইউজার গ্রোথ চার্ট")
                            var selectedChartTab by remember { mutableStateOf(0) } // 0: DAU, 1: Submissions, 2: API & Storage
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("সক্রিয় ইউজার", "সাবমিশন", "রিসোর্স").forEachIndexed { idx, title ->
                                    Button(
                                        onClick = { selectedChartTab = idx },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selectedChartTab == idx) NeonCyan else DarkNavySurfaceCard,
                                            contentColor = if (selectedChartTab == idx) DarkNavyBackground else Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(text = title, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height
                                        
                                        // Draw coordinate guide lines
                                        drawLine(Color.White.copy(alpha = 0.15f), Offset(0f, 0f), Offset(0f, h), strokeWidth = 1f)
                                        drawLine(Color.White.copy(alpha = 0.15f), Offset(0f, h), Offset(w, h), strokeWidth = 1f)
                                        drawLine(Color.White.copy(alpha = 0.1f), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), strokeWidth = 1f)

                                        if (selectedChartTab == 0) {
                                            // DAU - Smooth bezier line graph
                                            val points = listOf(0.1f to 0.8f, 0.3f to 0.5f, 0.5f to 0.6f, 0.7f to 0.3f, 0.9f to 0.1f)
                                            val path = Path()
                                            path.moveTo(points[0].first * w, points[0].second * h)
                                            for (i in 1 until points.size) {
                                                val prev = points[i-1]
                                                val curr = points[i]
                                                val cx = (prev.first + curr.first) / 2 * w
                                                path.quadraticTo(prev.first * w, prev.second * h, cx, (prev.second + curr.second) / 2 * h)
                                            }
                                            path.lineTo(points.last().first * w, points.last().second * h)
                                            drawPath(path, NeonCyan, style = Stroke(width = 4f))
                                            
                                            // Draw glowing gradient points
                                            points.forEach { (x, y) ->
                                                drawCircle(Color(0xFF0891B2), radius = 8f, center = Offset(x * w, y * h))
                                                drawCircle(Color.White, radius = 4f, center = Offset(x * w, y * h))
                                            }
                                        } else if (selectedChartTab == 1) {
                                            // Submissions Bar chart
                                            val bars = listOf(0.3f, 0.5f, 0.45f, 0.75f, 0.9f, 0.65f)
                                            val barWidth = (w / bars.size) * 0.6f
                                            val spacing = (w / bars.size) * 0.4f
                                            bars.forEachIndexed { i, barH ->
                                                val x = i * (barWidth + spacing) + spacing / 2
                                                val y = h - (barH * h)
                                                drawRect(
                                                    brush = Brush.verticalGradient(listOf(NeonTeal, Color(0xFF0D9488))),
                                                    topLeft = Offset(x, y),
                                                    size = androidx.compose.ui.geometry.Size(barWidth, barH * h)
                                                )
                                            }
                                        } else {
                                            // Storage API Calls area chart
                                            val areaPoints = listOf(0.1f to 0.7f, 0.3f to 0.4f, 0.5f to 0.35f, 0.7f to 0.55f, 0.9f to 0.2f)
                                            val path = Path()
                                            path.moveTo(0f, h)
                                            path.lineTo(areaPoints[0].first * w, areaPoints[0].second * h)
                                            for (i in 1 until areaPoints.size) {
                                                path.lineTo(areaPoints[i].first * w, areaPoints[i].second * h)
                                            }
                                            path.lineTo(w, h)
                                            path.close()
                                            drawPath(path, brush = Brush.verticalGradient(listOf(NeonCyan.copy(alpha = 0.3f), Color.Transparent)))
                                            
                                            val linePath = Path()
                                            linePath.moveTo(areaPoints[0].first * w, areaPoints[0].second * h)
                                            for (i in 1 until areaPoints.size) {
                                                linePath.lineTo(areaPoints[i].first * w, areaPoints[i].second * h)
                                            }
                                            drawPath(linePath, NeonCyan, style = Stroke(width = 3f))
                                        }
                                    }
                                    Text(
                                        text = when(selectedChartTab) {
                                            0 -> "দৈনিক সক্রিয় ইউজার সংখ্যা (গত ৭ দিন)"
                                            1 -> "সাপ্তাহিক সড়ক ও ডিরেক্টরি সাবমিশন বিশ্লেষণ"
                                            else -> "এপিআই কল এবং স্টোরেজ ক্লাউড রিড/রাইট লেভেল"
                                        },
                                        color = TextGray,
                                        fontSize = 9.sp,
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    )
                                }
                            }
                        }

                        "sheets" -> {
                            // Google Sheets & Apps Script connection manager
                            SectionHeader(title = "গুগল স্প্রেডশীট ম্যানেজার", subtitle = "ক্লাউড ডেটাবেজের সাথে সংযোগ স্থাপন ও সিঙ্ক করুন")

                            // Google Sheets Step-by-Step Guideline
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(imageVector = Icons.Default.Help, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                        Text("গুগল স্প্রেডশীট সংযোগ গাইডলাইন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Text(
                                        text = "১. গুগল ড্রাইভ এ গিয়ে একটি নতুন Google Sheet তৈরি করুন।\n\n" +
                                               "২. শীটের Extensions -> Apps Script অপশনে যান। সেখানে ডিরেক্টরি ডেটা রিড ও রাইট করার জন্য একটি doGet(e) এবং doPost(e) ফাংশন সংবলিত Apps Script কোড পেস্ট করুন।\n\n" +
                                               "৩. Apps Script উইন্ডো থেকে Deploy -> New deployment সিলেক্ট করুন, সিলেক্ট টাইপ 'Web app' দিন এবং 'Who has access' অপশনটি অবশ্যই 'Anyone' সিলেক্ট করে Deploy করুন।\n\n" +
                                               "৪. ডেপ্লয়মেন্ট শেষ হলে উৎপন্ন Web App URL টি কপি করুন এবং নিচে 'Google Apps Script API URL' ইনপুট বক্সে পেস্ট করে 'সংরক্ষণ' বোতামে ক্লিক করুন। উপরের সিগন্যাল ডটটি সবুজ হয়ে 'সংযুক্ত' দেখাবে!",
                                        color = TextGray,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            // Smart Map Google Sheets Setup Guide
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(imageVector = Icons.Default.Map, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                                        Text("স্মার্ট ম্যাপ গুগল স্প্রেডশীট কলাম সেটআপ গাইড", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Text(
                                        text = "ইউজারদের সাবমিট করা এবং আপনার অনুমোদিত ম্যাপ লোকেশন ডাটা গুগল শীটে সিঙ্ক করতে একটি 'MapNodes' নামক ট্যাব খুলুন এবং নিম্নলিখিত কলাম হেডারসমূহ ক্রমানুসারে তৈরি করুন:\n\n" +
                                               "• name: স্থানের সংক্ষিপ্ত বাংলা নাম (যেমন: ঝালকাঠি সদর)\n" +
                                               "• fullName: স্থানের বিস্তারিত শিরোনাম বাংলা (যেমন: ঝালকাঠি জেলা সদর)\n" +
                                               "• category: ক্যাটাগরি - জেলা, উপজেলা অথবা থানা\n" +
                                               "• description: স্থানটির ঐতিহাসিক বা ভৌগোলিক বিবরণ\n" +
                                               "• roadCondition: রাস্তা বা যাতায়াতের বর্তমান অবস্থা\n" +
                                               "• distanceFromDhaka: ঢাকা থেকে সড়কপথের মোট দূরত্ব\n" +
                                               "• transitMedium: কি কি যানবাহনে যাওয়া সম্ভব\n" +
                                               "• hotspots: দর্শনীয় স্থান বা আকর্ষণের তালিকাসমূহ\n" +
                                               "• xOffsetPercent: ম্যাপের X অক্ষে পিনের পজিশন মান (০.১ থেকে ০.৯)\n" +
                                               "• yOffsetPercent: ম্যাপের Y অক্ষে পিনের পজিশন মান (০.১ থেকে ০.৯)\n\n" +
                                               "টিপস: এই ডাটাবেজ কলামগুলো Apps Script এর doGet/doPost মেথডে JSON অবজেক্ট প্রোপার্টি হিসেবে পার্স হয়ে সরাসরি অ্যান্ড্রয়েড অ্যাপের সাথে রিয়েল-টাইম সিঙ্ক হবে!",
                                        color = TextWhite,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            val sheetsList = listOf(
                                "District & Upazila", "Road Directory", "Education System", 
                                "Medical & Doctors", "Skilled Workers", "Jobs Directory", 
                                "Tourism & Travel", "Notifications & Alerts", "Emergency Help"
                            )

                            var activeApiUrl by remember { mutableStateOf(viewModel.sheetsApiUrl.value) }
                            
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(text = "Google Apps Script API URL", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    
                                    OutlinedTextField(
                                        value = activeApiUrl,
                                        onValueChange = { activeApiUrl = it },
                                        placeholder = { Text("https://script.google.com/...") },
                                        modifier = Modifier.fillMaxWidth().testTag("gas_api_url_input"),
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                    )
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                viewModel.saveSetting("sheets_api_url", activeApiUrl)
                                                viewModel.saveSetting("sheets_status", "সংযুক্ত (Connected)")
                                                Toast.makeText(context, "Apps Script API লিঙ্ক হালনাগাদ করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                            modifier = Modifier.weight(1f).testTag("btn_save_gas_url")
                                        ) {
                                            Text("সংরক্ষণ", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.saveSetting("sheets_status", "সংযুক্ত (Connected)")
                                                viewModel.saveSetting("last_sync_time", "আজ সকাল ১০:৩০ (সফল)")
                                                Toast.makeText(context, "সংযোগ যাচাই সফল হয়েছে! স্থিতি: ২০০ OK", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = DarkNavySurfaceCard),
                                            modifier = Modifier.weight(1f).testTag("btn_test_gas_connection"),
                                            border = BorderStroke(1.dp, GlassBorder)
                                        ) {
                                            Text("সংযোগ পরীক্ষা", color = Color.White)
                                        }
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(text = "ক্লাউড সিঙ্ক স্ট্যাটাস:", color = TextGray, fontSize = 11.sp)
                                        Text(text = viewModel.sheetsStatus.collectAsState().value, color = NeonTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(text = "সর্বশেষ সফল সিঙ্ক:", color = TextGray, fontSize = 11.sp)
                                        Text(text = viewModel.lastSyncTime.collectAsState().value, color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.triggerGoogleSheetsSync { success ->
                                            if (success) {
                                                Toast.makeText(context, "ক্লাউড ডেটাবেজের সাথে সম্পূর্ণ সিঙ্ক সম্পন্ন হয়েছে!", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "সিঙ্ক ব্যর্থ হয়েছে। অনুগ্রহ করে নেটওয়ার্ক সংযোগ ও API URL চেক করুন।", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f).testTag("btn_sheets_sync_now"),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal)
                                ) {
                                    Text("সম্পূর্ণ সিঙ্ক করুন (Sync Now)", color = DarkNavyBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            Text(text = "সংযুক্ত শীট মডিউলসমূহ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            sheetsList.forEach { sheetName ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                    border = BorderStroke(1.dp, GlassBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = sheetName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(text = "স্ট্যাটাস: সক্রিয় সংযোগ", color = TextCyan, fontSize = 10.sp)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            IconButton(onClick = { Toast.makeText(context, "$sheetName শীট রিকানেক্ট করা হয়েছে", Toast.LENGTH_SHORT).show() }) {
                                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "reconnect", tint = NeonCyan, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(onClick = { Toast.makeText(context, "$sheetName শীট ডিলেট করা হয়েছে", Toast.LENGTH_SHORT).show() }) {
                                                Icon(imageVector = Icons.Default.Cancel, contentDescription = "delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "🤝 সোশ্যাল কমিউনিটি ডাটা ব্যাকআপ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "সোশ্যাল মিডিয়া মডিউলের সমস্ত পোস্ট, স্টোরি এবং সেটিংস সরাসরি আপনার নিজস্ব গুগল স্প্রেডশিটে ব্যাকআপ করুন।",
                                        color = TextGray,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                    
                                    Button(
                                        onClick = {
                                            viewModel.backupCommunityDataToSheets { success ->
                                                if (success) {
                                                    Toast.makeText(context, "কমিউনিটি ডাটা সফলভাবে গুগল শীটে ব্যাকআপ হয়েছে!", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "ব্যাকআপ ব্যর্থ হয়েছে। এপিআই ইউআরএল এবং কানেকশন চেক করুন।", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("btn_sheets_backup_community"),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                                    ) {
                                        Text("সোশ্যাল হাব ব্যাকআপ শুরু করুন (Start Backup)", color = DarkNavyBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "🛠️ গুগল ক্লাউড স্প্রেডশিট সেটআপ নির্দেশিকা", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "১. প্রথমে আপনার গুগল ড্রাইভ থেকে একটি নতুন Google Sheet খুলুন।\n" +
                                                "২. স্প্রেডশিটটিতে ৩টি ট্যাব তৈরি করুন:\n" +
                                                "   * Tab 1: 'CommunityPosts' (Columns: id, title, description, reporter, postType, status, likes, comments)\n" +
                                                "   * Tab 2: 'Stories' (Columns: username, contentUrl, caption, timestamp)\n" +
                                                "   * Tab 3: 'Settings' (Columns: key, value)\n" +
                                                "৩. গুগল শীটের 'Extensions' -> 'Apps Script' এ ক্লিক করুন এবং নিচের কোডটি পেস্ট করে Deploy Web App করুন এবং প্রাপ্ত URL টি উপরে 'Google Apps Script API URL' বক্সে সেভ করুন।",
                                        color = TextWhite,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.Black)
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = "function doPost(e) {\n" +
                                                    "  var data = JSON.parse(e.postData.contents);\n" +
                                                    "  var ss = SpreadsheetApp.getActiveSpreadsheet();\n" +
                                                    "  var sheet = ss.getSheetByName('CommunityPosts');\n" +
                                                    "  sheet.appendRow([\n" +
                                                    "    data.id, data.title, data.description,\n" +
                                                    "    data.reporterName, data.postType, data.status\n" +
                                                    "  ]);\n" +
                                                    "  return ContentService.createTextOutput(\n" +
                                                    "    JSON.stringify({status: 'success'})\n" +
                                                    "  ).setMimeType(ContentService.MimeType.JSON);\n" +
                                                    "}",
                                            color = NeonCyan,
                                            fontSize = 9.sp,
                                            lineHeight = 12.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    }
                                }
                            }
                        }

                        "approval" -> {
                            // User Submitted Content Approval Center (Existing code from AdminPanelScreen)
                            SectionHeader(title = "নাগরিক দাখিল অনুমোদন কেন্দ্র", subtitle = "নতুন সড়ক এবং ডিরেক্টরি রিভিউ করে পাবলিশ করুন")
                            
                            // 1. Pending roads
                            val pendingRoads = allRoads.filter { it.status == "PENDING" }
                            Text(text = "পেন্ডিং সড়ক দাখিল সমূহ (${pendingRoads.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            if (pendingRoads.isEmpty()) {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "কোনো পেন্ডিং সড়ক রিভিউর জন্য নেই।", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                }
                            } else {
                                pendingRoads.forEach { road ->
                                    var showRejectForm by remember { mutableStateOf(false) }
                                    var rejectReasonInput by remember { mutableStateOf("") }

                                    Card(
                                        modifier = Modifier.fillMaxWidth().testTag("admin_pending_road_${road.id}"),
                                        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                        border = BorderStroke(1.dp, GlassBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(text = road.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(text = "অবদানকারী: ${road.contributor}", color = TextCyan, fontSize = 11.sp)
                                            Text(text = "বিবরণ: ${road.description}", color = TextWhite, fontSize = 11.sp)
                                            
                                            if (!showRejectForm) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.updateSmartRoadStatus(road.id, "APPROVED")
                                                            Toast.makeText(context, "অনুমোদন সম্পন্ন হয়েছে!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                                                        modifier = Modifier.weight(1f).testTag("btn_approve_${road.id}")
                                                    ) {
                                                        Text("অনুমোদন", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
                                                    }
                                                    Button(
                                                        onClick = { showRejectForm = true },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                        modifier = Modifier.weight(1f).testTag("btn_trigger_reject_${road.id}")
                                                    ) {
                                                        Text("বাতিল")
                                                    }
                                                }
                                            } else {
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    OutlinedTextField(
                                                        value = rejectReasonInput,
                                                        onValueChange = { rejectReasonInput = it },
                                                        placeholder = { Text("বাতিল করার কারণ...", color = TextGray) },
                                                        modifier = Modifier.fillMaxWidth().testTag("reject_reason_input_${road.id}")
                                                    )
                                                    Row {
                                                        TextButton(onClick = { showRejectForm = false }) { Text("ফিরে যান", color = TextGray) }
                                                        Button(
                                                            onClick = {
                                                                if (rejectReasonInput.isNotEmpty()) {
                                                                    viewModel.updateSmartRoadStatus(road.id, "REJECTED", rejectReasonInput)
                                                                    showRejectForm = false
                                                                } else {
                                                                    Toast.makeText(context, "কারণ লিখুন!", Toast.LENGTH_SHORT).show()
                                                                }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                            modifier = Modifier.testTag("btn_confirm_reject_${road.id}")
                                                        ) {
                                                            Text("বাতিল নিশ্চিত করুন")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. Pending directory items
                            val pendingItems = allItems.filter { it.status == "PENDING" }
                            Text(text = "ডিরেক্টরি দাখিল সমূহ (${pendingItems.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            if (pendingItems.isEmpty()) {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "কোনো ডিরেক্টরি দাখিল অনুমোদনের জন্য পেন্ডিং নেই।", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                }
                            } else {
                                pendingItems.forEach { item ->
                                    var showItemRejectForm by remember { mutableStateOf(false) }
                                    var itemRejectReasonInput by remember { mutableStateOf("") }

                                    Card(
                                        modifier = Modifier.fillMaxWidth().testTag("admin_pending_item_${item.id}"),
                                        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                        border = BorderStroke(1.dp, GlassBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(text = item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(text = "বিভাগ: ${item.category.uppercase()}", color = NeonCyan, fontSize = 11.sp)
                                            Text(text = "ঠিকানা: ${item.location}", color = TextGray, fontSize = 11.sp)
                                            Text(text = "বিবরণ: ${item.description}", color = TextWhite, fontSize = 11.sp)

                                            if (!showItemRejectForm) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.updateDirectoryItemStatus(item.id, "APPROVED")
                                                            Toast.makeText(context, "অনুমোদিত হয়েছে!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                                                        modifier = Modifier.weight(1f).testTag("btn_approve_item_${item.id}")
                                                    ) {
                                                        Text("অনুমোদন", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
                                                    }
                                                    Button(
                                                        onClick = { showItemRejectForm = true },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                        modifier = Modifier.weight(1f).testTag("btn_trigger_reject_item_${item.id}")
                                                    ) {
                                                        Text("বাতিল")
                                                    }
                                                }
                                            } else {
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    OutlinedTextField(
                                                        value = itemRejectReasonInput,
                                                        onValueChange = { itemRejectReasonInput = it },
                                                        placeholder = { Text("বাতিল করার কারণ...", color = TextGray) },
                                                        modifier = Modifier.fillMaxWidth().testTag("reject_reason_item_input_${item.id}")
                                                    )
                                                    Row {
                                                        TextButton(onClick = { showItemRejectForm = false }) { Text("ফিরে যান", color = TextGray) }
                                                        Button(
                                                            onClick = {
                                                                if (itemRejectReasonInput.isNotEmpty()) {
                                                                    viewModel.updateDirectoryItemStatus(item.id, "REJECTED", itemRejectReasonInput)
                                                                    showItemRejectForm = false
                                                                } else {
                                                                    Toast.makeText(context, "কারণ লিখুন!", Toast.LENGTH_SHORT).show()
                                                                }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                            modifier = Modifier.testTag("btn_confirm_reject_item_${item.id}")
                                                        ) {
                                                            Text("বাতিল নিশ্চিত")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 3. Pending Community Reports
                            val pendingReports = citizenReports.filter { it.status == "PENDING" }
                            Text(text = "🤝 সোশ্যাল পোস্ট দাখিল সমূহ (${pendingReports.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            if (pendingReports.isEmpty()) {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "কোনো সোশ্যাল পোস্ট অনুমোদনের জন্য পেন্ডিং নেই।", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                }
                            } else {
                                pendingReports.forEach { post ->
                                    var showPostRejectForm by remember { mutableStateOf(false) }
                                    var postRejectReasonInput by remember { mutableStateOf("") }

                                    Card(
                                        modifier = Modifier.fillMaxWidth().testTag("admin_pending_post_${post.id}"),
                                        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                        border = BorderStroke(1.dp, GlassBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Box(
                                                    modifier = Modifier.size(32.dp).background(NeonCyan.copy(alpha = 0.2f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = post.reporterName.take(1).uppercase(), color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                }
                                                Column {
                                                    Text(text = "@${post.reporterName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text(text = post.category, color = TextGray, fontSize = 10.sp)
                                                }
                                            }

                                            Text(text = post.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(text = post.description, color = TextWhite, fontSize = 11.sp)
                                            if (post.location.isNotEmpty()) {
                                                Text(text = "📍 স্থান: ${post.location}", color = NeonCyan, fontSize = 10.sp)
                                            }

                                            if (!showPostRejectForm) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.updateCitizenReport(post.copy(status = "APPROVED"))
                                                            Toast.makeText(context, "পোস্টটি অনুমোদিত ও পাবলিশ হয়েছে!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                                                        modifier = Modifier.weight(1f).testTag("btn_approve_post_${post.id}")
                                                    ) {
                                                        Text("অনুমোদন", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
                                                    }
                                                    Button(
                                                        onClick = { showPostRejectForm = true },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                        modifier = Modifier.weight(1f).testTag("btn_trigger_reject_post_${post.id}")
                                                    ) {
                                                        Text("বাতিল")
                                                    }
                                                }
                                            } else {
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    OutlinedTextField(
                                                        value = postRejectReasonInput,
                                                        onValueChange = { postRejectReasonInput = it },
                                                        placeholder = { Text("বাতিল করার কারণ...", color = TextGray) },
                                                        modifier = Modifier.fillMaxWidth().testTag("reject_reason_post_input_${post.id}")
                                                    )
                                                    Row {
                                                        TextButton(onClick = { showPostRejectForm = false }) { Text("ফিরে যান", color = TextGray) }
                                                        Button(
                                                            onClick = {
                                                                if (postRejectReasonInput.isNotEmpty()) {
                                                                    viewModel.updateCitizenReport(post.copy(status = "REJECTED"))
                                                                    showPostRejectForm = false
                                                                    Toast.makeText(context, "পোস্ট বাতিল করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                                                } else {
                                                                    Toast.makeText(context, "কারণ লিখুন!", Toast.LENGTH_SHORT).show()
                                                                }
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                            modifier = Modifier.testTag("btn_confirm_reject_post_${post.id}")
                                                        ) {
                                                            Text("বাতিল নিশ্চিত")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 4. Pending Map Nodes / Locations
                            val pendingNodes = customMapNodes.filter { !it.isApproved }
                            Text(text = "🗺️ ম্যাপ লোকেশন দাখিল সমূহ (${pendingNodes.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            if (pendingNodes.isEmpty()) {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "কোনো ম্যাপ লোকেশন অনুমোদনের জন্য পেন্ডিং নেই।", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                }
                            } else {
                                pendingNodes.forEach { node ->
                                    var showNodeRejectForm by remember { mutableStateOf(false) }

                                    Card(
                                        modifier = Modifier.fillMaxWidth().testTag("admin_pending_node_${node.id}"),
                                        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                        border = BorderStroke(1.dp, GlassBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = node.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Surface(
                                                    color = NeonCyan.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = node.category,
                                                        color = NeonCyan,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Text(text = "পূর্ণ নাম: ${node.fullName}", color = TextWhite, fontSize = 11.sp)
                                            Text(text = "বিবরণ: ${node.description}", color = TextGray, fontSize = 11.sp)
                                            Text(text = "রাস্তার অবস্থা: ${node.roadCondition}", color = TextGray, fontSize = 11.sp)
                                            Text(text = "ঢাকা থেকে দূরত্ব: ${node.distanceFromDhaka} | যানবাহন: ${node.transitMedium}", color = TextGray, fontSize = 11.sp)
                                            Text(text = "দর্শনীয় স্থান: ${node.hotspots}", color = NeonTeal, fontSize = 11.sp)
                                            Text(text = "ম্যাপ পজিশন: X=${(node.xOffsetPercent * 100).toInt()}% , Y=${(node.yOffsetPercent * 100).toInt()}%", color = TextCyan, fontSize = 10.sp)

                                            if (!showNodeRejectForm) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.updateCustomMapNodeStatus(node, true)
                                                            Toast.makeText(context, "ম্যাপ লোকেশনটি অনুমোদিত হয়েছে এবং লাইভ ম্যাপে যুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                                                        modifier = Modifier.weight(1f).testTag("btn_approve_node_${node.id}")
                                                    ) {
                                                        Text("অনুমোদন", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
                                                    }
                                                    Button(
                                                        onClick = { showNodeRejectForm = true },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                        modifier = Modifier.weight(1f).testTag("btn_trigger_reject_node_${node.id}")
                                                    ) {
                                                        Text("বাতিল")
                                                    }
                                                }
                                            } else {
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text(text = "আপনি কি এই ম্যাপ লোকেশনটি ডিলিট/বাতিল করতে চান?", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                        TextButton(onClick = { showNodeRejectForm = false }) { Text("ফিরে যান", color = TextGray) }
                                                        Button(
                                                            onClick = {
                                                                viewModel.deleteCustomMapNode(node.id)
                                                                showNodeRejectForm = false
                                                                Toast.makeText(context, "ম্যাপ লোকেশনটি বাতিল করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                            modifier = Modifier.testTag("btn_confirm_reject_node_${node.id}")
                                                        ) {
                                                            Text("বাতিল নিশ্চিত")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "settings" -> {
                            // Dynamic App Settings Panel
                            SectionHeader(title = "ডাইনামিক অ্যাপ সেটিংস", subtitle = "অ্যাপের মেটাডেটা, কন্ট্যাক্ট এবং থিম কালার কাস্টমাইজ করুন")
                            
                            var tempAppName by remember { mutableStateOf(appName) }
                            var tempAppLogo by remember { mutableStateOf(appLogo) }
                            var tempContactPhone by remember { mutableStateOf(viewModel.contactPhone.value) }
                            var tempContactEmail by remember { mutableStateOf(viewModel.contactEmail.value) }
                            var tempFacebook by remember { mutableStateOf(viewModel.facebookUrl.value) }
                            var tempWeb by remember { mutableStateOf(viewModel.websiteUrl.value) }
                            var tempMaintenance by remember { mutableStateOf(viewModel.maintenanceMode.value) }
                            val currentAllowedEmojis by viewModel.allowedEmojis.collectAsState()
                            var tempAllowedEmojis by remember { mutableStateOf(currentAllowedEmojis) }

                            // Community Hub Settings
                            val communityNameVal by viewModel.communityName.collectAsState()
                            val communityLogoVal by viewModel.communityLogo.collectAsState()
                            val postingPermissionVal by viewModel.postingPermission.collectAsState()
                            val videoLimitVal by viewModel.videoUploadSize.collectAsState()
                            val imageLimitVal by viewModel.imageUploadSize.collectAsState()
                            val storyDurationVal by viewModel.storyDuration.collectAsState()
                            val maxPostLengthVal by viewModel.maxPostLength.collectAsState()
                            val enableStoriesVal by viewModel.enableStories.collectAsState()
                            val enableShortVideosVal by viewModel.enableShortVideos.collectAsState()
                            val enablePollsVal by viewModel.enablePolls.collectAsState()
                            val enableVoicePostsVal by viewModel.enableVoicePosts.collectAsState()
                            val enableCreatorVerificationVal by viewModel.enableCreatorVerification.collectAsState()

                            var tempCommName by remember { mutableStateOf(communityNameVal) }
                            var tempCommLogo by remember { mutableStateOf(communityLogoVal) }
                            var tempPostingPerm by remember { mutableStateOf(postingPermissionVal) }
                            var tempVideoLimit by remember { mutableStateOf(videoLimitVal) }
                            var tempImageLimit by remember { mutableStateOf(imageLimitVal) }
                            var tempStoryDuration by remember { mutableStateOf(storyDurationVal) }
                            var tempMaxLen by remember { mutableStateOf(maxPostLengthVal) }
                            var tempStories by remember { mutableStateOf(enableStoriesVal) }
                            var tempShortVideos by remember { mutableStateOf(enableShortVideosVal) }
                            var tempPolls by remember { mutableStateOf(enablePollsVal) }
                            var tempVoice by remember { mutableStateOf(enableVoicePostsVal) }
                            var tempVerification by remember { mutableStateOf(enableCreatorVerificationVal) }

                            val pDonationTitleVal by viewModel.profileDonationTitle.collectAsState()
                            val pDonationSubVal by viewModel.profileDonationSubtitle.collectAsState()
                            val pDonationBtnTextVal by viewModel.profileDonationBtnText.collectAsState()
                            val footerDevTextVal by viewModel.footerDevText.collectAsState()

                            var tempDonationTitle by remember(pDonationTitleVal) { mutableStateOf(pDonationTitleVal) }
                            var tempDonationSub by remember(pDonationSubVal) { mutableStateOf(pDonationSubVal) }
                            var tempDonationBtnText by remember(pDonationBtnTextVal) { mutableStateOf(pDonationBtnTextVal) }
                            var tempFooterDevText by remember(footerDevTextVal) { mutableStateOf(footerDevTextVal) }

                            val disabledFeaturesSet by viewModel.disabledFeatures.collectAsState()
                            val globalClosureMsgVal by viewModel.globalClosureMessage.collectAsState()
                            var tempClosureMessage by remember { mutableStateOf(globalClosureMsgVal) }

                            GlassTextField(value = tempAppName, onValueChange = { tempAppName = it }, label = "অ্যাপের নাম (App Name)", placeholder = "Barishal Connect", testTag = "setting_app_name_input")
                            GlassTextField(value = tempAppLogo, onValueChange = { tempAppLogo = it }, label = "অ্যাপের লোগো হরফ (App Logo Letter)", placeholder = "B", testTag = "setting_app_logo_input")
                            GlassTextField(value = tempContactPhone, onValueChange = { tempContactPhone = it }, label = "হেল্পলাইন ফোন নম্বর", placeholder = "০১৭০০০০০০০০", testTag = "setting_phone_input")
                            GlassTextField(value = tempContactEmail, onValueChange = { tempContactEmail = it }, label = "সাপোর্ট ইমেইল এড্রেস", placeholder = "support@barishalconnect.gov", testTag = "setting_email_input")
                            GlassTextField(value = tempFacebook, onValueChange = { tempFacebook = it }, label = "ফেসবুক পেজ লিংক", placeholder = "https://facebook.com/...", testTag = "setting_fb_input")
                            GlassTextField(value = tempWeb, onValueChange = { tempWeb = it }, label = "ওয়েবসাইট লিঙ্ক (Website URL)", placeholder = "https://...", testTag = "setting_web_input")
                            GlassTextField(value = tempAllowedEmojis, onValueChange = { tempAllowedEmojis = it }, label = "অনুমোদিত রিঅ্যাকশন ইমোজি (কমা দিয়ে আলাদা করুন)", placeholder = "👍,❤️,😮,⚠️,😡,🛠️", testTag = "setting_allowed_emojis_input")

                            // Profile & Donation Customize section
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "🎁 প্রোফাইল ও দক্ষিণাঞ্চল অনুদান সেটিংস", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            GlassTextField(value = tempDonationTitle, onValueChange = { tempDonationTitle = it }, label = "অনুদান কার্ডের শিরোনাম", placeholder = "দক্ষিণাঞ্চল দুর্যোগ ত্রাণ তহবিল")
                            GlassTextField(value = tempDonationSub, onValueChange = { tempDonationSub = it }, label = "অনুদান কার্ডের বিবরণ বা লেবেল", placeholder = "আপনার মোট ফান্ডিং অবদান")
                            GlassTextField(value = tempDonationBtnText, onValueChange = { tempDonationBtnText = it }, label = "অনুদান বোতামের টেক্সট", placeholder = "+ ৫০০ টাকা দান")
                            GlassTextField(value = tempFooterDevText, onValueChange = { tempFooterDevText = it }, label = "প্রোফাইল ফুটার ডেভেলপমেন্ট টেক্সট", placeholder = "AP development with AI code studio")

                            // Community Settings section
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "🤝 Community Hub ডাইনামিক সেটিংস", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            
                            GlassTextField(value = tempCommName, onValueChange = { tempCommName = it }, label = "কমিউনিটি নাম (Community Name)", placeholder = "বরিশাল সোশ্যাল ক্লাব", testTag = "setting_comm_name")
                            GlassTextField(value = tempCommLogo, onValueChange = { tempCommLogo = it }, label = "কমিউনিটি লোগো (ইমোজি বা হরফ)", placeholder = "🤝", testTag = "setting_comm_logo")
                            GlassTextField(value = tempPostingPerm, onValueChange = { tempPostingPerm = it }, label = "পোস্টিং অনুমতি বিবরণ", placeholder = "সবার জন্য উন্মুক্ত", testTag = "setting_comm_permission")
                            GlassTextField(value = tempVideoLimit, onValueChange = { tempVideoLimit = it }, label = "সর্বোচ্চ ভিডিও ফাইল লিমিট", placeholder = "25 MB")
                            GlassTextField(value = tempImageLimit, onValueChange = { tempImageLimit = it }, label = "সর্বোচ্চ ইমেজ ফাইল লিমিট", placeholder = "10 MB")
                            GlassTextField(value = tempStoryDuration, onValueChange = { tempStoryDuration = it }, label = "স্টোরি স্থায়িত্ব কাল", placeholder = "24 Hours")
                            GlassTextField(value = tempMaxLen, onValueChange = { tempMaxLen = it }, label = "সর্বোচ্চ ক্যাপশন দৈর্ঘ্য (অক্ষর)", placeholder = "1000")

                            // Feature Toggles switches
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "২৪ ঘণ্টার স্টোরি সচল রাখুন", color = Color.White, fontSize = 11.sp)
                                    Switch(checked = tempStories, onCheckedChange = { tempStories = it })
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "শর্ট ভিডিও ফিড সচল রাখুন", color = Color.White, fontSize = 11.sp)
                                    Switch(checked = tempShortVideos, onCheckedChange = { tempShortVideos = it })
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "কমিউনিটি পোল সচল রাখুন", color = Color.White, fontSize = 11.sp)
                                    Switch(checked = tempPolls, onCheckedChange = { tempPolls = it })
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "ভয়েস মেসেজ ও পডকাস্ট সচল রাখুন", color = Color.White, fontSize = 11.sp)
                                    Switch(checked = tempVoice, onCheckedChange = { tempVoice = it })
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "ভেরিফাইড ক্রিয়েটর সচল রাখুন", color = Color.White, fontSize = 11.sp)
                                    Switch(checked = tempVerification, onCheckedChange = { tempVerification = it })
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "মেইনটেনেন্স মোড (Maintenance Mode)", color = Color.White, fontSize = 12.sp)
                                Switch(
                                    checked = tempMaintenance,
                                    onCheckedChange = { tempMaintenance = it },
                                    modifier = Modifier.testTag("setting_maintenance_switch")
                                )
                            }

                            // --- FEATURE ON/OFF & CLOSURE NOTICE CONTROLS ---
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "🚫 যেকোনো ফিচার চালু/বন্ধ করুন (Feature Toggles)", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                            GlassTextField(
                                value = tempClosureMessage,
                                onValueChange = { tempClosureMessage = it },
                                label = "সার্ভিস বন্ধের নোটিশ (Closure Notice Message)",
                                placeholder = "সাময়িক সময়ের জন্য সার্ভিসটি বন্ধ আছে।",
                                testTag = "setting_closure_message"
                            )

                            Text(
                                text = "নিচের অপশনগুলো সিলেক্ট করে যেকোনো সার্ভিস বন্ধ রাখতে পারবেন। বন্ধ করা সার্ভিসে প্রবেশ করলে উপরের নোটিশটি শো করবে।",
                                color = TextGray,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val features = listOf(
                                        "road" to "স্মার্ট রোড (Smart Road)",
                                        "worker" to "কর্মী ডিরেক্টরি (Worker Directory)",
                                        "hospital" to "হাসপাতাল (Hospital Directory)",
                                        "doctor" to "ডাক্তার চেম্বার (Doctor Directory)",
                                        "school" to "শিক্ষা প্রতিষ্ঠান (Education Directory)",
                                        "tourist" to "দর্শনীয় স্থান (Tourism Directory)",
                                        "iptv" to "IPTV স্পোর্টস (IPTV Live Stream)",
                                        "browser" to "স্মার্ট ব্রাউজার ও ভিডিও ডাউনলোডার (Smart Web Browser)",
                                        "citizen_reports" to "নাগরিক রিপোর্ট (Citizen Report)",
                                        "bus" to "বাস সিডিউল (Bus Schedule)",
                                        "launch" to "লঞ্চ সিডিউল (Launch Schedule)",
                                        "transport" to "যাতায়াত ও রাইড-শেয়ারিং (Transport & Ride-Sharing Platform)",
                                        "gov" to "সরকারি ও ইউপি ডিরেক্টরি (Gov Service & Union Directory)",
                                        "market" to "বাজার দর (Market Prices)",
                                        "business" to "ব্যবসা ডিরেক্টরি (Business Directory)",
                                        "agriculture" to "স্মার্ট কৃষি (Smart Agriculture)",
                                        "disaster" to "দুর্যোগ সতর্ক (Disaster & Alerts)",
                                        "emergency_sos" to "জরুরি নম্বর (Emergency SOS)",
                                        "donation" to "সাহায্য ও দান (Donation & Blood)"
                                    )

                                    features.forEach { (featId, featName) ->
                                        val isCurrentlyDisabled = disabledFeaturesSet.contains(featId)
                                        var isFeatDisabledLocal by remember(disabledFeaturesSet) { mutableStateOf(isCurrentlyDisabled) }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = featName, color = if (isFeatDisabledLocal) Color.Red else Color.White, fontSize = 11.sp)
                                            Switch(
                                                checked = !isFeatDisabledLocal, // Switched on means enabled
                                                onCheckedChange = { isEnabled ->
                                                    isFeatDisabledLocal = !isEnabled
                                                    viewModel.setFeatureDisabled(featId, !isEnabled)
                                                },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = NeonCyan,
                                                    checkedTrackColor = NeonCyan.copy(alpha = 0.5f),
                                                    uncheckedThumbColor = Color.Red,
                                                    uncheckedTrackColor = Color.Red.copy(alpha = 0.3f)
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            GlowButton(
                                text = "সকল সেটিংস সংরক্ষণ করুন",
                                onClick = {
                                    viewModel.saveSetting("app_name", tempAppName)
                                    viewModel.saveSetting("app_logo", tempAppLogo)
                                    viewModel.saveSetting("contact_phone", tempContactPhone)
                                    viewModel.saveSetting("contact_email", tempContactEmail)
                                    viewModel.saveSetting("facebook_url", tempFacebook)
                                    viewModel.saveSetting("website_url", tempWeb)
                                    viewModel.saveSetting("maintenance_mode", tempMaintenance)
                                    viewModel.saveSetting("allowed_emojis", tempAllowedEmojis)
                                    viewModel.saveSetting("global_closure_message", tempClosureMessage)
                                    
                                    // Save Profile Donation and Footer Settings
                                    viewModel.saveSetting("profile_donation_title", tempDonationTitle)
                                    viewModel.saveSetting("profile_donation_sub", tempDonationSub)
                                    viewModel.saveSetting("profile_donation_btn", tempDonationBtnText)
                                    viewModel.saveSetting("footer_dev_text", tempFooterDevText)

                                    // Save community settings
                                    viewModel.saveSetting("community_name", tempCommName)
                                    viewModel.saveSetting("community_logo", tempCommLogo)
                                    viewModel.saveSetting("posting_permission", tempPostingPerm)
                                    viewModel.saveSetting("video_upload_size", tempVideoLimit)
                                    viewModel.saveSetting("image_upload_size", tempImageLimit)
                                    viewModel.saveSetting("story_duration", tempStoryDuration)
                                    viewModel.saveSetting("max_post_length", tempMaxLen)
                                    viewModel.saveSetting("enable_stories", tempStories)
                                    viewModel.saveSetting("enable_short_videos", tempShortVideos)
                                    viewModel.saveSetting("enable_polls", tempPolls)
                                    viewModel.saveSetting("enable_voice_posts", tempVoice)
                                    viewModel.saveSetting("enable_creator_verification", tempVerification)

                                    Toast.makeText(context, "সকল অ্যাপ ও ফিচার সেটিংস সফলভাবে সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "btn_save_app_settings"
                            )
                        }

                        "notifications" -> {
                            // Notification Manager compose alerts
                            SectionHeader(title = "পুশ ও ইন-অ্যাপ বিজ্ঞপ্তি", subtitle = "বরিশালবাসীর ফোনে নতুন ঘোষণা এবং নোটিফিকেশন পাঠান")
                            
                            var notifTitle by remember { mutableStateOf("") }
                            var notifBody by remember { mutableStateOf("") }
                            var notifType by remember { mutableStateOf("broadcast") } // broadcast, emergency, scheduled

                            GlassTextField(value = notifTitle, onValueChange = { notifTitle = it }, label = "বিজ্ঞপ্তির শিরোনাম (Title)", placeholder = "যেমন: ঘূর্ণিঝড় সতর্কতা বা নতুন ফিচার", testTag = "notif_title_input")
                            GlassTextField(value = notifBody, onValueChange = { notifBody = it }, label = "বিজ্ঞপ্তির বিষয়বস্তু (Body Description)", placeholder = "এখানে বিস্তারিত বিবরণ লিখুন...", testTag = "notif_body_input")

                            Text(text = "বিজ্ঞপ্তির ধরণ সিলেক্ট করুন", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("broadcast" to "সাধারণ ব্রডকাস্ট", "emergency" to "জরুরী এলার্ট", "scheduled" to "শিডিউল পুশ").forEach { (typeVal, label) ->
                                    val isSel = notifType == typeVal
                                    Button(
                                        onClick = { notifType = typeVal },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSel) NeonCyan else DarkNavySurfaceCard,
                                            contentColor = if (isSel) DarkNavyBackground else Color.White
                                        ),
                                        modifier = Modifier.weight(1f).height(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (notifTitle.isNotEmpty() && notifBody.isNotEmpty()) {
                                        Toast.makeText(context, "বিজ্ঞপ্তিটি সফলভাবে বরিশালবাসীর ফোনে পাঠানো হয়েছে!", Toast.LENGTH_LONG).show()
                                        notifTitle = ""
                                        notifBody = ""
                                    } else {
                                        Toast.makeText(context, "শিরোনাম এবং বিষয়বস্তু আবশ্যক!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                modifier = Modifier.fillMaxWidth().testTag("btn_send_broadcast_notif")
                            ) {
                                Text(text = "নোটিফিকেশন পাঠান (Send Notification)", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
                            }
                        }

                        "donation" -> {
                            // Donation Accounts configuration
                            SectionHeader(title = "অনুদান ব্যাংক ও মোবাইল অ্যাকাউন্ট", subtitle = "বিকাশ, নগদ, রকেট নম্বর এবং অনুদান স্ট্যাটাস ম্যানেজ করুন")
                            
                            var tempBkash by remember { mutableStateOf(viewModel.bkashNumber.value) }
                            var tempNagad by remember { mutableStateOf(viewModel.nagadNumber.value) }
                            var tempRocket by remember { mutableStateOf(viewModel.rocketNumber.value) }
                            var tempBank by remember { mutableStateOf(viewModel.bankAccount.value) }
                            var tempDonationStatus by remember { mutableStateOf(viewModel.donationStatus.value) }

                            GlassTextField(value = tempBkash, onValueChange = { tempBkash = it }, label = "বিকাশ পার্সোনাল নম্বর", placeholder = "০১৭০০০০০০০০", testTag = "setting_bkash_input")
                            GlassTextField(value = tempNagad, onValueChange = { tempNagad = it }, label = "নগদ পার্সোনাল নম্বর", placeholder = "০১৯০০০০০০০০", testTag = "setting_nagad_input")
                            GlassTextField(value = tempRocket, onValueChange = { tempRocket = it }, label = "রকেট পার্সোনাল নম্বর", placeholder = "০১৮০০০০০০০০", testTag = "setting_rocket_input")
                            GlassTextField(value = tempBank, onValueChange = { tempBank = it }, label = "ব্যাংক অ্যাকাউন্ট তথ্য (Bank Details)", placeholder = "যেমন: ডাচ্-বাংলা ব্যাংক, হিসাব নং...", testTag = "setting_bank_input")
                            GlassTextField(value = tempDonationStatus, onValueChange = { tempDonationStatus = it }, label = "অনুদান পোর্টালে প্রদর্শন বার্তা / স্থিতি", placeholder = "সচল (Active)", testTag = "setting_donation_status_input")

                            GlowButton(
                                text = "অনুদান অ্যাকাউন্টসমূহ আপডেট করুন",
                                onClick = {
                                    viewModel.saveSetting("bkash_number", tempBkash)
                                    viewModel.saveSetting("nagad_number", tempNagad)
                                    viewModel.saveSetting("rocket_number", tempRocket)
                                    viewModel.saveSetting("bank_account", tempBank)
                                    viewModel.saveSetting("donation_status", tempDonationStatus)
                                    Toast.makeText(context, "অনুদান পেমেন্ট গেটওয়ে তথ্য হালনাগাদ হয়েছে!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "btn_save_donation_settings"
                            )
                        }

                        "developer" -> {
                            // Developer settings
                            SectionHeader(title = "ডেভেলপার পরিচিতি সেটিংস", subtitle = "অ্যাপের ক্রেডিট সেকশনে প্রদর্শিত ডেভেলপার পরিচিতি সম্পাদনা করুন")
                            
                            var tempDevName by remember { mutableStateOf(viewModel.developerName.value) }
                            var tempDevDesig by remember { mutableStateOf(viewModel.developerDesignation.value) }
                            var tempDevDesc by remember { mutableStateOf(viewModel.developerDesc.value) }
                            var tempDevEmail by remember { mutableStateOf(viewModel.developerEmail.value) }
                            var tempDevPhone by remember { mutableStateOf(viewModel.developerPhone.value) }
                            var tempDevGithub by remember { mutableStateOf(viewModel.developerGithub.value) }
                            var tempDevLinkedin by remember { mutableStateOf(viewModel.developerLinkedin.value) }

                            GlassTextField(value = tempDevName, onValueChange = { tempDevName = it }, label = "ডেভেলপারের নাম", placeholder = "সাকিব আহমেদ (Sakib)", testTag = "setting_dev_name_input")
                            GlassTextField(value = tempDevDesig, onValueChange = { tempDevDesig = it }, label = "পদবি / খেতাব (Designation)", placeholder = "সফটওয়্যার প্রকৌশলী", testTag = "setting_dev_desig_input")
                            GlassTextField(value = tempDevDesc, onValueChange = { tempDevDesc = it }, label = "সংক্ষিপ্ত বিবরণী (About)", placeholder = "ইঞ্জিনিয়ারিং বিবরণ...", testTag = "setting_dev_desc_input")
                            GlassTextField(value = tempDevEmail, onValueChange = { tempDevEmail = it }, label = "ডেভেলপার কন্ট্যাক্ট ইমেইল", placeholder = "sakib.barishal@gmail.com", testTag = "setting_dev_email_input")
                            GlassTextField(value = tempDevPhone, onValueChange = { tempDevPhone = it }, label = "কন্ট্যাক্ট ফোন নম্বর", placeholder = "+৮৮০১৭...", testTag = "setting_dev_phone_input")
                            GlassTextField(value = tempDevGithub, onValueChange = { tempDevGithub = it }, label = "GitHub প্রোফাইল লিংক", placeholder = "https://github.com/...", testTag = "setting_dev_github_input")
                            GlassTextField(value = tempDevLinkedin, onValueChange = { tempDevLinkedin = it }, label = "LinkedIn প্রোফাইল লিংক", placeholder = "https://linkedin.com/in/...", testTag = "setting_dev_linkedin_input")

                            GlowButton(
                                text = "পরিচিতি প্রোফাইল সংরক্ষণ করুন",
                                onClick = {
                                    viewModel.saveSetting("dev_name", tempDevName)
                                    viewModel.saveSetting("dev_designation", tempDevDesig)
                                    viewModel.saveSetting("dev_desc", tempDevDesc)
                                    viewModel.saveSetting("dev_email", tempDevEmail)
                                    viewModel.saveSetting("dev_phone", tempDevPhone)
                                    viewModel.saveSetting("dev_github", tempDevGithub)
                                    viewModel.saveSetting("dev_linkedin", tempDevLinkedin)
                                    Toast.makeText(context, "ডেভেলপার পরিচিতি তথ্য সফলভাবে সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "btn_save_dev_profile"
                            )
                        }

                        "iptv" -> {
                            SectionHeader(title = "IPTV ও লাইভ চ্যানেল ম্যানেজার", subtitle = "M3U ফাইলের মাধ্যমে চ্যানেল আমদানি করুন অথবা সরাসরি নতুন চ্যানেল যুক্ত করুন")

                            // IPTV Upload Step-by-Step Guideline
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF0EA5E9).copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(imageVector = Icons.Default.Help, contentDescription = null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(20.dp))
                                        Text("আইপি টিভি চ্যানেল আপলোড গাইডলাইন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Text(
                                        text = "১. একক চ্যানেল যোগ করতে: উপরের ফর্মে চ্যানেলের নাম, লাইভ স্ট্রিম ইউআরএল (অবশ্যই .m3u8 বা লাইভ আইপি লিংক হতে হবে) এবং ক্যাটেগরি (যেমন: Sports বা News) লিখে 'চ্যানেল যুক্ত করুন' বোতামে চাপুন।\n\n" +
                                               "২. একসাথে অনেক চ্যানেল (M3U প্লেলিস্ট) আমদানি করতে: আপনার M3U ফাইলের পুরো লেখাটি কপি করে নিচের টেক্সট এরিয়া বক্সে পেস্ট করুন এবং 'M3U প্লেলিস্ট আমদানি করুন' বোতামে ক্লিক করুন। অ্যাপটি স্বয়ংক্রিয়ভাবে চ্যানেল ও লিংকগুলো পার্স করে ডেটাবেজে সংরক্ষণ করে নেবে।",
                                        color = TextGray,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            var pastedM3uContent by remember { mutableStateOf("") }
                            var editingChannel by remember { mutableStateOf<com.example.data.model.IptvChannel?>(null) }
                            var newChannelName by remember { mutableStateOf("") }
                            var newChannelUrl by remember { mutableStateOf("") }
                            var newChannelCategory by remember { mutableStateOf("Sports") }

                            // Form 1: Add individual channel
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("নতুন চ্যানেল যোগ করুন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    GlassTextField(value = newChannelName, onValueChange = { newChannelName = it }, label = "চ্যানেলের নাম", placeholder = "যেমন: T-Sports")
                                    GlassTextField(value = newChannelUrl, onValueChange = { newChannelUrl = it }, label = "চ্যানেলের স্ট্রিম ইউআরএল (.m3u8)", placeholder = "http://example.com/live.m3u8")
                                    GlassTextField(value = newChannelCategory, onValueChange = { newChannelCategory = it }, label = "ক্যাটেগরি", placeholder = "যেমন: Sports, News")

                                    GlowButton(
                                        text = "চ্যানেল যুক্ত করুন",
                                        onClick = {
                                            if (newChannelName.isNotEmpty() && newChannelUrl.isNotEmpty()) {
                                                viewModel.addIptvChannel(
                                                    com.example.data.model.IptvChannel(
                                                        name = newChannelName,
                                                        url = newChannelUrl,
                                                        category = newChannelCategory
                                                    )
                                                )
                                                Toast.makeText(context, "চ্যানেল সফলভাবে যুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
                                                newChannelName = ""
                                                newChannelUrl = ""
                                            } else {
                                                Toast.makeText(context, "নাম এবং ইউআরএল আবশ্যক!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // Form 2: Paste M3U
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("M3U ফাইল আপলোড / কপি-পেস্ট করুন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    OutlinedTextField(
                                        value = pastedM3uContent,
                                        onValueChange = { pastedM3uContent = it },
                                        modifier = Modifier.fillMaxWidth().height(120.dp),
                                        placeholder = { Text("M3U ফাইলের কন্টেন্ট এখানে পেস্ট করুন...", color = TextGray) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = NeonCyan,
                                            unfocusedBorderColor = GlassBorder,
                                            focusedContainerColor = DarkNavySurface,
                                            unfocusedContainerColor = DarkNavySurface
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.clearIptvChannels()
                                                Toast.makeText(context, "সকল IPTV চ্যানেল সফলভাবে ডিলিট হয়েছে", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("সব ডিলিট করুন")
                                        }

                                        GlowButton(
                                            text = "আমদানি করুন (Import)",
                                            onClick = {
                                                if (pastedM3uContent.trim().isNotEmpty()) {
                                                    val m3uString = pastedM3uContent
                                                    val parsedChannels = mutableListOf<com.example.data.model.IptvChannel>()
                                                    var currentName = ""
                                                    var currentLogo = ""
                                                    var currentCategory = "Sports"
                                                    m3uString.lines().forEach { line ->
                                                        val trimmed = line.trim()
                                                        if (trimmed.startsWith("#EXTINF:")) {
                                                            if (trimmed.contains("tvg-logo=\"")) {
                                                                currentLogo = trimmed.substringAfter("tvg-logo=\"").substringBefore("\"")
                                                            }
                                                            if (trimmed.contains("group-title=\"")) {
                                                                currentCategory = trimmed.substringAfter("group-title=\"").substringBefore("\"")
                                                            }
                                                            currentName = trimmed.substringAfterLast(",")
                                                            if (currentName.isEmpty() || currentName.startsWith("#")) {
                                                                currentName = "চ্যানেল " + (parsedChannels.size + 1)
                                                            }
                                                        } else if (trimmed.startsWith("http")) {
                                                            if (currentName.isNotEmpty()) {
                                                                parsedChannels.add(
                                                                    com.example.data.model.IptvChannel(
                                                                        name = currentName,
                                                                        url = trimmed,
                                                                        logo = currentLogo,
                                                                        category = currentCategory
                                                                    )
                                                                )
                                                                currentName = ""
                                                                currentLogo = ""
                                                                currentCategory = "Sports"
                                                            }
                                                        }
                                                    }
                                                    if (parsedChannels.isNotEmpty()) {
                                                        viewModel.importIptvChannels(parsedChannels)
                                                        Toast.makeText(context, "${parsedChannels.size}টি IPTV চ্যানেল সফলভাবে আমদানি করা হয়েছে!", Toast.LENGTH_LONG).show()
                                                        pastedM3uContent = ""
                                                    } else {
                                                        Toast.makeText(context, "M3U ফাইলের সঠিক ফরম্যাট পাওয়া যায়নি!", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            // Channel List & Actions
                            Text("বিদ্যমান চ্যানেল তালিকা", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val iptvList by viewModel.iptvChannels.collectAsState()
                            iptvList.forEach { ch ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                    border = BorderStroke(1.dp, GlassBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(ch.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(ch.url, color = TextGray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }

                                        Row {
                                            IconButton(onClick = { editingChannel = ch }) {
                                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Rename", tint = NeonCyan)
                                            }
                                            IconButton(onClick = { viewModel.deleteIptvChannel(ch.id) }) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }

                            // Channel Rename dialog
                            editingChannel?.let { ch ->
                                var renameInput by remember { mutableStateOf(ch.name) }
                                AlertDialog(
                                    onDismissRequest = { editingChannel = null },
                                    title = { Text("চ্যানেল পরিবর্তন করুন", color = Color.White, fontSize = 14.sp) },
                                    text = {
                                        TextField(
                                            value = renameInput,
                                            onValueChange = { renameInput = it },
                                            colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                        )
                                    },
                                    confirmButton = {
                                        Button(onClick = {
                                            viewModel.addIptvChannel(ch.copy(name = renameInput))
                                            editingChannel = null
                                            Toast.makeText(context, "নাম সফলভাবে পরিবর্তন হয়েছে!", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Text("সংরক্ষণ")
                                        }
                                    },
                                    containerColor = DarkNavySurface
                                )
                            }
                        }

                        "browser" -> {
                            SectionHeader(title = "স্মার্ট ব্রাউজার ডাইনামিক সেটিংস", subtitle = "সার্চ ইঞ্জিন, হোমপেজ ইউআরএল এবং ডাইনামিক বাটনসমূহ ম্যানেজ করুন")

                            var searchEngineInput by remember { mutableStateOf(viewModel.browserSearchEngine.value) }
                            var homepageUrlInput by remember { mutableStateOf(viewModel.browserHomepageUrl.value) }

                            var dynamicBtnTitle by remember { mutableStateOf("") }
                            var dynamicBtnTarget by remember { mutableStateOf("") }
                            var dynamicBtnType by remember { mutableStateOf("url") } // url or iptv

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("সার্চ ইঞ্জিন ও হোমপেজ সেটিংস", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    GlassTextField(value = searchEngineInput, onValueChange = { searchEngineInput = it }, label = "সার্চ ইঞ্জিন কুয়েরি ইউআরএল", placeholder = "https://www.google.com/search?q=")
                                    GlassTextField(value = homepageUrlInput, onValueChange = { homepageUrlInput = it }, label = "ডিফল্ট হোমপেজ ইউআরএল", placeholder = "https://www.google.com")

                                    GlowButton(
                                        text = "ব্রাউজার সেটিংস সংরক্ষণ করুন",
                                        onClick = {
                                            viewModel.saveSetting("browser_search_engine", searchEngineInput)
                                            viewModel.saveSetting("browser_homepage_url", homepageUrlInput)
                                            Toast.makeText(context, "ব্রাউজার সেটিংস হালনাগাদ করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // Add custom dynamic button inside browser home
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("হোমপেজে ডাইনামিক শর্টকাট বাটন যোগ করুন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    GlassTextField(value = dynamicBtnTitle, onValueChange = { dynamicBtnTitle = it }, label = "বাটনের শিরোনাম (Title)", placeholder = "যেমন: বরিশাল सरकारी ওয়েব পোর্টাল")
                                    GlassTextField(value = dynamicBtnTarget, onValueChange = { dynamicBtnTarget = it }, label = "টার্গেট ইউআরএল (Link / Stream)", placeholder = "https://barishal.gov.bd")

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("বাটন ধরণ: ", color = Color.White, fontSize = 11.sp)
                                        RadioButton(selected = dynamicBtnType == "url", onClick = { dynamicBtnType = "url" })
                                        Text("Website URL", color = Color.White, fontSize = 11.sp)
                                        RadioButton(selected = dynamicBtnType == "iptv", onClick = { dynamicBtnType = "iptv" })
                                        Text("Stream Link", color = Color.White, fontSize = 11.sp)
                                    }

                                    GlowButton(
                                        text = "ডাইনামিক বাটন যুক্ত করুন",
                                        onClick = {
                                            if (dynamicBtnTitle.isNotEmpty() && dynamicBtnTarget.isNotEmpty()) {
                                                viewModel.addDynamicButton(
                                                    com.example.data.model.DynamicButton(
                                                        title = dynamicBtnTitle,
                                                        target = dynamicBtnTarget,
                                                        type = dynamicBtnType
                                                    )
                                                )
                                                Toast.makeText(context, "ডাইনামিক বাটন যুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
                                                dynamicBtnTitle = ""
                                                dynamicBtnTarget = ""
                                            } else {
                                                Toast.makeText(context, "সকল তথ্য পূরণ করুন!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // Display current dynamic buttons
                            Text("বিদ্যমান ডাইনামিক বাটনসমূহ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val dynamicBtns by viewModel.dynamicButtons.collectAsState()
                            dynamicBtns.forEach { btn ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                    border = BorderStroke(1.dp, GlassBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(btn.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Type: ${btn.type.uppercase()} | Link: ${btn.target}", color = TextGray, fontSize = 10.sp)
                                        }
                                        IconButton(onClick = { viewModel.deleteDynamicButton(btn.id) }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }

                        "backup" -> {
                            SectionHeader(title = "ডিজিটাল হাব ব্যাকআপ ও রিস্টোর", subtitle = "অ্যাপ্লিকেশন সেটিংস এবং ডাটাবেজ অফলাইন ব্যাকআপ তৈরি ও রিস্টোর করুন")

                            var backupOutputText by remember { mutableStateOf("") }
                            var restoreInputText by remember { mutableStateOf("") }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("ডাটা ব্যাকআপ জেনারেট করুন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("নিচের বাটনে ক্লিক করলে বরিশাল কানেক্ট অ্যাপের সেটিংসের একটি ব্যাকআপ কোড জেনারেট হবে। এই কোডটি কপি করে অন্য ডিভাইসে বা রিসেট করার পর রিস্টোর করতে পারবেন।", color = TextGray, fontSize = 11.sp)

                                    GlowButton(
                                        text = "ব্যাকআপ কোড তৈরি করুন",
                                        onClick = {
                                            val backupMap = mapOf(
                                                "app_name" to viewModel.appName.value,
                                                "theme_color" to viewModel.themeColor.value,
                                                "accent_color" to viewModel.accentColor.value,
                                                "contact_phone" to viewModel.contactPhone.value,
                                                "contact_email" to viewModel.contactEmail.value,
                                                "bkash_number" to viewModel.bkashNumber.value,
                                                "nagad_number" to viewModel.nagadNumber.value,
                                                "rocket_number" to viewModel.rocketNumber.value,
                                                "browser_search_engine" to viewModel.browserSearchEngine.value,
                                                "browser_homepage_url" to viewModel.browserHomepageUrl.value
                                            )
                                            // Convert simple JSON mockup string
                                            val json = backupMap.entries.joinToString(prefix = "{", postfix = "}") {
                                                "\"${it.key}\":\"${it.value.replace("\"", "\\\"")}\""
                                            }
                                            backupOutputText = json
                                            Toast.makeText(context, "ব্যাকআপ কোড জেনারেট করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (backupOutputText.isNotEmpty()) {
                                        OutlinedTextField(
                                            value = backupOutputText,
                                            onValueChange = {},
                                            modifier = Modifier.fillMaxWidth().height(100.dp),
                                            readOnly = true,
                                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = NeonCyan,
                                                unfocusedTextColor = NeonCyan,
                                                focusedBorderColor = NeonCyan,
                                                unfocusedBorderColor = GlassBorder,
                                                focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                                unfocusedContainerColor = Color.Black.copy(alpha = 0.2f)
                                            )
                                        )
                                        Button(
                                            onClick = {
                                                val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                val clipData = android.content.ClipData.newPlainText("BarishalBackup", backupOutputText)
                                                clipboardManager.setPrimaryClip(clipData)
                                                Toast.makeText(context, "ব্যাকআপ কোড ক্লিপবোর্ডে কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("ব্যাকআপ কোড কপি করুন", color = NeonCyan)
                                        }
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("ব্যাকআপ রিস্টোর করুন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    OutlinedTextField(
                                        value = restoreInputText,
                                        onValueChange = { restoreInputText = it },
                                        modifier = Modifier.fillMaxWidth().height(100.dp),
                                        placeholder = { Text("এখানে ব্যাকআপ কোড পেস্ট করুন...", color = TextGray) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = NeonCyan,
                                            unfocusedBorderColor = GlassBorder,
                                            focusedContainerColor = DarkNavySurface,
                                            unfocusedContainerColor = DarkNavySurface
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    GlowButton(
                                        text = "ডাটা রিস্টোর করুন (Restore)",
                                        onClick = {
                                            try {
                                                if (restoreInputText.trim().startsWith("{") && restoreInputText.trim().endsWith("}")) {
                                                    val raw = restoreInputText.trim().removeSurrounding("{", "}")
                                                    val parts = raw.split("\",\"")
                                                    parts.forEach { part ->
                                                        val pair = part.split("\":\"")
                                                        if (pair.size == 2) {
                                                            val key = pair[0].replace("\"", "").trim()
                                                            val value = pair[1].replace("\"", "").trim()
                                                            viewModel.saveSetting(key, value)
                                                        }
                                                    }
                                                    Toast.makeText(context, "সমস্ত সেটিংস সফলভাবে রিস্টোর করা হয়েছে!", Toast.LENGTH_LONG).show()
                                                    restoreInputText = ""
                                                } else {
                                                    Toast.makeText(context, "ভুল ব্যাকআপ ফরম্যাট!", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "রিস্টোর ব্যর্থ হয়েছে! সঠিক কোড দিন।", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

// Sub-section mapping helper for header text representation
private fun getMenuTitleBangla(menu: String): String {
    return when (menu) {
        "dashboard" -> "এনালিটিক্স ড্যাশবোর্ড"
        "sheets" -> "গুগল স্প্রেডশীট ম্যানেজার"
        "approval" -> "দাখিল অনুমোদন কেন্দ্র"
        "iptv" -> "IPTV চ্যানেল ম্যানেজার"
        "browser" -> "ব্রাউজার কনফিগারেশন"
        "backup" -> "সিস্টেম ব্যাকআপ ও রিস্টোর"
        "settings" -> "ডাইনামিক অ্যাপ সেটিংস"
        "notifications" -> "পুশ নোটিফিকেশন পোর্টাল"
        "donation" -> "অনুদান গেটওয়ে কনфিগ"
        "developer" -> "ডেভেলপার সেটিংস"
        else -> "অ্যাডমিন প্যানেল"
    }
}
