package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // ViewModel weather states
    val currentTemp by viewModel.weatherTemp.collectAsState()
    val conditionText by viewModel.weatherCondition.collectAsState()
    val humidityText by viewModel.weatherHumidity.collectAsState()
    val windText by viewModel.weatherWindSpeed.collectAsState()
    
    // User selected location
    var selectedDistrict by remember { mutableStateOf("Barishal") }
    var selectedUpazila by remember { mutableStateOf("Sadar") }
    
    // Dropdown state
    var showDistrictMenu by remember { mutableStateOf(false) }
    var showUpazilaMenu by remember { mutableStateOf(false) }
    
    // Weather Alerts
    var isAlertActive by remember { mutableStateOf(true) }
    val alertTitle = "কালবৈশাখী ঝড় সতর্কবার্তা"
    val alertDesc = "পরবর্তী ২৪ ঘণ্টার মধ্যে বরিশাল বিভাগে তীব্র কালবৈশাখী ঝড় ও শিলাবৃষ্টির সম্ভাবনা রয়েছে। সকলকে নিরাপদ আশ্রয়ে থাকার অনুরোধ করা হচ্ছে।"

    val districts = listOf("Barishal", "Bhola", "Jhalakathi", "Patuakhali", "Pirojpur", "Barguna")
    val upazilasMap = mapOf(
        "Barishal" to listOf("Sadar", "Babuganj", "Bakerganj", "Banaripara", "Gournadi", "Hizla", "Mehendiganj", "Muladi", "Wazirpur"),
        "Bhola" to listOf("Sadar", "Burhanuddin", "Char Fasson", "Daulatkhan", "Lalmohan", "Manpura", "Tajumuddin"),
        "Jhalakathi" to listOf("Sadar", "Kathalia", "Nalchity", "Rajapur"),
        "Patuakhali" to listOf("Sadar", "Bauphal", "Galachipa", "Kalapara", "Mirzaganj", "Dumki", "Rangabali"),
        "Pirojpur" to listOf("Sadar", "Bhandaria", "Kawkhali", "Mathbaria", "Nazirpur", "Nesarabad"),
        "Barguna" to listOf("Sadar", "Amtali", "Bamna", "Betagi", "Patharghata", "Taltali")
    )
    
    val upazilas = upazilasMap[selectedDistrict] ?: listOf("Sadar")
    
    // Animation states for Weather visual elements
    val infiniteTransition = rememberInfiniteTransition(label = "weather_anim")
    
    // For cloud movement
    val cloudOffset1 by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud1"
    )
    val cloudOffset2 by infiniteTransition.animateFloat(
        initialValue = 50f,
        targetValue = -100f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud2"
    )

    // Rain drop vertical animation
    val rainDropAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain"
    )

    // Lightning flash alpha
    val lightningFlash by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0.1f at 0
                0.1f at 1400
                0.9f at 1500
                0.1f at 1600
                0.8f at 1700
                0.1f at 1800
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "lightning"
    )

    // Sun rotating/scaling glow
    val sunScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sun_scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("আবহাওয়া ও জলবায়ু", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("বরিশাল ডিজিটাল আবহাওয়া সেবা", color = TextCyan, fontSize = 10.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("weather_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.simulateWeatherRefresh()
                            Toast.makeText(context, "আবহাওয়া লাইভ সিঙ্ক সম্পন্ন হয়েছে!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("weather_refresh")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavyBackground)
            )
        },
        containerColor = DarkNavyBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Location Selectors
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // District dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { showDistrictMenu = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkNavySurfaceCard),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "জেলা: $selectedDistrict",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NeonCyan)
                            }
                        }
                        DropdownMenu(
                            expanded = showDistrictMenu,
                            onDismissRequest = { showDistrictMenu = false },
                            modifier = Modifier.background(DarkNavySurface)
                        ) {
                            districts.forEach { dist ->
                                DropdownMenuItem(
                                    text = { Text(dist, color = Color.White) },
                                    onClick = {
                                        selectedDistrict = dist
                                        selectedUpazila = upazilasMap[dist]?.first() ?: "Sadar"
                                        showDistrictMenu = false
                                        viewModel.simulateWeatherRefresh()
                                    }
                                )
                            }
                        }
                    }

                    // Upazila dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { showUpazilaMenu = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkNavySurfaceCard),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "উপজেলা: $selectedUpazila",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NeonCyan)
                            }
                        }
                        DropdownMenu(
                            expanded = showUpazilaMenu,
                            onDismissRequest = { showUpazilaMenu = false },
                            modifier = Modifier.background(DarkNavySurface)
                        ) {
                            upazilas.forEach { upz ->
                                DropdownMenuItem(
                                    text = { Text(upz, color = Color.White) },
                                    onClick = {
                                        selectedUpazila = upz
                                        showUpazilaMenu = false
                                        viewModel.simulateWeatherRefresh()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Weather Warning Alert Banner
            if (isAlertActive) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, RedEmergency.copy(alpha = 0.4f)), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = RedEmergency.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = RedEmergency,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = alertTitle,
                                        color = RedEmergency,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(
                                    onClick = { isAlertActive = false },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = TextGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = alertDesc,
                                color = TextWhite,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Main Weather Hero Card (Glassmorphic Atmosphere Card with beautiful canvas effects)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                                )
                            )
                    ) {
                        // Weather Canvas Effects Layer (Draw behind details)
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            val w = size.width
                            val h = size.height

                            if (conditionText.contains("Rainy") || conditionText.contains("বৃষ্টি")) {
                                // Draw 15 animated falling rain drops
                                for (i in 0..15) {
                                    val startX = (w / 15) * i
                                    val startY = ((rainDropAnim * h + i * 20) % h)
                                    drawLine(
                                        color = NeonCyan.copy(alpha = 0.4f),
                                        start = Offset(startX, startY),
                                        end = Offset(startX - 5f, startY + 15f),
                                        strokeWidth = 3f,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }

                            if (conditionText.contains("Stormy") || conditionText.contains("ঝড়")) {
                                // Storm Clouds and lightning
                                if (lightningFlash > 0.5f) {
                                    drawRect(Color.White.copy(alpha = 0.08f * lightningFlash))
                                    // Main bolt
                                    val lightningPath = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(w * 0.5f, 0f)
                                        lineTo(w * 0.45f, h * 0.4f)
                                        lineTo(w * 0.55f, h * 0.35f)
                                        lineTo(w * 0.48f, h * 0.7f)
                                        lineTo(w * 0.52f, h * 0.9f)
                                    }
                                    drawPath(
                                        path = lightningPath,
                                        color = NeonCyan,
                                        style = Stroke(width = 4f)
                                    )
                                }
                            }

                            if (conditionText.contains("Sunny") || conditionText.contains("রোদ")) {
                                // Rotating/Glowing sun
                                drawCircle(
                                    color = Color(0xFFFBBF24).copy(alpha = 0.15f * sunScale),
                                    radius = 70.dp.toPx() * sunScale,
                                    center = Offset(w * 0.85f, h * 0.25f)
                                )
                                drawCircle(
                                    color = Color(0xFFFBBF24),
                                    radius = 35.dp.toPx(),
                                    center = Offset(w * 0.85f, h * 0.25f)
                                )
                            }
                        }

                        // Weather Info Text Overlay
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "$selectedUpazila, $selectedDistrict, Barishal",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            // Condition Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(NeonCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = conditionText,
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "$currentTemp°C",
                                color = Color.White,
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Light
                            )

                            Text(
                                text = "অনুভূত তাপমাত্রা: ${currentTemp + 2}°C",
                                color = TextGray,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 4 Metrics Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                WeatherMetricItem(
                                    icon = Icons.Default.WaterDrop,
                                    label = "আর্দ্রতা",
                                    value = humidityText,
                                    color = NeonCyan
                                )
                                WeatherMetricItem(
                                    icon = Icons.Default.Air,
                                    label = "বাতাস",
                                    value = windText,
                                    color = Color(0xFF6366F1)
                                )
                                WeatherMetricItem(
                                    icon = Icons.Default.WbSunny,
                                    label = "UV সূচক",
                                    value = "৭.৫ (তীব্র)",
                                    color = Color(0xFFF59E0B)
                                )
                                WeatherMetricItem(
                                    icon = Icons.Default.Waves,
                                    label = "বায়ু মান",
                                    value = "৪৭ (ভালো)",
                                    color = NeonTeal
                                )
                            }
                        }
                    }
                }
            }

            // Astronomy (Sunrise & Sunset) details row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFFEF08A).copy(alpha = 0.1f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.WbTwilight, contentDescription = "Sunrise", tint = Color(0xFFFBBF24), modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text("সূর্যোদয়", color = TextGray, fontSize = 9.sp)
                                Text("সকাল ৫:১২", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFFFEDD5).copy(alpha = 0.1f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.WbTwilight, contentDescription = "Sunset", tint = Color(0xFFF97316), modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text("সূর্যাস্ত", color = TextGray, fontSize = 9.sp)
                                Text("সন্ধ্যা ৬:৪৫", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Hourly Forecast Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ঘণ্টাভিত্তিক পূর্বাভাস", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "লাইভ ট্র্যাকিং", color = TextCyan, fontSize = 10.sp)
                }
            }

            // Horizontal Hourly List
            item {
                val hours = listOf(
                    HourlyItem("১২:০০ PM", 31, "মেঘাচ্ছন্ন", Icons.Default.Cloud),
                    HourlyItem("১:০০ PM", 32, "ঝুম বৃষ্টি", Icons.Default.WaterDrop),
                    HourlyItem("২:০০ PM", 30, "বজ্রপাত", Icons.Default.FlashOn),
                    HourlyItem("৩:০০ PM", 29, "হালকা বৃষ্টি", Icons.Default.Opacity),
                    HourlyItem("৪:০০ PM", 29, "মেঘলা আকাশ", Icons.Default.Cloud),
                    HourlyItem("৫:০০ PM", 28, "পরিষ্কার", Icons.Default.WbSunny)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(hours) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                            border = BorderStroke(1.dp, GlassBorder),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.width(90.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = item.time, color = TextGray, fontSize = 9.sp)
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.desc,
                                    tint = if (item.desc == "বজ্রপাত") NeonCyan else if (item.desc == "পরিষ্কার") Color(0xFFFBBF24) else TextWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(text = "${item.temp}°", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = item.desc, color = TextGray, fontSize = 8.sp)
                            }
                        }
                    }
                }
            }

            // 7-Day Forecast Section Header
            item {
                Text(text = "৭ দিনের আবহাওয়া পূর্বাভাস", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            // 7-Day vertical items list
            val days = listOf(
                DayForecastItem("আজ", "কালবৈশাখী ঝড় ও বৃষ্টি", Icons.Default.FlashOn, 29, 32),
                DayForecastItem("আগামীকাল", "আংশিক মেঘলা ও রোদ", Icons.Default.CloudQueue, 30, 33),
                DayForecastItem("বুধবার", "পরিষ্কার উজ্জ্বল আকাশ", Icons.Default.WbSunny, 31, 35),
                DayForecastItem("বৃহস্পতিবার", "তীব্র ভ্যাপসা গরম", Icons.Default.WbSunny, 32, 36),
                DayForecastItem("শুক্রবার", "হালকা বজ্রবিদ্যুৎ সহ ঝড়", Icons.Default.FlashOn, 29, 33),
                DayForecastItem("শনিবার", "টানা ভারী বর্ষণ", Icons.Default.WaterDrop, 27, 30),
                DayForecastItem("রবিবার", "ঝড়ো হাওয়া ও বৃষ্টি", Icons.Default.Air, 28, 31)
            )

            items(days) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = item.day, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(60.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.desc,
                                tint = if (item.desc.contains("ঝড়") || item.desc.contains("বজ্র")) NeonCyan else if (item.desc.contains("রোদ") || item.desc.contains("উজ্জ্বল")) Color(0xFFFBBF24) else TextWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(text = item.desc, color = TextGray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        Text(
                            text = "${item.minTemp}° / ${item.maxTemp}°",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun WeatherMetricItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(text = label, color = TextGray, fontSize = 9.sp)
        Text(text = value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

data class HourlyItem(
    val time: String,
    val temp: Int,
    val desc: String,
    val icon: ImageVector
)

data class DayForecastItem(
    val day: String,
    val desc: String,
    val icon: ImageVector,
    val minTemp: Int,
    val maxTemp: Int
)
