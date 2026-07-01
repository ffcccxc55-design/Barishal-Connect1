package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("সব (All)") }

    val categories = listOf("সব (All)", "জাতীয়", "পুলিশ", "ফায়ার", "মেডিকেল")

    val emergencies = listOf(
        EmergencyHotline(
            "999", "জাতীয় জরুরি সেবা (999)", "পুলিশ, ফায়ার বা এ্যাম্বুলেন্স সাহায্য পেতে সরাসরি ২৪ ঘণ্টা ফ্রি কল করুন।",
            Icons.Default.Sos, RedEmergency, "জাতীয়", "geo:23.7115,90.4111", "999"
        ),
        EmergencyHotline(
            "109", "জাতীয় নারী ও শিশু নির্যাতন প্রতিরোধ সেল", "নারী ও শিশু নির্যাতনের বিরুদ্ধে যেকোনো সাহায্য ও অভিযোগ জানাতে সাহায্য করবে।",
            Icons.Default.Warning, RedEmergency, "জাতীয়", "geo:23.7115,90.4111", "109"
        ),
        EmergencyHotline(
            "01713-374251", "বরিশাল কোতোয়ালী থানা", "শহরের মূল পুলিশ থানা কার্যালয় ও টহল টিম কন্টাক্ট সাপোর্ট।",
            Icons.Default.LocalPolice, ElectricBlue, "পুলিশ", "geo:22.7001,90.3541", "01713374251"
        ),
        EmergencyHotline(
            "01713-374255", "বরিশাল মডেল থানা ও ডিবি শাখা", "অপরাধ প্রতিরোধে স্পেশাল ডিবি টিম ও নাইট টহল অফিসার কন্ট্রোল।",
            Icons.Default.LocalPolice, ElectricBlue, "পুলিশ", "geo:22.6989,90.3522", "01713374255"
        ),
        EmergencyHotline(
            "01730-336699", "ফায়ার সার্ভিস ও সিভিল ডিফেন্স সদর দপ্তর", "বরিশাল সদর অগ্নি নির্বাপন স্টেশন, যেকোনো অগ্নি দুর্ঘটনা ও অগ্নিকাণ্ডে কল দিন।",
            Icons.Default.LocalFireDepartment, RedEmergency, "ফায়ার", "geo:22.7051,90.3621", "01730336699"
        ),
        EmergencyHotline(
            "01712-887755", "বরিশাল নৌ ফায়ার রেসকিউ স্টেশন", "নদী ও লঞ্চ দুর্ঘটনার জরুরি উদ্ধার দল এবং উদ্ধার বোট সাপোর্ট স্টেশন।",
            Icons.Default.LocalFireDepartment, RedEmergency, "ফায়ার", "geo:22.7103,90.3705", "01712887755"
        ),
        EmergencyHotline(
            "0431-2173500", "SBMCH এ্যাম্বুলেন্স ও জরুরি বিভাগ", "শের-ই-বাংলা মেডিকেল কলেজ হাসপাতালের সার্বক্ষণিক অ্যাম্বুলেন্স কন্ট্রোল রুম।",
            Icons.Default.LocalHospital, NeonTeal, "মেডিকেল", "geo:22.6841,90.3582", "04312173500"
        ),
        EmergencyHotline(
            "01711-123456", "বরিশাল রেড ক্রিসেন্ট এ্যাম্বুলেন্স", "জরুরি রেসপন্স টিম ও লাইফ সাপোর্ট যুক্ত এ্যাম্বুলেন্স ট্র্যাকিং ও কল সাপোর্ট।",
            Icons.Default.LocalHospital, NeonTeal, "মেডিকেল", "geo:22.6952,90.3601", "01711123456"
        )
    )

    val filteredHotlines = remember(selectedCategory) {
        if (selectedCategory == "সব (All)") emergencies
        else emergencies.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "জরুরি সেবা (SOS)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("emergency_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                SectionHeader(title = "এক ক্লিকে জরুরি কল ও সহায়তা", subtitle = "বিভাগে যেকোনো বিপদে দ্রুততম সময়ের ভেতর সরাসরি যোগাযোগ করুন")
            }

            // Categories horizontal selectors
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSel = selectedCategory == cat
                        Surface(
                            onClick = { selectedCategory = cat },
                            color = if (isSel) RedEmergency else DarkNavySurfaceCard,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSel) RedEmergency else GlassBorder)
                        ) {
                            Text(
                                text = cat,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Main emergency list items
            items(filteredHotlines) { h ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("emergency_hotline_${h.number}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(h.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = h.icon, contentDescription = null, tint = h.color, modifier = Modifier.size(22.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = h.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    
                                    // 24 Hour status indicator
                                    Surface(
                                        color = NeonTeal.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "২৪ ঘণ্টা",
                                            color = NeonTeal,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(text = h.desc, color = TextGray, fontSize = 11.sp, lineHeight = 15.sp)
                                Text(text = "নম্বর: ${h.number}", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                            }
                        }

                        // Call, SMS, Maps actions button row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Dial Button
                            Button(
                                onClick = {
                                    val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${h.number}"))
                                    context.startActivity(dial)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = h.color.copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, h.color.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = h.color, modifier = Modifier.size(14.dp))
                                    Text(text = "সরাসরি কল", color = h.color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // SMS Button
                            Button(
                                onClick = {
                                    val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${h.smsNumber}")).apply {
                                        putExtra("sms_body", "জরুরি সাহায্য প্রয়োজন! আমি এই মুহূর্তে বিপদে পড়েছি। অনুগ্রহ করে দ্রুত যোগাযোগ করুন।")
                                    }
                                    context.startActivity(smsIntent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkNavySurface),
                                border = BorderStroke(1.dp, GlassBorder),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Sms, contentDescription = "SMS", tint = NeonCyan, modifier = Modifier.size(14.dp))
                                    Text(text = "SMS বার্তা", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Navigation/Maps Location Button
                            Button(
                                onClick = {
                                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(h.mapLocation))
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    try {
                                        context.startActivity(mapIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "গুগল ম্যাপ পাওয়া যায়নি!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkNavySurface),
                                border = BorderStroke(1.dp, GlassBorder),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Map, contentDescription = "Navigate", tint = NeonTeal, modifier = Modifier.size(14.dp))
                                    Text(text = "ম্যাপ", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class EmergencyHotline(
    val number: String,
    val title: String,
    val desc: String,
    val icon: ImageVector,
    val color: Color,
    val category: String,
    val mapLocation: String,
    val smsNumber: String
)
