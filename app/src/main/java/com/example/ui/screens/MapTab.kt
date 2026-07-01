package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun MapTab() {
    var selectedNode by remember { mutableStateOf(MapNodes.first()) }
    
    // Radar pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "স্মার্ট বরিশাল ম্যাপ",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "বিভাগের প্রধান সড়ক ও জিপিএস ট্র্যাকিং রুট",
                    color = TextCyan,
                    fontSize = 11.sp
                )
            }
            
            Icon(
                imageVector = Icons.Default.GpsFixed,
                contentDescription = "GPS Active",
                tint = NeonCyan,
                modifier = Modifier.testTag("gps_status_icon")
            )
        }

        // Animated Location Radar Map Canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .testTag("interactive_map_canvas"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Vector Canvas drawing roads and points
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Drawing Rivers (Kirtankhola, Meghna, Payra) in semi transparent blue
                    val riverBrush = Brush.linearGradient(listOf(ElectricBlue.copy(alpha = 0.2f), Color.Transparent))
                    drawCircle(color = ElectricBlue.copy(alpha = 0.1f), radius = w / 3, center = Offset(w * 0.7f, h * 0.4f))

                    // Main Dhaka - Barishal - Kuakata Highway line (N8)
                    drawLine(
                        color = GlassBorder.copy(alpha = 0.8f),
                        start = Offset(w * 0.2f, 0f),
                        end = Offset(w * 0.5f, h * 0.4f),
                        strokeWidth = 6f
                    )
                    drawLine(
                        color = GlassBorder.copy(alpha = 0.8f),
                        start = Offset(w * 0.5f, h * 0.4f),
                        end = Offset(w * 0.5f, h),
                        strokeWidth = 6f
                    )

                    // Draw connecting minor roads
                    drawLine(
                        color = GlassBorder.copy(alpha = 0.4f),
                        start = Offset(w * 0.5f, h * 0.4f),
                        end = Offset(w * 0.1f, h * 0.5f), // Jhalakathi
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = GlassBorder.copy(alpha = 0.4f),
                        start = Offset(w * 0.5f, h * 0.4f),
                        end = Offset(w * 0.9f, h * 0.3f), // Bhola
                        strokeWidth = 4f
                    )

                    // Pulse Radar ring around selected node coordinates
                    val targetX = w * selectedNode.xOffsetPercent
                    val targetY = h * selectedNode.yOffsetPercent
                    drawCircle(
                        color = NeonCyan.copy(alpha = pulseAlpha),
                        radius = pulseRadius,
                        center = Offset(targetX, targetY)
                    )
                    drawCircle(
                        color = NeonCyan,
                        radius = 8f,
                        center = Offset(targetX, targetY)
                    )
                }

                // Interactive Buttons on the canvas matching Map nodes
                MapNodes.forEach { node ->
                    val alignmentBiasX = (node.xOffsetPercent * 2) - 1f
                    val alignmentBiasY = (node.yOffsetPercent * 2) - 1f
                    
                    Box(
                        modifier = Modifier
                            .align(BiasAlignment(alignmentBiasX, alignmentBiasY))
                            .offset(y = (-15).dp) // Adjust pin height
                            .clickable { selectedNode = node }
                            .testTag("map_node_${node.id}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = node.name,
                                tint = if (selectedNode.id == node.id) NeonCyan else TextGray,
                                modifier = Modifier.size(if (selectedNode.id == node.id) 28.dp else 22.dp)
                            )
                            
                            Surface(
                                color = DarkNavyBackground.copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, if (selectedNode.id == node.id) NeonCyan else GlassBorder),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = node.name,
                                    color = if (selectedNode.id == node.id) NeonCyan else TextWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                               )
                            }
                        }
                    }
                }
            }
        }

        // Horizontal slider of quick locations to click
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MapNodes) { node ->
                FilterChip(
                    selected = selectedNode.id == node.id,
                    onClick = { selectedNode = node },
                    label = { Text(node.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                        selectedLabelColor = NeonCyan,
                        containerColor = DarkNavySurfaceCard,
                        labelColor = TextWhite
                    ),
                    border = BorderStroke(1.dp, if (selectedNode.id == node.id) NeonCyan else GlassBorder),
                    modifier = Modifier.testTag("quick_map_chip_${node.id}")
                )
            }
        }

        // Selected Node detail / road condition
        GlassCard(
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedNode.fullName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Surface(
                        color = if (selectedNode.roadCondition.contains("স্বাভাবিক") || selectedNode.roadCondition.contains("ফ্রি")) NeonTeal.copy(alpha = 0.15f) else ElectricBlue.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = selectedNode.roadCondition,
                            color = if (selectedNode.roadCondition.contains("স্বাভাবিক") || selectedNode.roadCondition.contains("ফ্রি")) NeonTeal else ElectricBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Text(
                    text = selectedNode.description,
                    color = TextWhite,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Divider(color = GlassBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "দূরত্ব (ঢাকা থেকে)", color = TextGray, fontSize = 11.sp)
                        Text(text = selectedNode.distanceFromDhaka, color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(text = "ভ্রমণ মাধ্যম", color = TextGray, fontSize = 11.sp)
                        Text(text = selectedNode.transitMedium, color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(text = "জনপ্রিয় আকর্ষণ", color = TextGray, fontSize = 11.sp)
                        Text(text = selectedNode.hotspots, color = NeonTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Alert",
                        tint = ElectricBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "পদ্মা সেতু উদ্বোধনের পর ঢাকা-বরিশাল যাতায়াত সময় প্রায় ৪.৫ ঘণ্টায় নেমে এসেছে।",
                        color = TextGray,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

data class MapNode(
    val id: String,
    val name: String,
    val fullName: String,
    val description: String,
    val roadCondition: String,
    val distanceFromDhaka: String,
    val transitMedium: String,
    val hotspots: String,
    val xOffsetPercent: Float,
    val yOffsetPercent: Float
)

val MapNodes = listOf(
    MapNode(
        id = "barishal",
        name = "বরিশাল সদর",
        fullName = "বরিশাল সদর (Barishal Sadar Hub)",
        description = "কীর্তনখোলা নদীর তীরে অবস্থিত বরিশাল বিভাগের মূল প্রশাসনিক ও শিক্ষা শহর। লঞ্চ টার্মিনাল ও বিমানবন্দর রয়েছে এখানে।",
        roadCondition = "মাঝারি জ্যাম (Active Hub)",
        distanceFromDhaka = "১৮০ কিমি",
        transitMedium = "বাস / লাক্সারি লঞ্চ",
        hotspots = "সদর রোড, গুঠিয়া",
        xOffsetPercent = 0.5f,
        yOffsetPercent = 0.4f
    ),
    MapNode(
        id = "jhalakathi",
        name = "ঝালকাঠি",
        fullName = "ঝালকাঠি জেলা সদর",
        description = "সুগন্ধা নদীর তীরে সুদৃশ্য পেয়ারা চাষের প্রাণকেন্দ্র। এ অঞ্চলের ভাসমান হাট বিশ্বব্যাপী জনপ্রিয় পর্যটন স্থান।",
        roadCondition = "রাস্তা ফাঁকা ও মসৃণ",
        distanceFromDhaka = "১৯৫ কিমি",
        transitMedium = "বাস ও লোকাল ট্রানজিট",
        hotspots = "ভাসমান পেয়ারা হাট",
        xOffsetPercent = 0.25f,
        yOffsetPercent = 0.45f
    ),
    MapNode(
        id = "patuakhali",
        name = "পটুয়াখালী",
        fullName = "পটুয়াখালী বিজ্ঞান ও প্রযুক্তি শহর",
        description = "কুয়াকাটা সৈকতের প্রবেশদ্বার। বিখ্যাত বিজ্ঞান ও প্রযুক্তি বিশ্ববিদ্যালয় (PSTU) এখানে অবস্থিত। লোহালিয়া নদী দ্বারা বেষ্টিত।",
        roadCondition = "স্বাভাবিক (Clear Expressway)",
        distanceFromDhaka = "২২০ কিমি",
        transitMedium = "পায়রা সেতু হয়ে সরাসরি বাস",
        hotspots = "পায়রা সেতু, পিজিসিબી",
        xOffsetPercent = 0.52f,
        yOffsetPercent = 0.65f
    ),
    MapNode(
        id = "bhola",
        name = "ভোলা",
        fullName = "ভোলা দ্বীপ জেলা (Bhola Island)",
        description = "দেশের একমাত্র দ্বীপ জেলা, যা মেঘনা ও তেঁতুলিয়া নদী দ্বারা বেষ্টিত। প্রচুর প্রাকৃতিক গ্যাস ও ইলিশ মাছের সমারোহ।",
        roadCondition = "লঞ্চ/ফেরি সংযোগ ট্রাফিক",
        distanceFromDhaka = "২০০ কিমি",
        transitMedium = "সরাসরি লঞ্চ / ফেরি",
        hotspots = "চর কুকরি-মুকরি",
        xOffsetPercent = 0.78f,
        yOffsetPercent = 0.45f
    ),
    MapNode(
        id = "kuakata",
        name = "কুয়াকাটা সৈকত",
        fullName = "কুয়াকাটা সমুদ্র সৈকত (Sagor Konna)",
        description = "সাগরকন্যা খ্যাত ১৮ কিলোমিটার দীর্ঘ সমুদ্র সৈকত যেখান থেকে সূর্যোদয় এবং সূর্যাস্ত উভয়ই সরাসরি দেখা যায়।",
        roadCondition = "চমৎকার এক্সপ্রেসওয়ে",
        distanceFromDhaka = "২৮০ কিমি",
        transitMedium = "সরাসরি বিলাসবহুল এসি বাস",
        hotspots = "লাল কাঁকড়ার চর, গঙ্গামতি",
        xOffsetPercent = 0.54f,
        yOffsetPercent = 0.9f
    )
)
