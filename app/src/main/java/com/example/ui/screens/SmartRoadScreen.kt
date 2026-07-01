package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SmartRoad
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartRoadScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    var currentScreen by remember { mutableStateOf("DASHBOARD") }
    var selectedRoad by remember { mutableStateOf<SmartRoad?>(null) }
    
    // For Add Road mapping simulation
    var isTracking by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var trackingTime by remember { mutableStateOf(0) } // seconds
    var trackingDistance by remember { mutableStateOf(0.0) } // km
    var trackingCoordinates = remember { mutableStateListOf<Offset>() }
    
    // Navigation simulation
    var isNavigating by remember { mutableStateOf(false) }
    var navProgress by remember { mutableStateOf(0f) }
    var wrongRouteSimulated by remember { mutableStateOf(false) }
    var voiceInstruction by remember { mutableStateOf("১০০ মিটার সামনে গিয়ে সোজা এগিয়ে চলুন") }

    // Navigation trigger loop
    LaunchedEffect(isTracking, isPaused) {
        if (isTracking && !isPaused) {
            while (isTracking && !isPaused) {
                delay(1000)
                trackingTime += 1
                trackingDistance += 0.002 + (0.001 * (0..3).random())
                
                // Add coordinates dynamically for line drawing
                val lastPoint = trackingCoordinates.lastOrNull() ?: Offset(100f, 400f)
                val nextX = lastPoint.x + (-15..25).random().toFloat()
                val nextY = lastPoint.y - (5..20).random().toFloat()
                trackingCoordinates.add(Offset(nextX, nextY))
            }
        }
    }

    LaunchedEffect(isNavigating, wrongRouteSimulated) {
        if (isNavigating) {
            while (isNavigating) {
                delay(1200)
                if (!wrongRouteSimulated) {
                    navProgress = (navProgress + 0.05f).coerceAtMost(1f)
                    voiceInstruction = when {
                        navProgress < 0.2f -> "১০০ মিটার সামনে গিয়ে সোজা এগিয়ে চলুন"
                        navProgress < 0.4f -> "কীর্তনখোলা ব্রিজের উপর উঠুন এবং ৫০ কিমি বেগে ড্রাইভ করুন"
                        navProgress < 0.6f -> "সামনে বামে মোড় নিয়ে রূপাতলী বাস টার্মিনালের দিকে যান"
                        navProgress < 0.8f -> "নিরাপদ দূরত্ব বজায় রাখুন, সড়ক এখন ফাঁকা"
                        navProgress >= 1f -> "অভিনন্দন! আপনি গন্তব্যে পৌঁছে গেছেন।"
                        else -> "সড়ক নির্দেশনা অনুযায়ী চলুন"
                    }
                } else {
                    voiceInstruction = "সতর্কতা! রুট বিচ্যুতি সনাক্ত হয়েছে! অনুগ্রহ করে বামে ঘুরুন এবং পূর্বের সড়কে ফিরে যান।"
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentScreen) {
                            "DASHBOARD" -> "স্মার্ট সড়ক ও ম্যাপিং"
                            "SEARCH" -> "সড়ক অনুসন্ধান"
                            "ADD_ROAD" -> "নতুন সড়ক ট্র্যাকিং"
                            "ROAD_FORM" -> "সড়ক বিবরণ ফর্ম"
                            "MY_ROADS" -> "আমার সড়কসমূহ"
                            "STATS" -> "সড়ক পরিসংখ্যান"
                            "DETAILS" -> "সড়কের বিস্তারিত"
                            "NAVIGATION" -> "লাইভ নেভিগেশন"
                            else -> "স্মার্ট সড়ক মডিউল"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            when (currentScreen) {
                                "DASHBOARD" -> onBack()
                                "DETAILS" -> currentScreen = "SEARCH"
                                "NAVIGATION" -> {
                                    isNavigating = false
                                    currentScreen = "DETAILS"
                                }
                                "ROAD_FORM" -> currentScreen = "ADD_ROAD"
                                else -> currentScreen = "DASHBOARD"
                            }
                        },
                        modifier = Modifier.testTag("smart_road_back")
                    ) {
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
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    "DASHBOARD" -> RoadDashboardView(
                        viewModel = viewModel,
                        onNavigate = { currentScreen = it }
                    )
                    "SEARCH" -> RoadSearchView(
                        viewModel = viewModel,
                        onSelectRoad = { road ->
                            selectedRoad = road
                            currentScreen = "DETAILS"
                        }
                    )
                    "ADD_ROAD" -> RoadAddTrackingView(
                        isTracking = isTracking,
                        isPaused = isPaused,
                        trackingTime = trackingTime,
                        trackingDistance = trackingDistance,
                        trackingCoordinates = trackingCoordinates,
                        onStart = {
                            isTracking = true
                            isPaused = false
                            trackingTime = 0
                            trackingDistance = 0.0
                            trackingCoordinates.clear()
                            trackingCoordinates.add(Offset(150f, 400f))
                        },
                        onPauseResume = { isPaused = !isPaused },
                        onFinish = {
                            isTracking = false
                            currentScreen = "ROAD_FORM"
                        }
                    )
                    "ROAD_FORM" -> RoadFormView(
                        viewModel = viewModel,
                        distance = trackingDistance,
                        duration = trackingTime,
                        coordinates = trackingCoordinates,
                        onSubmit = {
                            currentScreen = "MY_ROADS"
                        }
                    )
                    "MY_ROADS" -> MyRoadsView(
                        viewModel = viewModel,
                        onSelectRoad = { road ->
                            selectedRoad = road
                            currentScreen = "DETAILS"
                        }
                    )
                    "STATS" -> RoadStatsView(viewModel = viewModel)
                    "DETAILS" -> RoadDetailsView(
                        viewModel = viewModel,
                        road = selectedRoad,
                        onStartNavigation = {
                            isNavigating = true
                            navProgress = 0f
                            wrongRouteSimulated = false
                            currentScreen = "NAVIGATION"
                        }
                    )
                    "NAVIGATION" -> NavigationActiveView(
                        road = selectedRoad,
                        progress = navProgress,
                        voiceInstruction = voiceInstruction,
                        wrongRouteSimulated = wrongRouteSimulated,
                        onToggleWrongRoute = { wrongRouteSimulated = !wrongRouteSimulated },
                        onReturnToRoute = { wrongRouteSimulated = false }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1. DASHBOARD VIEW
// -------------------------------------------------------------
@Composable
fun RoadDashboardView(
    viewModel: BarishalViewModel,
    onNavigate: (String) -> Unit
) {
    val allRoads by viewModel.allSmartRoads.collectAsState()
    val approvedRoads = allRoads.filter { it.status == "APPROVED" }
    val totalDist = approvedRoads.sumOf { it.distance }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
                .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.CompassCalibration, contentDescription = null, tint = NeonCyan)
                    Text(text = "Barishal GPS Mapping", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                
                Text(
                    text = "স্মার্ট সড়ক ও লাইভ জিপিএস ট্র্যাকিং সিস্টেম",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "বরিশাল বিভাগের যেকোনো নতুন সড়ক হেঁটে বা সাইকেল চালিয়ে জিপিএস পয়েন্ট সংগ্রহ করুন, ডাটাবেজে সাবমিট করুন এবং লাইভ নেভিগেশন উপভোগ করুন।",
                    color = TextWhite,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // 4 Large Core Action Buttons Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardSquareButton(
                    title = "সড়ক খুঁজুন",
                    subtitle = "Road Search",
                    icon = Icons.Default.Search,
                    gradientStart = NeonCyan,
                    gradientEnd = ElectricBlue,
                    testTag = "btn_road_search",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("SEARCH") }
                )

                DashboardSquareButton(
                    title = "নতুন সড়ক ম্যাপিং",
                    subtitle = "Add Road Tracking",
                    icon = Icons.Default.AddLocationAlt,
                    gradientStart = NeonTeal,
                    gradientEnd = NeonCyan,
                    testTag = "btn_add_road",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("ADD_ROAD") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardSquareButton(
                    title = "আমার সড়কসমূহ",
                    subtitle = "My Mapped Roads",
                    icon = Icons.Default.ListAlt,
                    gradientStart = ElectricBlue,
                    gradientEnd = Color(0xFF6366F1),
                    testTag = "btn_my_roads",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("MY_ROADS") }
                )

                DashboardSquareButton(
                    title = "সড়ক পরিসংখ্যান",
                    subtitle = "Road Statistics",
                    icon = Icons.Default.BarChart,
                    gradientStart = Color(0xFFEC4899),
                    gradientEnd = Color(0xFFF43F5E),
                    testTag = "btn_road_stats",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("STATS") }
                )
            }
        }

        // Statistics mini dashboard card
        SectionHeader(title = "সংক্ষিপ্ত ডেটাবেজ রিপোর্ট", subtitle = "বিভাগীয় রিয়েল-টাইম ট্র্যাকিং তথ্য")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatMiniCard(
                label = "মোট সড়ক",
                value = "${approvedRoads.size} টি",
                icon = Icons.Default.Traffic,
                color = NeonCyan,
                modifier = Modifier.weight(1f)
            )
            StatMiniCard(
                label = "মোট দূরত্ব",
                value = String.format("%.1f কিমি", totalDist),
                icon = Icons.Default.TrendingUp,
                color = NeonTeal,
                modifier = Modifier.weight(1f)
            )
            StatMiniCard(
                label = "কন্ট্রিবিউটর",
                value = "${allRoads.map { it.contributor }.distinct().size} জন",
                icon = Icons.Default.People,
                color = ElectricBlue,
                modifier = Modifier.weight(1f)
            )
        }

        // Live Ticker info banner
        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "লাইভ কমিউনিটি আপডেট", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = "বর্ষার সময় নথুল্লাবাদ রোড ও রূপাতলী এলাকায় জলাবদ্ধতা এড়াতে কন্ট্রিবিউটরদের আপডেটকৃত রুট ম্যাপ অনুসরণ করুন।", color = TextWhite, fontSize = 10.sp, lineHeight = 13.sp)
                }
            }
        }
    }
}

