package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisasterScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: সর্তকতা (Active Alerts), 1: আশ্রয়কেন্দ্র (Shelter List), 2: জরুরি গাইড (Safety Instructions)
    var selectedAlert by remember { mutableStateOf<DisasterAlert?>(null) }
    
    // Static lists for disaster alerts
    val alertsList = listOf(
        DisasterAlert(
            id = "alert_1",
            title = "ঘূর্ণিঝড় 'রেমাল' সতর্কবার্তা (সংকেত ৯)",
            type = "Cyclone",
            description = "উত্তর-পশ্চিম বঙ্গোপসাগরে সৃষ্ট অতি প্রবল ঘূর্ণিঝড় 'রেমাল' শক্তি বৃদ্ধি করে বরিশাল উপকূল অভিমুখে ধেয়ে আসছে। উপকূলীয় চরাঞ্চলসমূহ ৮-১২ ফুট উচ্চতার জলোচ্ছ্বাসে প্লাবিত হতে পারে।",
            affectedArea = "ভোলা, পটুয়াখালী, বরগুনা এবং বরিশাল উপকূলীয় এলাকা",
            district = "ভোলা",
            upazila = "চরফ্যাশন ও মনপুরা",
            riskLevel = "তীব্র (CRITICAL)",
            safetyInstructions = "১. উপকূলবর্তী সকল কাঁচা ও আধা-পাকা ঘর ছেড়ে দ্রুত নিকটস্থ সাইক্লোন শেল্টারে আশ্রয় নিন।\n২. পর্যাপ্ত শুকনো খাবার, খাবার পানি, ফার্স্ট এইড কিট এবং টর্চলাইট সাথে রাখুন।\n৩. গবাদি পশুদের উঁচু স্থানে বা কিল্লাহ-তে নিরাপদ স্থানে বেঁধে রাখুন।\n৪. গুজব এড়াতে সরকারি বেতার বা টেলিভিশনের বুলেটিনে চোখ রাখুন।",
            emergencyPhone = "01730-336699 (ফায়ার সার্ভিস কন্ট্রোল রুম)",
            shelterName = "মনপুরা মডেল হাই স্কুল শেল্টার, ভোলা সাইক্লোন শেল্টার ৩",
            govNotice = "দুর্যোগ ব্যবস্থাপনা ও ত্রাণ মন্ত্রনালয় কতৃক বরিশাল বিভাগের সকল কর্মকর্তা কর্মচারীর ছুটি বাতিল করা হয়েছে এবং ১০,০০০ মেট্রিক টন চাল বরাদ্দ করা হয়েছে।"
        ),
        DisasterAlert(
            id = "alert_2",
            title = "কীর্তনখোলা নদীর পানি বিপদসীমার ওপরে",
            type = "Flood",
            description = "টানা ভারী বর্ষণ ও জোয়ারের পানির প্রভাবে কীর্তনখোলা নদী ও সন্ধ্যা নদীর পানি বিপদসীমার ৫০ সেমি ওপর দিয়ে প্রবাহিত হচ্ছে। নদী তীরবর্তী নিম্নাঞ্চল প্লাবিত হওয়ার আশঙ্কা রয়েছে।",
            affectedArea = "বরিশাল সদর, বাবুগঞ্জ, উজিরপুর নিম্নাঞ্চল",
            district = "Barishal",
            upazila = "সদর",
            riskLevel = "মাঝারি (MEDIUM)",
            safetyInstructions = "১. নদী ভাঙন ও পানির স্তরের ওপর সার্বক্ষণিক নজর রাখুন।\n২. ঘরের শুকনো খাদ্যশস্য ও প্রয়োজনীয় নথিপত্র ওয়াটারপ্রুফ ব্যাগে উঁচুতে তুলে রাখুন।\n৩. সাপ ও বিষাক্ত পোকামাকড় থেকে সাবধান থাকতে কার্বলিক এসিড ব্যবহার করুন।",
            emergencyPhone = "0431-61001 (ডিসি অফিস কন্ট্রোল)",
            shelterName = "সদর ইউনিয়ন পরিষদ আশ্রয়কেন্দ্র",
            govNotice = "পানি উন্নয়ন বোর্ড ও জেলা প্রশাসনের পক্ষ থেকে জরুরি বেড়িবাঁধ সংস্কারের জন্য বালুর বস্তা প্রস্তুত রাখা হয়েছে।"
        ),
        DisasterAlert(
            id = "alert_3",
            title = "তীব্র তাপপ্রবাহ (হিট ওয়েভ) সতর্কবার্তা",
            type = "Heat Wave",
            description = "বরিশাল অঞ্চলের ওপর দিয়ে তীব্র দাবদাহ বয়ে যাচ্ছে। তাপমাত্রা ৩৯° থেকে ৪১.৫° সেলসিয়াসে পৌঁছাতে পারে।",
            affectedArea = "বরিশাল জেলা ও সমগ্র দক্ষিণবঙ্গ",
            district = "Barishal",
            upazila = "সমগ্র বরিশাল",
            riskLevel = "নিম্ন (LOW)",
            safetyInstructions = "১. অতি জরুরি প্রয়োজন ছাড়া দুপুর ১২ টা থেকে বিকাল ৪ টা পর্যন্ত রোদে বের হওয়া এড়িয়ে চলুন।\n২. বেশি করে পানি, স্যালাইন, ডাবের পানি এবং তরল পুষ্টিকর খাদ্য গ্রহণ করুন।\n৩. ঢিলেঢালা সুতি কাপড় পরিধান করুন এবং বাইরে বের হলে ছাতা ব্যবহার করুন।",
            emergencyPhone = "333 (সরকারি তথ্য সেবা)",
            shelterName = "নিকটস্থ শীতল ছায়াযুক্ত এলাকা বা সরকারি হাসপাতাল",
            govNotice = "সকল শিক্ষা প্রতিষ্ঠানকে হিট অ্যালার্ট পিরিয়ডে অনলাইন ক্লাসের ওপর জোর দেওয়ার নির্দেশ দেওয়া হয়েছে।"
        )
    )

    // Designated shelters list in Barishal region
    val sheltersList = listOf(
        ShelterInfo("মনপুরা সরকারি সাইক্লোন শেল্টার-১", "ভোলা জেলা, মনপুরা চর এলাকা", "১,২০০ জন", "সচল (ACTIVATED)", "01711-112233"),
        ShelterInfo("চরফ্যাশন হাই স্কুল বহুমুখী আশ্রয়কেন্দ্র", "চরফ্যাশন, ভোলা", "২,৫০০ জন (গবাদি পশু রাখার স্থান সহ)", "সচল (ACTIVATED)", "01819-445566"),
        ShelterInfo("কুয়াকাটা সাইক্লোন শেল্টার ও রেডি ক্রিসেন্ট", "কলাপাড়া, পটুয়াখালী", "১,৮০০ জন", "সচল (ACTIVATED)", "01911-778899"),
        ShelterInfo("পাথরঘাটা কোস্ট গার্ড আশ্রয় কেন্দ্র", "পাথরঘাটা, বরগুনা", "১,০০০ জন", "সচল (ACTIVATED)", "01511-223344"),
        ShelterInfo("রূপাতলী হাউজিং বালিকা বিদ্যালয় আশ্রয়কেন্দ্র", "রূপাতলী, বরিশাল সদর", "৮০০ জন", "স্ট্যান্ডবাই (READY)", "01722-556677")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("দুর্যোগ সতর্কীকরণ ও আশ্রয় কেন্দ্র", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("জাতীয় জরুরি দুর্যোগ রেসপন্স সেন্টার", color = TextCyan, fontSize = 10.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedAlert != null) {
                            selectedAlert = null
                        } else {
                            onBack()
                        }
                    }, modifier = Modifier.testTag("disaster_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "রেড অ্যালার্ট সংকেত সিঙ্ক করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Default.Campaign, contentDescription = "Siren", tint = RedEmergency)
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
            if (selectedAlert != null) {
                // --- DETAILED ALERT DETAILS VIEW ---
                val alert = selectedAlert!!
                val isCrit = alert.riskLevel.contains("CRITICAL") || alert.riskLevel.contains("তীব্র")
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .border(
                                    BorderStroke(1.dp, if (isCrit) RedEmergency.copy(alpha = 0.5f) else NeonCyan.copy(alpha = 0.3f)),
                                    RoundedCornerShape(16.dp)
                                ),
                            colors = CardDefaults.cardColors(containerColor = if (isCrit) RedEmergency.copy(alpha = 0.08f) else DarkNavySurfaceCard),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NewReleases,
                                        contentDescription = "Crit",
                                        tint = if (isCrit) RedEmergency else NeonCyan
                                    )
                                    Text(
                                        text = alert.title,
                                        color = if (isCrit) RedEmergency else Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Text("আক্রান্ত অঞ্চল: ${alert.affectedArea}", color = TextCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Divider(color = GlassBorder.copy(alpha = 0.4f))
                                Text(alert.description, color = TextWhite, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                        }
                    }

                    // Emergency safety guidelines section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Security, contentDescription = "Safety", tint = NeonTeal)
                                    Text("🛡️ নিরাপত্তা ও আত্মরক্ষা গাইড", color = NeonTeal, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(alert.safetyInstructions, color = TextWhite, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                        }
                    }

                    // Shell / Government Notice Section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Assignment, contentDescription = "Notice", tint = NeonCyan)
                                    Text("📢 সরকারি জরুরী নোটিশ", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(alert.govNotice, color = TextWhite, fontSize = 11.sp, lineHeight = 16.sp)
                            }
                        }
                    }

                    // Shelters and numbers row
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("📍 প্রস্তাবিত সাইক্লোন আশ্রয়কেন্দ্র:", color = TextCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(alert.shelterName, color = TextWhite, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("📞 জরুরী হেল্পলাইন নাম্বার:", color = RedEmergency, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(alert.emergencyPhone, color = TextWhite, fontSize = 12.sp)
                            }
                        }
                    }

                    // Instant emergency phone call trigger
                    item {
                        Button(
                            onClick = {
                                Toast.makeText(context, "জরুরি নম্বরে কল করা হচ্ছে: ${alert.emergencyPhone}", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RedEmergency),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.PhoneInTalk, contentDescription = "SOS", tint = Color.White)
                                Text("জরুরি দুর্যোগ কন্ট্রোল রুমে কল দিন (SOS)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            } else {
                // --- DISASTER SELECTION MAIN TABS VIEW ---
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Blink emergency SOS Call card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 10.dp)
                            .border(BorderStroke(2.dp, RedEmergency), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = RedEmergency.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    Toast.makeText(context, "উদ্ধারকারী হটলাইন ৯৯৯-এ সংযোগ দেওয়া হচ্ছে...", Toast.LENGTH_LONG).show()
                                }
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(RedEmergency, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Sos, contentDescription = "SOS", tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Column {
                                    Text("তাৎক্ষণিক ফ্লাড ও সাইক্লোন উদ্ধার", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                    Text("জাতীয় ফ্রি জরুরি হেল্পলাইন ৯৯৯", color = RedEmergency, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Icon(Icons.Default.PhoneInTalk, contentDescription = "call", tint = RedEmergency)
                        }
                    }

                    // Tabs selection for Alerts / Shelters / Safety guide
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = DarkNavySurface,
                        contentColor = NeonCyan,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("লাইভ সংকেত", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("আশ্রয়কেন্দ্র সমূহ", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("জরুরি গাইড", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    // Content of selected tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        when (selectedTab) {
                            0 -> {
                                // 1. Active Live Alerts List
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(alertsList) { alert ->
                                        val isCrit = alert.riskLevel.contains("CRITICAL") || alert.riskLevel.contains("তীব্র")
                                        
                                        GlassCard(
                                            onClick = { selectedAlert = alert },
                                            modifier = Modifier.fillMaxWidth().testTag("disaster_alert_card_${alert.id}")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .background(
                                                            if (isCrit) RedEmergency.copy(alpha = 0.1f) else NeonCyan.copy(alpha = 0.1f),
                                                            shape = CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (isCrit) Icons.Default.Campaign else Icons.Default.Info,
                                                        contentDescription = "Alert",
                                                        tint = if (isCrit) RedEmergency else NeonCyan,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = alert.title,
                                                            color = if (isCrit) RedEmergency else Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    Text(text = alert.description, color = TextGray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    ) {
                                                        Text(text = "ঝুঁকি: ${alert.riskLevel}", color = if (isCrit) RedEmergency else NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                        Text(text = "এলাকা: ${alert.affectedArea.take(15)}...", color = TextGray, fontSize = 9.sp)
                                                    }
                                                }
                                                Icon(Icons.Default.ChevronRight, contentDescription = "View", tint = NeonCyan)
                                            }
                                        }
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(20.dp))
                                    }
                                }
                            }

                            1 -> {
                                // 2. Shelters List with capacity & call
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(sheltersList) { shelter ->
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
                                                Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(shelter.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("📍 ঠিকানা: ${shelter.location}", color = TextGray, fontSize = 10.sp)
                                                    Text("👥 ধারণক্ষমতা: ${shelter.capacity}", color = TextCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }

                                                Column(
                                                    horizontalAlignment = Alignment.End,
                                                    modifier = Modifier.weight(0.8f),
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(NeonTeal.copy(alpha = 0.15f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(shelter.status, color = NeonTeal, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            Toast.makeText(context, "আশ্রয়কেন্দ্র দায়িত্বপ্রাপ্ত অফিসারকে কল করা হচ্ছে: ${shelter.phone}", Toast.LENGTH_LONG).show()
                                                        },
                                                        modifier = Modifier.size(28.dp).background(NeonCyan, shape = CircleShape)
                                                    ) {
                                                        Icon(Icons.Default.Phone, contentDescription = "call", tint = DarkNavyBackground, modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(20.dp))
                                    }
                                }
                            }

                            2 -> {
                                // 3. Emergency guidelines (Storm, Cyclone, Flood, Lightning)
                                val guides = listOf(
                                    SafetyGuide("ঘূর্ণিঝড় সতর্কতা নির্দেশনা", "ঘূর্ণিঝড়ের সংকেত ৩ নম্বর দেখানো হলে নিরাপদ আশ্রয়ের খোঁজ শুরু করুন। ৫ নম্বর বা তার বেশি হলে বিলম্ব না করে তাৎক্ষণিক সাইক্লোন শেল্টারে চলে যান। ঘরে পর্যাপ্ত মোমবাতি, দিয়াশলাই ও শুকনো মুড়ি-চিঁড়া প্রস্তুত রাখুন।"),
                                    SafetyGuide("বন্যা ও জলোচ্ছ্বাসের আত্মরক্ষা", "বন্যার পানি ঘরে ঢুকলে বৈদ্যুতিক মেইন সুইচ অফ করে দিন। গবাদিপশুদের উঁচু মাটির ঢিবি বা কিল্লায় সরিয়ে দিন। সবসময় পানি ফুটিয়ে বা হ্যালোজেন ট্যাবলেট দিয়ে বিশুদ্ধ করে পান করুন।"),
                                    SafetyGuide("বজ্রপাত ও কালবৈশাখী ঝড়", "বজ্রপাতের শব্দ শোনামাত্র খোলা মাঠ, নদী বা বড় গাছের নিচ থেকে সরে যান এবং পাকা ঘরের নিচে আশ্রয় নিন। বাড়ির সমস্ত বৈদ্যুতিক প্লাগ ও ক্যাবল সংযোগ বিচ্ছিন্ন করে দিন।")
                                )

                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(guides) { guide ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                            border = BorderStroke(1.dp, GlassBorder)
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(guide.title, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(guide.text, color = TextWhite, fontSize = 11.sp, lineHeight = 16.sp)
                                            }
                                        }
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class DisasterAlert(
    val id: String,
    val title: String,
    val type: String,
    val description: String,
    val affectedArea: String,
    val district: String,
    val upazila: String,
    val riskLevel: String,
    val safetyInstructions: String,
    val emergencyPhone: String,
    val shelterName: String,
    val govNotice: String
)

data class ShelterInfo(
    val name: String,
    val location: String,
    val capacity: String,
    val status: String,
    val phone: String
)

data class SafetyGuide(
    val title: String,
    val text: String
)
