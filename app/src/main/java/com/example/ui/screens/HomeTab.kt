package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel
import kotlinx.coroutines.delay

@Composable
fun HomeTab(
    viewModel: BarishalViewModel,
    onCategoryClick: (String) -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToDonation: () -> Unit,
    onNavigateToEmergency: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    // Notice Board automatic rotation
    var noticeIndex by remember { mutableStateOf(0) }
    val notices = listOf(
        "কীর্তনখোলা নদীতে লঞ্চ চলাচল স্বাভাবিক রয়েছে।",
        "বরিশাল সদর রোডে সংস্কার কাজ চলছে। সাবধানে ড্রাইভ করুন।",
        "ঝালকাঠির ভীমরুলী পেয়ারা বাজারে পর্যটকদের উপচে পড়া ভিড়।",
        "বরিশাল পলিটেকনিক ইন্সটিটিউটে ফ্রি স্কিল ট্রেনিং প্রোগ্রাম শুরু।"
    )
    
    LaunchedEffect(key1 = true) {
        while (true) {
            delay(5000)
            noticeIndex = (noticeIndex + 1) % notices.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. Intelligent Lightweight Browser Search Bar (Fast Launcher)
        HomeBrowserWidget(viewModel, onCategoryClick)

        // 1. Weather Card (Interactive and styled matching Elegant Dark)
        WeatherWidget(viewModel, onCategoryClick)

        // 2. Notice Ticker (Styled as the HTML Community News banner)
        NoticeTicker(noticeText = notices[noticeIndex])

        // 3. Quick Access Services Grid Header
        SectionHeader(
            title = "জনপ্রিয় সেবাসমূহ",
            subtitle = "প্রয়োজনীয় সার্ভিস সিলেক্ট করুন"
        )

        // Services List - Responsive 3-Column Grid Layout matching the HTML Theme
        ServicesSection(
            viewModel = viewModel,
            onCategoryClick = onCategoryClick,
            onNavigateToReports = onNavigateToReports,
            onNavigateToDonation = onNavigateToDonation,
            onNavigateToEmergency = onNavigateToEmergency
        )

        // 4. Quick Stats / Citizen Report summary
        CitizenReportsSummaryCard(viewModel, onNavigateToReports)

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun WeatherWidget(viewModel: BarishalViewModel, onCategoryClick: (String) -> Unit) {
    val temp by viewModel.weatherTemp.collectAsState()
    val condition by viewModel.weatherCondition.collectAsState()
    val humidity by viewModel.weatherHumidity.collectAsState()
    val wind by viewModel.weatherWindSpeed.collectAsState()

    var isRotating by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotating) 360f else 0f,
        animationSpec = tween(600, easing = LinearEasing),
        finishedListener = { isRotating = false },
        label = "RefreshRotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCategoryClick("weather") }
            .testTag("weather_widget_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF102A43), Color(0xFF161F2C))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                // Pulse dot + LIVE IN BARISHAL at top
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(NeonCyan.copy(alpha = alpha), shape = CircleShape)
                    )
                    Text(
                        text = "LIVE IN BARISHAL",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$temp°",
                                color = Color.White,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Light
                            )
                            Text(
                                text = "C",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        Text(
                            text = "$condition • আর্দ্রতা $humidity • বাতাস $wind",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    // Sun/Cloud badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
                            .padding(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFFBBF24), shape = CircleShape)
                                .border(BorderStroke(4.dp, Color(0xFFFBBF24).copy(alpha = 0.3f)), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom actions pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // SMART ROAD pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(NeonCyan)
                            .clickable { onCategoryClick("road") }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "SMART ROAD",
                            color = Color(0xFF0A0E1A),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // BUS TRACKING pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(50))
                            .clickable { onCategoryClick("bus") }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "BUS TRACKING",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Refresh icon button
                    IconButton(
                        onClick = {
                            isRotating = true
                            viewModel.simulateWeatherRefresh()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("weather_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = NeonCyan,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(rotationAngle)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoticeTicker(noticeText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2563EB).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "খবর",
                color = Color(0xFF60A5FA),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = noticeText,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "এইমাত্র পাওয়া • বরিশাল কানেক্ট",
                color = TextGray,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

data class ServiceItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color = NeonCyan,
    val isExternalScreen: Boolean = false,
    val isDynamicButton: Boolean = false,
    val dynamicTarget: String = ""
)

@Composable
fun ServicesSection(
    viewModel: BarishalViewModel,
    onCategoryClick: (String) -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToDonation: () -> Unit,
    onNavigateToEmergency: () -> Unit
) {
    val disabledFeatures by viewModel.disabledFeatures.collectAsState()
    val dynamicButtons by viewModel.dynamicButtons.collectAsState()

    val staticServices = listOf(
        ServiceItem("road", "স্মার্ট রোড", "সড়ক ও ট্রাফিক", Icons.Default.Traffic, Color(0xFF22D3EE)),
        ServiceItem("worker", "কর্মী ডিরেক্টরি", "দক্ষ শ্রমিক", Icons.Default.Engineering, Color(0xFF6366F1)),
        ServiceItem("hospital", "হাসপাতাল", "জরুরি চিকিৎসা", Icons.Default.LocalHospital, Color(0xFFEF4444)),
        ServiceItem("school", "শিক্ষা প্রতিষ্ঠান", "স্কুল ও কলেজ", Icons.Default.School, Color(0xFFFBBF24)),
        ServiceItem("tourist", "দর্শনীয় স্থান", "ভ্রমণ ও পর্যটন", Icons.Default.PhotoCamera, Color(0xFF10B981)),
        ServiceItem("iptv", "IPTV স্পোর্টস", "লাইভ টিভি ও খেলা", Icons.Default.Tv, Color(0xFF10B981)),
        ServiceItem("browser", "স্মার্ট ব্রাউজার", "ভিডিও ডাউনলোডার", Icons.Default.Language, Color(0xFF22D3EE)),
        ServiceItem("citizen_reports", "নাগরিক রিপোর্ট", "অভিযোগ জানান", Icons.Default.Description, Color(0xFF06B6D4), isExternalScreen = true),
        ServiceItem("bus", "বাস সিডিউল", "Bus Schedule", Icons.Default.DirectionsBus, Color(0xFF3B82F6)),
        ServiceItem("transport", "যাতায়াত ও রাইড", "Transport & Rides", Icons.Default.Commute, Color(0xFF22D3EE)),
        ServiceItem("gov", "সরকারি ও ইউপি ডিরেক্টরি", "Gov & Union Dir", Icons.Default.AccountBalance, Color(0xFF3B82F6)),
        ServiceItem("business", "ব্যবসা ডিরেক্টরি", "স্থানীয় দোকানপাট", Icons.Default.Storefront, Color(0xFF22D3EE)),
        ServiceItem("agriculture", "স্মার্ট কৃষি", "ফসল ও কীটনাশক", Icons.Default.Agriculture, Color(0xFF10B981)),
        ServiceItem("emergency_sos", "জরুরি নম্বর", "৯৯৯ ও ফায়ার", Icons.Default.Sos, Color(0xFFEF4444), isExternalScreen = true),
        ServiceItem("donation", "সাহায্য ও দান", "ব্লাড ও ফান্ড", Icons.Default.VolunteerActivism, Color(0xFF10B981), isExternalScreen = true)
    )

    // Filter out disabled features
    val filteredServices = staticServices.filter { !disabledFeatures.contains(it.id) }.toMutableList()

    // Append dynamic buttons created from admin panel
    dynamicButtons.forEach { db ->
        filteredServices.add(
            ServiceItem(
                id = "db_${db.id}",
                title = db.title,
                subtitle = if (db.type == "iptv") "লাইভ টিভি লিংক" else "ওয়েব লিংক",
                icon = if (db.type == "iptv") Icons.Default.Tv else Icons.Default.Link,
                color = Color(0xFF06B6D4),
                isExternalScreen = false,
                isDynamicButton = true,
                dynamicTarget = db.target
            )
        )
    }

    val services = filteredServices

    // Layout as 3-column responsive grid matching the HTML template perfectly
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        for (i in services.indices step 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Item 1
                Box(modifier = Modifier.weight(1f)) {
                    val item1 = services[i]
                    ServiceCard(item = item1, onClick = {
                        handleServiceClick(viewModel, item1, onCategoryClick, onNavigateToReports, onNavigateToDonation, onNavigateToEmergency)
                    })
                }
                
                // Item 2
                Box(modifier = Modifier.weight(1f)) {
                    if (i + 1 < services.size) {
                        val item2 = services[i + 1]
                        ServiceCard(item = item2, onClick = {
                            handleServiceClick(viewModel, item2, onCategoryClick, onNavigateToReports, onNavigateToDonation, onNavigateToEmergency)
                        })
                    } else {
                        Spacer(modifier = Modifier.fillMaxWidth())
                    }
                }

                // Item 3
                Box(modifier = Modifier.weight(1f)) {
                    if (i + 2 < services.size) {
                        val item3 = services[i + 2]
                        ServiceCard(item = item3, onClick = {
                            handleServiceClick(viewModel, item3, onCategoryClick, onNavigateToReports, onNavigateToDonation, onNavigateToEmergency)
                        })
                    } else {
                        Spacer(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

fun handleServiceClick(
    viewModel: BarishalViewModel,
    item: ServiceItem,
    onCategoryClick: (String) -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToDonation: () -> Unit,
    onNavigateToEmergency: () -> Unit
) {
    if (item.isDynamicButton) {
        viewModel.browserTargetUrl.value = item.dynamicTarget
        onCategoryClick("browser")
    } else {
        when (item.id) {
            "citizen_reports" -> onNavigateToReports()
            "donation" -> onNavigateToDonation()
            "emergency_sos" -> onNavigateToEmergency()
            else -> onCategoryClick(item.id)
        }
    }
}

@Composable
fun ServiceCard(
    item: ServiceItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("service_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.color.copy(alpha = 0.1f))
                    .border(BorderStroke(1.dp, item.color.copy(alpha = 0.2f)), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.color,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = item.title,
                color = TextWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CitizenReportsSummaryCard(viewModel: BarishalViewModel, onNavigateToReports: () -> Unit) {
    val reports by viewModel.citizenReports.collectAsState()
    
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onNavigateToReports
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "নাগরিক রিপোর্ট আপডেট",
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "মোট দাখিলকৃত রিপোর্ট: ${reports.size} টি",
                    color = TextCyan,
                    fontSize = 11.sp
                )
                
                if (reports.isNotEmpty()) {
                    Text(
                        text = "সর্বশেষ: \"${reports.first().title}\" (${reports.first().status})",
                        color = TextGray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Text(
                        text = "কোন অভিযোগ অমীমাংসিত নেই।",
                        color = TextGray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Details",
                tint = NeonCyan
            )
        }
    }
}

@Composable
fun HomeBrowserWidget(viewModel: BarishalViewModel, onCategoryClick: (String) -> Unit) {
    var queryInput by remember { mutableStateOf("") }
    val searchEngineUrl by viewModel.browserSearchEngine.collectAsState()

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Fast Browser",
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "স্মার্ট লাইটওয়েট ব্রাউজার ও ডাউনলোডার",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(Color.Red.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("FAST", color = Color.Red, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedTextField(
                value = queryInput,
                onValueChange = { queryInput = it },
                placeholder = { Text("ওয়েব লিঙ্ক বা সার্চ লিখুন...", color = TextGray, fontSize = 12.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_browser_input"),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (queryInput.isNotEmpty()) {
                                val target = if (queryInput.startsWith("http://") || queryInput.startsWith("https://")) {
                                    queryInput
                                } else if (queryInput.contains(".") && !queryInput.contains(" ")) {
                                    "https://$queryInput"
                                } else {
                                    "$searchEngineUrl$queryInput"
                                }
                                viewModel.browserTargetUrl.value = target
                                onCategoryClick("browser")
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = NeonCyan)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = GlassBorder,
                    focusedContainerColor = DarkNavySurface,
                    unfocusedContainerColor = DarkNavySurface
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )
            
            // Fast shortcuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val shortcuts = listOf(
                    "Youtube" to "https://www.youtube.com",
                    "Facebook" to "https://www.facebook.com",
                    "Google" to "https://www.google.com"
                )
                shortcuts.forEach { (label, url) ->
                    AssistChip(
                        onClick = {
                            viewModel.browserTargetUrl.value = url
                            onCategoryClick("browser")
                        },
                        label = { Text(label, color = TextCyan, fontSize = 10.sp) },
                        border = BorderStroke(1.dp, GlassBorder)
                    )
                }
            }
        }
    }
}