@Composable
fun DashboardSquareButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientStart: Color,
    gradientEnd: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GlassBorder),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background subtle gradient corner
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(bottomStart = 80.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(gradientStart.copy(alpha = 0.2f), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(gradientStart, gradientEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = subtitle, color = TextCyan.copy(alpha = 0.7f), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun StatMiniCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = DarkNavySurfaceCard,
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = label, color = TextGray, fontSize = 9.sp)
        }
    }
}

// -------------------------------------------------------------
// 2. ROAD SEARCH VIEW
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoadSearchView(
    viewModel: BarishalViewModel,
    onSelectRoad: (SmartRoad) -> Unit
) {
    val allRoads by viewModel.allSmartRoads.collectAsState()
    val approvedRoads = allRoads.filter { it.status == "APPROVED" }

    var searchNameQuery by remember { mutableStateOf("") }
    var selectedDistrict by remember { mutableStateOf("All") }
    var selectedType by remember { mutableStateOf("All") }
    var selectedCondition by remember { mutableStateOf("All") }

    val districts = listOf("All", "Barishal", "Jhalakathi", "Patuakhali", "Bhola")
    val roadTypes = listOf("All", "Paved Road", "Brick Road", "Dirt Road", "Semi Paved")
    val roadConditions = listOf("All", "Excellent", "Good", "Damaged", "Under Construction")

    val filteredRoads = remember(approvedRoads, searchNameQuery, selectedDistrict, selectedType, selectedCondition) {
        approvedRoads.filter { road ->
            val matchesName = road.name.contains(searchNameQuery, ignoreCase = true) ||
                    road.description.contains(searchNameQuery, ignoreCase = true)
            val matchesDistrict = selectedDistrict == "All" || road.district.equals(selectedDistrict, ignoreCase = true)
            val matchesType = selectedType == "All" || road.category.equals(selectedType, ignoreCase = true)
            val matchesCondition = selectedCondition == "All" || road.condition.equals(selectedCondition, ignoreCase = true)
            matchesName && matchesDistrict && matchesType && matchesCondition
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "সড়ক অনুসন্ধান ফিল্টারসমূহ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)

        // Text Search input
        OutlinedTextField(
            value = searchNameQuery,
            onValueChange = { searchNameQuery = it },
            placeholder = { Text("সড়কের নাম দিয়ে খুঁজুন...", color = TextGray, fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("road_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = GlassBorder,
                focusedContainerColor = DarkNavySurfaceCard,
                unfocusedContainerColor = DarkNavySurfaceCard
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Quick Filters horizontally
        Text(text = "জেলা ফিল্টার করুন:", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            districts.forEach { dist ->
                val isSelected = selectedDistrict == dist
                SuggestionChip(
                    onClick = { selectedDistrict = dist },
                    label = { Text(text = if (dist == "All") "সকল জেলা" else dist, fontSize = 10.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isSelected) NeonCyan.copy(alpha = 0.2f) else DarkNavySurfaceCard,
                        labelColor = if (isSelected) NeonCyan else Color.White
                    ),
                    border = BorderStroke(1.dp, if (isSelected) NeonCyan else GlassBorder),
                    modifier = Modifier.testTag("filter_dist_$dist")
                )
            }
        }

        Text(text = "সড়কের ধরন:", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            roadTypes.forEach { rType ->
                val isSelected = selectedType == rType
                SuggestionChip(
                    onClick = { selectedType = rType },
                    label = { Text(text = if (rType == "All") "সব ধরন" else rType, fontSize = 10.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isSelected) NeonTeal.copy(alpha = 0.2f) else DarkNavySurfaceCard,
                        labelColor = if (isSelected) NeonTeal else Color.White
                    ),
                    border = BorderStroke(1.dp, if (isSelected) NeonTeal else GlassBorder),
                    modifier = Modifier.testTag("filter_type_$rType")
                )
            }
        }

        Text(text = "সড়কের অবস্থা:", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            roadConditions.forEach { cond ->
                val isSelected = selectedCondition == cond
                SuggestionChip(
                    onClick = { selectedCondition = cond },
                    label = { Text(text = if (cond == "All") "সকল অবস্থা" else cond, fontSize = 10.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isSelected) ElectricBlue.copy(alpha = 0.2f) else DarkNavySurfaceCard,
                        labelColor = if (isSelected) ElectricBlue else Color.White
                    ),
                    border = BorderStroke(1.dp, if (isSelected) ElectricBlue else GlassBorder),
                    modifier = Modifier.testTag("filter_cond_$cond")
                )
            }
        }

        Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 4.dp))

        Text(
            text = "খুঁজে পাওয়া গেছে: ${filteredRoads.size} টি সড়ক",
            color = NeonCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        // Result list
        if (filteredRoads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TextGray, modifier = Modifier.size(48.dp))
                    Text(text = "কোনো সড়ক খুঁজে পাওয়া যায়নি। ফিল্টার পরিবর্তন করে চেষ্টা করুন।", color = TextGray, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("road_search_results"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredRoads) { road ->
                    RoadItemRow(road = road, onClick = { onSelectRoad(road) })
                }
            }
        }
    }
}

@Composable
fun RoadItemRow(
    road: SmartRoad,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("road_item_card_${road.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
        border = BorderStroke(1.dp, GlassBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = road.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                
                // Road category tag
                Surface(
                    color = when (road.category) {
                        "Paved Road" -> NeonTeal.copy(alpha = 0.15f)
                        "Brick Road" -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                        "Dirt Road" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                        else -> ElectricBlue.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = road.category,
                        color = when (road.category) {
                            "Paved Road" -> NeonTeal
                            "Brick Road" -> Color(0xFFF59E0B)
                            "Dirt Road" -> Color(0xFFEF4444)
                            else -> ElectricBlue
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = road.description,
                color = TextWhite,
                fontSize = 11.sp,
                maxLines = 2
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.DirectionsWalk, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(12.dp))
                        Text(text = String.format("%.1f km", road.distance), color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = TextCyan, modifier = Modifier.size(12.dp))
                        Text(text = "${road.durationSeconds / 60} min", color = TextCyan, fontSize = 11.sp)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = when (road.condition) {
                        "Excellent" -> NeonTeal
                        "Good" -> NeonCyan
                        else -> Color(0xFFF59E0B)
                    }, modifier = Modifier.size(12.dp))
                    Text(text = road.condition, color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. ROAD TRACKING (ADD_ROAD) VIEW
// -------------------------------------------------------------
@Composable
fun RoadAddTrackingView(
    isTracking: Boolean,
    isPaused: Boolean,
    trackingTime: Int,
    trackingDistance: Double,
    trackingCoordinates: List<Offset>,
    onStart: () -> Unit,
    onPauseResume: () -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    var showingPermissionRequest by remember { mutableStateOf(true) }
    var gpsCoordinatesText by remember { mutableStateOf("জিপিএস কানেক্ট হচ্ছে...") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fine || coarse) {
            hasPermission = true
        } else {
            showingPermissionRequest = false
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                val provider = if (isGpsEnabled) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
                
                if (isGpsEnabled || isNetworkEnabled) {
                    val lastKnown = locationManager.getLastKnownLocation(provider)
                    if (lastKnown != null) {
                        gpsCoordinatesText = String.format("Lat: %.5f, Lng: %.5f", lastKnown.latitude, lastKnown.longitude)
                    }
                    
                    val listener = object : LocationListener {
                        override fun onLocationChanged(loc: Location) {
                            gpsCoordinatesText = String.format("Lat: %.5f, Lng: %.5f", loc.latitude, loc.longitude)
                        }
                        override fun onStatusChanged(p: String?, status: Int, extras: Bundle?) {}
                        override fun onProviderEnabled(p: String) {}
                        override fun onProviderDisabled(p: String) {}
                    }
                    locationManager.requestLocationUpdates(provider, 2000L, 1f, listener)
                } else {
                    gpsCoordinatesText = "মোবাইলের GPS বন্ধ রয়েছে!"
                }
            } catch (e: SecurityException) {
                gpsCoordinatesText = "লোকেশন পারমিশন ত্রুটি!"
            } catch (e: Exception) {
                gpsCoordinatesText = "জিপিএস সক্রিয় করা যাচ্ছে না!"
            }
        }
    }

    if (!hasPermission) {
        if (showingPermissionRequest) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(56.dp))
                        Text(text = "জিপিএস লোকেশন পারমিশন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
                        Text(
                            text = " can live mapping সড়ক মডিউল অ্যাক্সেস করতে আপনার মোবাইলের সঠিক GPS লোকেশন পারমিশন প্রয়োজন। অনুমতি প্রদান না করলে মডিউলটি কাজ করবে না।",
                            color = TextWhite,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showingPermissionRequest = false },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkNavySurface),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("বাতিল", color = Color.White)
                            }

                            Button(
                                onClick = {
                                    launcher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_grant_permission")
                            ) {
                                Text("অনুমতি দিন", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Permission Denied Explanation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(imageVector = Icons.Default.LocationOff, contentDescription = null, tint = Color.Red, modifier = Modifier.size(56.dp))
                    Text(text = "লোকেশন পারমিশন ছাড়া কাজ করবে না", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "সড়ক ড্র করার জন্য জিপিএস ডাটা রিড করা বাধ্যতামূলক। দয়া করে নিচে ক্লিক করে অনুমতি প্রদান করুন।", color = TextGray, fontSize = 12.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = { showingPermissionRequest = true }, colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)) {
                        Text("পুনরায় চেষ্টা করুন", color = DarkNavyBackground)
                    }
                }
            }
        }
    } else {
        // Active GPS Tracking Map Simulator View
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats Panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "দূরত্ব", color = TextGray, fontSize = 10.sp)
                        Text(text = String.format("%.3f km", trackingDistance), color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "সময়", color = TextGray, fontSize = 10.sp)
                        val mins = trackingTime / 60
                        val secs = trackingTime % 60
                        Text(text = String.format("%02d:%02d", mins, secs), color = NeonTeal, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "গতিবেগ", color = TextGray, fontSize = 10.sp)
                        val speed = if (isTracking && !isPaused) (4..6).random() else 0
                        Text(text = "$speed km/h", color = ElectricBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            // Interactive Dynamic Canvas Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkNavySurfaceCard)
                    .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(16.dp))
            ) {
                // Pulse Animation around the last pointer
                val lastPoint = trackingCoordinates.lastOrNull() ?: Offset(150f, 400f)
                val infiniteTransition = rememberInfiniteTransition(label = "GPSPulse")
                val pulseRad by infiniteTransition.animateFloat(
                    initialValue = 5f,
                    targetValue = 60f,
                    animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
                    label = "PulseRad"
                )
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
                    label = "PulseAlpha"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Background grid lines to look like technical blueprint map
                    val gridSpacing = 60f
                    for (x in 0..size.width.toInt() step gridSpacing.toInt()) {
                        drawLine(color = GlassBorder.copy(alpha = 0.1f), start = Offset(x.toFloat(), 0f), end = Offset(x.toFloat(), size.height))
                    }
                    for (y in 0..size.height.toInt() step gridSpacing.toInt()) {
                        drawLine(color = GlassBorder.copy(alpha = 0.1f), start = Offset(0f, y.toFloat()), end = Offset(size.width, y.toFloat()))
                    }

                    // Draw the live plotted coordinates as a cyan line
                    if (trackingCoordinates.size > 1) {
                        val path = Path().apply {
                            moveTo(trackingCoordinates[0].x, trackingCoordinates[0].y)
                            for (i in 1 until trackingCoordinates.size) {
                                lineTo(trackingCoordinates[i].x, trackingCoordinates[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = NeonCyan,
                            style = Stroke(width = 8f, cap = StrokeCap.Round)
                        )
                    }

                    // Pulse at current locator
                    drawCircle(
                        color = NeonCyan.copy(alpha = pulseAlpha),
                        radius = pulseRad,
                        center = lastPoint
                    )
                    drawCircle(
                        color = NeonCyan,
                        radius = 8f,
                        center = lastPoint
                    )
                }

                // Interactive Map Overlays
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = DarkNavyBackground.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (isTracking && !isPaused) NeonTeal else Color.Red))
                            Text(
                                text = if (isTracking && !isPaused) "লাইভ ট্র্যাকিং সক্রিয়" else if (isPaused) "ট্র্যাকিং সাময়িক বিরতি" else "ট্র্যাকিং শুরু করার জন্য অপেক্ষা",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = DarkNavyBackground.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(10.dp))
                            Text(
                                text = "জিপিএস অবস্থান: $gpsCoordinatesText",
                                color = NeonCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isTracking) {
                    GlowButton(
                        text = "ম্যাপিং শুরু করুন (Start)",
                        onClick = onStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_start_tracking")
                    )
                } else {
                    Button(
                        onClick = onPauseResume,
                        colors = ButtonDefaults.buttonColors(containerColor = if (isPaused) NeonTeal else Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_pause_tracking")
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null, tint = DarkNavyBackground)
                            Text(text = if (isPaused) "পুনরায় শুরু" else "বিরতি (Pause)", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onFinish,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_finish_tracking")
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null, tint = Color.White)
                            Text(text = "শেষ করুন (Finish)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. ROAD FORM VIEW
// -------------------------------------------------------------
@Composable
fun RoadFormView(
    viewModel: BarishalViewModel,
    distance: Double,
    duration: Int,
    coordinates: List<Offset>,
    onSubmit: () -> Unit
) {
    val context = LocalContext.current
    var roadName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Paved Road") }
    var roadWidth by remember { mutableStateOf("12 feet") }
    var selectedCondition by remember { mutableStateOf("Good") }
    var startPoint by remember { mutableStateOf("Bhola Mor") }
    var endPoint by remember { mutableStateOf("Central Hub") }
    var district by remember { mutableStateOf("Barishal") }
    var upazila by remember { mutableStateOf("Sadar") }
    var unionName by remember { mutableStateOf("Kirtankhola Union") }
    var description by remember { mutableStateOf("") }

    val categories = listOf("Paved Road", "Brick Road", "Dirt Road", "Semi Paved")
    val conditions = listOf("Excellent", "Good", "Damaged", "Under Construction")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "ম্যাপকৃত সড়কের তথ্য পূরণ করুন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)

        // Precalculated Stats info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "সংগৃহীত দূরত্ব", color = TextGray, fontSize = 10.sp)
                    Text(text = String.format("%.3f কিমি", distance), color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column {
                    Text(text = "রেকর্ডকৃত সময়", color = TextGray, fontSize = 10.sp)
                    Text(text = "${duration / 60} মিনিট ${duration % 60} সেকেন্ড", color = NeonTeal, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column {
                    Text(text = "কোঅর্ডিনেট পয়েন্ট", color = TextGray, fontSize = 10.sp)
                    Text(text = "${coordinates.size} টি জিপিএস নোড", color = ElectricBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Inputs
        OutlinedTextField(
            value = roadName,
            onValueChange = { roadName = it },
            label = { Text("সড়কের নাম (যেমন: রুপাতলী বাইপাস সংযোগ)") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_road_name"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = GlassBorder
            )
        )

        Text(text = "সড়কের ধরন:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            categories.forEach { cat ->
                val isSel = selectedCategory == cat
                Surface(
                    modifier = Modifier
                        .clickable { selectedCategory = cat }
                        .testTag("form_cat_$cat"),
                    color = if (isSel) NeonCyan.copy(alpha = 0.2f) else DarkNavySurfaceCard,
                    border = BorderStroke(1.dp, if (isSel) NeonCyan else GlassBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSel) NeonCyan else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = roadWidth,
            onValueChange = { roadWidth = it },
            label = { Text("সড়কের প্রস্থ (যেমন: ১২ ফিট বা ২৪ ফিট)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = GlassBorder
            )
        )

        Text(text = "সড়কের বর্তমান অবস্থা:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            conditions.forEach { cond ->
                val isSel = selectedCondition == cond
                Surface(
                    modifier = Modifier
                        .clickable { selectedCondition = cond }
                        .testTag("form_cond_$cond"),
                    color = if (isSel) NeonTeal.copy(alpha = 0.2f) else DarkNavySurfaceCard,
                    border = BorderStroke(1.dp, if (isSel) NeonTeal else GlassBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = cond,
                        color = if (isSel) NeonTeal else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = startPoint,
                onValueChange = { startPoint = it },
                label = { Text("শুরুর পয়েন্ট") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = GlassBorder
                )
            )
            OutlinedTextField(
                value = endPoint,
                onValueChange = { endPoint = it },
                label = { Text("শেষের পয়েন্ট") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = GlassBorder
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = district,
                onValueChange = { district = it },
                label = { Text("জেলা") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = GlassBorder
                )
            )
            OutlinedTextField(
                value = upazila,
                onValueChange = { upazila = it },
                label = { Text("উপজেলা") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = GlassBorder
                )
            )
        }

        OutlinedTextField(
            value = unionName,
            onValueChange = { unionName = it },
            label = { Text("ইউনিয়ন") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = GlassBorder
            )
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("সড়ক বিবরণ ও ল্যান্ডমার্কসমূহ") },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = GlassBorder
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Submit Button
        GlowButton(
            text = "সড়ক ম্যাপিং ডাটা দাখিল করুন (Submit)",
            onClick = {
                if (roadName.isEmpty() || description.isEmpty()) {
                    Toast.makeText(context, "দয়া করে সড়কের নাম এবং বিবরণ সঠিকভাবে লিখুন!", Toast.LENGTH_SHORT).show()
                } else {
                    // Create coordinates JSON
                    val coordsString = coordinates.joinToString(separator = ",") { "[${it.x}, ${it.y}]" }
                    val newRoad = SmartRoad(
                        id = UUID.randomUUID().toString(),
                        name = roadName,
                        category = selectedCategory,
                        width = roadWidth,
                        condition = selectedCondition,
                        description = description,
                        startPoint = startPoint,
                        endPoint = endPoint,
                        coordinatesJson = "[$coordsString]",
                        district = district,
                        upazila = upazila,
                        unionName = unionName,
                        status = "PENDING", // Strictly PENDING initially
                        contributor = viewModel.userName.value,
                        approvedDate = "",
                        lastUpdated = "2026-06-28",
                        distance = if (distance > 0.0) distance else 1.2,
                        durationSeconds = if (duration > 0) duration else 300
                    )
                    viewModel.addSmartRoad(newRoad) { success ->
                        if (success) {
                            Toast.makeText(context, "সড়ক ম্যাপিং ক্লাউডে সাবমিট হয়েছে এবং অনুমোদনের অপেক্ষায় আছে!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "অফলাইনে স্থানীয়ভাবে সংরক্ষিত হয়েছে! ইন্টারনেট সংযোগ সচল হলে এডমিন প্যানেল থেকে সিঙ্ক করুন।", Toast.LENGTH_LONG).show()
                        }
                    }
                    onSubmit()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_submit_road_form")
        )
    }
}

// -------------------------------------------------------------
// 5. MY ROADS VIEW
// -------------------------------------------------------------
@Composable
fun MyRoadsView(
    viewModel: BarishalViewModel,
    onSelectRoad: (SmartRoad) -> Unit
) {
    val allRoads by viewModel.allSmartRoads.collectAsState()
    val myName by viewModel.userName.collectAsState()
    val myRoads = allRoads.filter { it.contributor == myName }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "আমার সংযোজিত সড়কসমূহ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = "আপনার ম্যাপকৃত এবং সাবমিটকৃত সড়কসমূহের অনুমোদনের লাইভ স্ট্যাটাস দেখুন এখানে।", color = TextCyan, fontSize = 11.sp)

        if (myRoads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.DirectionsBike, contentDescription = null, tint = TextGray, modifier = Modifier.size(56.dp))
                    Text(text = "আপনি এখনো কোনো সড়ক ম্যাপ করেননি।", color = TextGray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("my_roads_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(myRoads) { road ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectRoad(road) }
                            .testTag("my_road_card_${road.id}"),
                        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = road.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                
                                // Status Badge
                                Surface(
                                    color = when (road.status) {
                                        "APPROVED" -> NeonTeal.copy(alpha = 0.15f)
                                        "PENDING" -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                        else -> Color.Red.copy(alpha = 0.15f)
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, when (road.status) {
                                        "APPROVED" -> NeonTeal
                                        "PENDING" -> Color(0xFFF59E0B)
                                        else -> Color.Red
                                    })
                                ) {
                                    Text(
                                        text = when (road.status) {
                                            "APPROVED" -> "APPROVED (অনুমোদিত)"
                                            "PENDING" -> "PENDING (অপেক্ষমাণ)"
                                            else -> "REJECTED (প্রত্যাখ্যাত)"
                                        },
                                        color = when (road.status) {
                                            "APPROVED" -> NeonTeal
                                            "PENDING" -> Color(0xFFF59E0B)
                                            else -> Color.Red
                                        },
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(text = "রুট প্রকার: ${road.category} | প্রস্থ: ${road.width}", color = TextWhite, fontSize = 11.sp)
                            Text(text = "সর্বশেষ আপডেট: ${road.lastUpdated}", color = TextGray, fontSize = 10.sp)

                            if (road.status == "REJECTED" && road.rejectReason.isNotEmpty()) {
                                Surface(
                                    color = Color.Red.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "প্রত্যাখ্যানের কারণ: ${road.rejectReason}",
                                        color = Color(0xFFEF4444),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(8.dp)
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

// -------------------------------------------------------------
// 6. ROAD STATISTICS VIEW
// -------------------------------------------------------------
@Composable
fun RoadStatsView(
    viewModel: BarishalViewModel
) {
    val allRoads by viewModel.allSmartRoads.collectAsState()
    val approvedRoads = allRoads.filter { it.status == "APPROVED" }

    var selectedUnionFilter by remember { mutableStateOf("All Unions") }
    val unions = listOf("All Unions", "Kirtankhola Union", "Chowmatha Union", "Char Fasson Union", "Nalchity Union")

    val unionRoads = remember(approvedRoads, selectedUnionFilter) {
        if (selectedUnionFilter == "All Unions") {
            approvedRoads
        } else {
            approvedRoads.filter { it.unionName.equals(selectedUnionFilter, ignoreCase = true) }
        }
    }

    // Calculations
    val totalRoadsCount = unionRoads.size
    val pavedCount = unionRoads.count { it.category == "Paved Road" }
    val brickCount = unionRoads.count { it.category == "Brick Road" }
    val dirtCount = unionRoads.count { it.category == "Dirt Road" }
    val damagedCount = unionRoads.count { it.condition == "Damaged" }
    val activeRepairCount = unionRoads.count { it.condition == "Under Construction" }
    val totalDistance = unionRoads.sumOf { it.distance }
    val contributorsCount = unionRoads.map { it.contributor }.distinct().size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "ইউনিয়ন সড়ক পরিসংখ্যান", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Icon(imageVector = Icons.Default.BarChart, contentDescription = null, tint = NeonCyan)
        }

        // Union filter dropdown shortcuts
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            unions.forEach { union ->
                val isSelected = selectedUnionFilter == union
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedUnionFilter = union },
                    label = { Text(text = if (union == "All Unions") "সকল ইউনিয়ন" else union, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                        selectedLabelColor = NeonCyan,
                        containerColor = DarkNavySurfaceCard,
                        labelColor = TextWhite
                    ),
                    border = BorderStroke(1.dp, if (isSelected) NeonCyan else GlassBorder),
                    modifier = Modifier.testTag("chip_stats_union_$union")
                )
            }
        }

        // Broad Stats Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(DarkNavySurfaceCard, DarkNavyBackground)))
                .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = selectedUnionFilter, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "মোট ম্যাপকৃত সড়ক", color = TextGray, fontSize = 10.sp)
                        Text(text = "$totalRoadsCount টি সড়ক", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                    Column {
                        Text(text = "সর্বমোট দৈর্ঘ্য (দূরত্ব)", color = TextGray, fontSize = 10.sp)
                        Text(text = String.format("%.2f কিমি", totalDistance), color = NeonTeal, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                }
            }
        }

        // Breakdown stats
        Text(text = "সড়কের প্রকারের বিন্যাস:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        
        BreakdownRow(label = "পাকা সড়ক (Paved Road)", count = pavedCount, total = totalRoadsCount, color = NeonTeal)
        BreakdownRow(label = "এইচবিবি/ইটের সড়ক (Brick)", count = brickCount, total = totalRoadsCount, color = Color(0xFFF59E0B))
        BreakdownRow(label = "কাঁচা সড়ক (Dirt Road)", count = dirtCount, total = totalRoadsCount, color = Color(0xFFEF4444))

        Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 4.dp))

        Text(text = "রাস্তা ও রক্ষণাবেক্ষণ স্ট্যাটাস:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "ক্ষতিগ্রস্ত বা ভাঙা সড়ক", color = TextGray, fontSize = 9.sp)
                    Text(text = "$damagedCount টি", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "সংস্কার বা সংস্কারাধীন সড়ক", color = TextGray, fontSize = 9.sp)
                    Text(text = "$activeRepairCount টি", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        // Contributor list
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.VolunteerActivism, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
                Column {
                    Text(text = "স্বেচ্ছাসেবী অবদানকারী", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "এই ইউনিয়নে সর্বমোট $contributorsCount জন নাগরিক স্বেচ্ছায় নতুন জিপিএস সড়ক ম্যাপ সংযোগ করেছেন।", color = TextWhite, fontSize = 11.sp, lineHeight = 15.sp)
                }
            }
        }
    }
}

@Composable
fun BreakdownRow(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val percent = if (total > 0) (count.toFloat() / total.toFloat()) else 0f
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, color = TextWhite, fontSize = 11.sp)
            Text(text = "$count টি (${String.format("%.0f%%", percent * 100)})", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(GlassBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(if (percent > 0f) percent else 0.01f)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

// -------------------------------------------------------------
// 7. ROAD DETAILS VIEW
// -------------------------------------------------------------
@Composable
fun RoadDetailsView(
    viewModel: BarishalViewModel,
    road: SmartRoad?,
    onStartNavigation: () -> Unit
) {
    val context = LocalContext.current
    if (road == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "কোনো সড়ক নির্বাচন করা হয়নি।", color = TextGray)
        }
        return
    }

    var showReportSheet by remember { mutableStateOf(false) }
    var reportType by remember { mutableStateOf("Road Broken") }
    var reportDesc by remember { mutableStateOf("") }
    val reportTypes = listOf("Road Broken", "Road Repaired", "Bridge Added", "Construction Running", "Flooded Road")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Image Cover with Map vector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkNavySurfaceCard)
                .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(16.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // Draw decorative road outline representing the details map
                val path = Path().apply {
                    moveTo(w * 0.1f, h * 0.8f)
                    cubicTo(w * 0.3f, h * 0.9f, w * 0.4f, h * 0.3f, w * 0.6f, h * 0.4f)
                    lineTo(w * 0.9f, h * 0.1f)
                }
                drawPath(path = path, color = NeonCyan, style = Stroke(width = 10f, cap = StrokeCap.Round))
                
                // Pulse start/end markers
                drawCircle(color = NeonTeal, radius = 8f, center = Offset(w * 0.1f, h * 0.8f))
                drawCircle(color = Color.Red, radius = 8f, center = Offset(w * 0.9f, h * 0.1f))
            }

            // Banner Overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                color = DarkNavyBackground.copy(alpha = 0.8f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.DirectionsWalk, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                    Text(text = "Map Preview Mode", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Road meta information
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = road.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "${road.unionName}, ${road.upazila}, ${road.district}", color = TextCyan, fontSize = 12.sp)
            }
        }

        // 3 Column Grid with spec data
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SpecItem(label = "সড়কের ধরন", value = road.category, icon = Icons.Default.AltRoute, color = NeonCyan, modifier = Modifier.weight(1f))
            SpecItem(label = "বর্তমান অবস্থা", value = road.condition, icon = Icons.Default.Speed, color = NeonTeal, modifier = Modifier.weight(1f))
            SpecItem(label = "সড়কের প্রস্থ", value = road.width, icon = Icons.Default.SwapHoriz, color = ElectricBlue, modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SpecItem(label = "শ শুরুর পয়েন্ট", value = road.startPoint, icon = Icons.Default.LocationOn, color = NeonTeal, modifier = Modifier.weight(1f))
            SpecItem(label = "শেষ পয়েন্ট", value = road.endPoint, icon = Icons.Default.Flag, color = Color.Red, modifier = Modifier.weight(1f))
        }

        // Description
        Text(text = "সড়ক বিবরণ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = road.description, color = TextWhite, fontSize = 12.sp, lineHeight = 18.sp)

        // Contributor credit
        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = "সংযোজনকারী (Contributor)", color = TextGray, fontSize = 9.sp)
                    Text(text = road.contributor, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = "অনুমোদনের তারিখ", color = TextGray, fontSize = 9.sp)
                    Text(text = if (road.approvedDate.isEmpty()) "2026-06-28" else road.approvedDate, color = NeonTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Navigation Action
        GlowButton(
            text = "লাইভ জিপিএস নেভিগেশন শুরু (Start)",
            onClick = onStartNavigation,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_start_nav")
        )

        Divider(color = GlassBorder)

        // Community Updates List
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "নাগরিক লাইভ সড়ক আপডেট রিপোর্ট", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            IconButton(
                onClick = { showReportSheet = true },
                modifier = Modifier.testTag("btn_trigger_report_road")
            ) {
                Icon(imageVector = Icons.Default.AddComment, contentDescription = "Report update", tint = NeonCyan)
            }
        }

        val reportsList = remember(road.reportsJson) {
            if (road.reportsJson.isEmpty()) emptyList() else road.reportsJson.split("|")
        }

        if (reportsList.isEmpty()) {
            Text(
                text = "কোনো সাম্প্রতিক আপডেট নেই। সড়কটি স্বাভাবিক সচল রয়েছে।",
                color = TextGray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                reportsList.forEach { report ->
                    Surface(
                        color = DarkNavySurfaceCard,
                        border = BorderStroke(1.dp, GlassBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (report.contains("Repaired")) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (report.contains("Repaired")) NeonTeal else Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(text = report, color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Submitting community report bottom section / dialog simulator
        if (showReportSheet) {
            AlertDialog(
                onDismissRequest = { showReportSheet = false },
                title = { Text("নতুন সড়ক আপডেট রিপোর্ট করুন", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("রিপোর্টের ধরন বাছাই করুন:", color = TextWhite, fontSize = 11.sp)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            reportTypes.forEach { type ->
                                val isSelected = reportType == type
                                Surface(
                                    modifier = Modifier.clickable { reportType = type },
                                    color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else DarkNavySurfaceCard,
                                    border = BorderStroke(1.dp, if (isSelected) NeonCyan else GlassBorder),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(text = type, color = if (isSelected) NeonCyan else Color.White, fontSize = 9.sp, modifier = Modifier.padding(6.dp))
                                }
                            }
                        }

                        OutlinedTextField(
                            value = reportDesc,
                            onValueChange = { reportDesc = it },
                            placeholder = { Text("সমস্যার বিবরণ লিখুন (যেমন: ৫ নং ব্রিজের কাছে ৫ ফুট গর্ত আছে)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorder
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (reportDesc.isEmpty()) {
                                Toast.makeText(context, "দয়া করে বিবরণ লিখুন!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.reportRoadUpdate(road.id, reportType, reportDesc)
                                Toast.makeText(context, "সড়ক রিপোর্ট আপডেট দাখিল সম্পন্ন! এডমিন মূল্যায়নের পর লাইভ হবে।", Toast.LENGTH_LONG).show()
                                showReportSheet = false
                                reportDesc = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.testTag("btn_confirm_report_submit")
                    ) {
                        Text("দাখিল করুন", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReportSheet = false }) {
                        Text("বাতিল", color = TextGray)
                    }
                },
                containerColor = DarkNavySurface
            )
        }
    }
}

@Composable
fun SpecItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = DarkNavySurfaceCard,
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
            Text(text = label, color = TextGray, fontSize = 8.sp, textAlign = TextAlign.Center)
        }
    }
}

// -------------------------------------------------------------
// 8. NAVIGATION ACTIVE VIEW
// -------------------------------------------------------------
@Composable
fun NavigationActiveView(
    road: SmartRoad?,
    progress: Float,
    voiceInstruction: String,
    wrongRouteSimulated: Boolean,
    onToggleWrongRoute: () -> Unit,
    onReturnToRoute: () -> Unit
) {
    if (road == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Voice Instruction Bubble Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (wrongRouteSimulated) Color(0xFFEF4444).copy(alpha = 0.25f) else NeonCyan.copy(alpha = 0.15f)
            ),
            border = BorderStroke(1.5.dp, if (wrongRouteSimulated) Color.Red else NeonCyan)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (wrongRouteSimulated) Color.Red else NeonCyan),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (wrongRouteSimulated) Icons.Default.VolumeUp else Icons.Default.VolumeUp,
                        contentDescription = "Voice Output",
                        tint = DarkNavyBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (wrongRouteSimulated) "ভুল রুট সতর্কতা! (Wrong Route)" else "ভয়েস গাইডলাইন নির্দেশনা",
                        color = if (wrongRouteSimulated) Color.Red else NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = voiceInstruction,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Active Navigation Vector Map Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkNavySurfaceCard)
                .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(16.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw background secondary roads in faint color
                drawLine(color = GlassBorder.copy(alpha = 0.15f), start = Offset(0f, h * 0.3f), end = Offset(w, h * 0.3f), strokeWidth = 10f)
                drawLine(color = GlassBorder.copy(alpha = 0.15f), start = Offset(w * 0.4f, 0f), end = Offset(w * 0.4f, h), strokeWidth = 10f)

                // The Cyan main route line
                val routePath = Path().apply {
                    moveTo(w * 0.15f, h * 0.85f)
                    cubicTo(w * 0.35f, h * 0.95f, w * 0.45f, h * 0.35f, w * 0.65f, h * 0.45f)
                    lineTo(w * 0.85f, h * 0.15f)
                }
                drawPath(path = routePath, color = NeonCyan.copy(alpha = 0.3f), style = Stroke(width = 16f, cap = StrokeCap.Round))
                drawPath(path = routePath, color = NeonCyan, style = Stroke(width = 6f, cap = StrokeCap.Round))

                // Start Marker (Origin)
                drawCircle(color = NeonTeal, radius = 10f, center = Offset(w * 0.15f, h * 0.85f))
                // End Marker (Destination)
                drawCircle(color = Color.Red, radius = 10f, center = Offset(w * 0.85f, h * 0.15f))

                // Determine active user marker location along progress line
                val p1 = Offset(w * 0.15f, h * 0.85f)
                val p2 = Offset(w * 0.35f, h * 0.95f)
                val p3 = Offset(w * 0.45f, h * 0.35f)
                val p4 = Offset(w * 0.65f, h * 0.45f)
                val p5 = Offset(w * 0.85f, h * 0.15f)

                // Simple linear progress approximation for drawing user locator
                val userCenter = when {
                    progress < 0.25f -> {
                        val subP = progress / 0.25f
                        Offset(p1.x + (p2.x - p1.x) * subP, p1.y + (p2.y - p1.y) * subP)
                    }
                    progress < 0.5f -> {
                        val subP = (progress - 0.25f) / 0.25f
                        Offset(p2.x + (p3.x - p2.x) * subP, p2.y + (p3.y - p2.y) * subP)
                    }
                    progress < 0.75f -> {
                        val subP = (progress - 0.5f) / 0.25f
                        Offset(p3.x + (p4.x - p3.x) * subP, p3.y + (p4.y - p3.y) * subP)
                    }
                    else -> {
                        val subP = (progress - 0.75f) / 0.25f
                        Offset(p4.x + (p5.x - p4.x) * subP, p4.y + (p5.y - p4.y) * subP)
                    }
                }

                val finalUserPos = if (wrongRouteSimulated) {
                    // Deviate off path to top-left to simulate wrong route
                    Offset(userCenter.x - 60f, userCenter.y - 60f)
                } else {
                    userCenter
                }

                // Draw locator pulse ring
                drawCircle(
                    color = if (wrongRouteSimulated) Color.Red.copy(alpha = 0.4f) else NeonCyan.copy(alpha = 0.4f),
                    radius = 24f,
                    center = finalUserPos
                )
                // Draw locator dot
                drawCircle(
                    color = if (wrongRouteSimulated) Color.Red else NeonTeal,
                    radius = 8f,
                    center = finalUserPos
                )
            }

            // Direction Compass Icon top right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkNavyBackground.copy(alpha = 0.8f))
                    .border(BorderStroke(1.dp, GlassBorder), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Explore, contentDescription = "Compass", tint = NeonCyan, modifier = Modifier.size(24.dp))
            }
        }

        // Warning bar / Recalculate button when Wrong Route Triggered
        if (wrongRouteSimulated) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, Color.Red)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                        Text(text = "⚠ Wrong Route / রুট ভুল!", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onReturnToRoute,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_return_route")
                    ) {
                        Text("↩ Return to Route", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }
        }

        // Live stats panel bottom
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "বাকি দূরত্ব (Remaining)", color = TextGray, fontSize = 10.sp)
                    val remaining = (road.distance * (1f - progress)).coerceAtLeast(0.0)
                    Text(text = String.format("%.2f কিমি", remaining), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "আনুমানিক সময় (ETA)", color = TextGray, fontSize = 10.sp)
                    val remainingSecs = (road.durationSeconds * (1f - progress)).toInt().coerceAtLeast(0)
                    Text(text = "${remainingSecs / 60} মিনিট", color = NeonTeal, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "গতিবেগ (Speed)", color = TextGray, fontSize = 10.sp)
                    Text(text = "৪.৫ কিমি/ঘণ্টা", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        // Simulator deviation triggers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onToggleWrongRoute,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (wrongRouteSimulated) NeonTeal else Color(0xFFF59E0B)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_simulate_deviation")
            ) {
                Text(
                    text = if (wrongRouteSimulated) "রুট সোজা করুন" else "রুট বিচ্যুতি সিমুলেট",
                    color = DarkNavyBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
