package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.DirectoryItem
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.GlowButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GovServiceScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allItems by viewModel.allItems.collectAsState()
    val isAdmin by viewModel.isAdminLoggedIn.collectAsState()
    val isBengali by viewModel.isBengali.collectAsState()

    // Categories (Bilingual support)
    val categories = if (isBengali) {
        listOf("UNO দপ্তর", "DC অফিস", "ভূমি অফিস (Land)", "পৌরসভা", "ইউনিয়ন পরিষদ", "কৃষি অফিস", "থানা (Police)", "ফায়ার সার্ভিস", "বিদ্যুৎ অফিস", "পাসপোর্ট অফিস")
    } else {
        listOf("UNO Office", "DC Office", "Land Office", "Pourashava", "Union Parishad", "Agriculture", "Police Station", "Fire Service", "Power Office", "Passport Office")
    }
    var selectedCategory by remember { mutableStateOf("UNO দপ্তর") }
    
    // Auto-update category when language toggles to ensure valid match
    LaunchedEffect(isBengali) {
        selectedCategory = if (isBengali) "UNO দপ্তর" else "UNO Office"
    }

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var activeDetailOffice by remember { mutableStateOf<DirectoryItem?>(null) }

    // Filter government offices
    val govOffices = remember(allItems, selectedCategory, searchQuery, isAdmin, isBengali) {
        allItems.filter { item ->
            val matchCategory = if (isBengali) {
                item.subtitle.contains(selectedCategory) || selectedCategory == "UNO দপ্তর" || item.description.contains(selectedCategory)
            } else {
                item.subtitle.contains(selectedCategory) || selectedCategory == "UNO Office" || item.description.contains(selectedCategory) ||
                (selectedCategory == "Union Parishad" && (item.subtitle.contains("ইউনিয়ন") || item.title.contains("Union")))
            }
            item.category == "gov" &&
            (isAdmin || item.status == "APPROVED") &&
            matchCategory &&
            (searchQuery.isEmpty() || item.title.contains(searchQuery, ignoreCase = true) || item.description.contains(searchQuery, ignoreCase = true))
        }
    }

    // Default pre-population if fresh
    LaunchedEffect(allItems) {
        if (allItems.none { it.category == "gov" }) {
            viewModel.addDirectoryItem(
                DirectoryItem(
                    id = "gov_1",
                    category = "gov",
                    title = "বরিশাল জেলা প্রশাসক কার্যালয় (DC Office)",
                    subtitle = "DC অফিস - বান্দ রোড, বরিশাল",
                    description = "বরিশাল জেলার প্রধান সমন্বয়কারী ও প্রশাসনিক দপ্তর। ভূমি রাজস্ব, জেলা আইন-শৃঙ্খলা এবং নির্বাচন কার্যক্রম পরিচালনা করা হয়।",
                    location = "বান্দ রোড, বরিশাল সিটি",
                    contactPhone = "01713-333000",
                    rating = 4.5f,
                    priceOrFee = "সকাল ৯:০০ - বিকাল ৫:০০", // Custom Office Hours
                    statusOrSchedule = "ড. গাজী মো: সাইফুজ্জামান (ডিসি)", // Custom Officer name
                    imageUrl = "https://images.unsplash.com/photo-1577962917302-cd874c4e31d2?auto=format&fit=crop&q=80&w=600",
                    extraDataJson = "dc.barishal.gov.bd|dcbarishal@mopa.gov.bd|৯:০০ AM - ৫:০০ PM|সেবাসমূহ: জেলা ডিরেক্টরি, নাগরিক সনদপত্র, ট্রেড লাইসেন্স আপিল, লাইসেন্স নবায়ন|প্রয়োজনীয় কাগজপত্র: ভোটার আইডি কার্ড, স্থায়ী ঠিকানা প্রমাণপত্র, আবেদন ফর্ম"
                )
            )
            viewModel.addDirectoryItem(
                DirectoryItem(
                    id = "gov_2",
                    category = "gov",
                    title = "উপজেলা নির্বাহী কর্মকর্তার কার্যালয় (UNO Office)",
                    subtitle = "UNO দপ্তর - বরিশাল সদর",
                    description = "উপজেলা প্রশাসনের কেন্দ্রবিন্দু। দুর্যোগ ব্যবস্থাপনা, প্রাথমিক শিক্ষা সমন্বয় ও সরকারি সকল অনুদান বিতরণ দপ্তর।",
                    location = "সদর উপজেলা কমপ্লেক্স, বরিশাল",
                    contactPhone = "01733-332021",
                    rating = 4.7f,
                    priceOrFee = "সকাল ৯:০০ - বিকাল ৫:০০",
                    statusOrSchedule = "মাহমুদা খাতুন (UNO)",
                    imageUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&q=80&w=600",
                    extraDataJson = "unosadar.barishal.gov.bd|unosadar@mopa.gov.bd|৯:০০ AM - ৫:০০ PM|সেবাসমূহ: প্রত্যয়ন পত্র, কাবিখা প্রকল্প তদারকি, অভিযোগ প্রতিকার|প্রয়োজনীয় কাগজপত্র: পাসপোর্ট সাইজ ছবি, ইউপি প্রত্যয়ন, জাতীয় পরিচয়পত্র"
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (isBengali) "সরকারি তথ্য ও দপ্তর" else "Gov Information & Offices", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("gov_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Language Switcher Toggle
                    IconButton(
                        onClick = { viewModel.saveSetting("is_bengali", !isBengali) },
                        modifier = Modifier
                            .background(NeonCyan.copy(alpha = 0.15f), CircleShape)
                            .size(38.dp)
                    ) {
                        Text(
                            text = if (isBengali) "EN" else "বাং",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(imageVector = Icons.Default.AddHome, contentDescription = "Add Office", tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavyBackground)
            )
        },
        containerColor = DarkNavyBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            SectionHeader(title = "সরকারি দপ্তরসমূহ (Directories)", subtitle = "বিভাগ ও জেলা পর্যায়ের সকল সরকারি অফিসের বিবরণ ও কন্টাক্ট")

            // Search Bar
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextGray)
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(text = "অফিসের নাম বা সার্ভিস খুঁজুন...", color = TextGray, fontSize = 13.sp)
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color.White,
                            modifier = Modifier.clickable { searchQuery = "" }
                        )
                    }
                }
            }

            // Category list selector
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(categories) { cat ->
                    val isSel = selectedCategory == cat
                    Surface(
                        onClick = { selectedCategory = cat },
                        color = if (isSel) NeonCyan else DarkNavySurfaceCard,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, if (isSel) NeonCyan else GlassBorder)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSel) DarkNavyBackground else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Office list
            if (govOffices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = TextGray, modifier = Modifier.size(48.dp))
                        Text(text = "কোনো সরকারি দপ্তর খুঁজে পাওয়া যায়নি", color = TextGray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("gov_offices_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(govOffices) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeDetailOffice = item }
                                .testTag("gov_office_card_${item.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(listOf(DarkNavySurface, ElectricBlue.copy(alpha = 0.2f)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
                                }

                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = item.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "দায়িত্বপ্রাপ্ত কর্মকর্তা: ${item.statusOrSchedule}", color = TextCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = TextGray, modifier = Modifier.size(12.dp))
                                        Text(text = item.location, color = TextGray, fontSize = 11.sp)
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.contactPhone}"))
                                        context.startActivity(dialIntent)
                                    },
                                    modifier = Modifier
                                        .background(NeonTeal.copy(alpha = 0.15f), CircleShape)
                                        .size(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = NeonTeal, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Office Dialog Modal
    if (activeDetailOffice != null) {
        val item = activeDetailOffice!!
        val extraData = remember(item) {
            item.extraDataJson.split("|")
        }

        AlertDialog(
            onDismissRequest = { activeDetailOffice = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavyBackground),
            content = {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(text = "দপ্তর ও সেবাসমূহ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                            navigationIcon = {
                                IconButton(onClick = { activeDetailOffice = null }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkNavyBackground)
                        )
                    },
                    containerColor = DarkNavyBackground
                ) { modalPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(modalPadding)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title Header
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = item.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            Text(text = "দায়িত্বপ্রাপ্ত: ${item.statusOrSchedule}", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        // Stats Grid (Hours & Location)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = item.priceOrFee.ifEmpty { "৯:০০ AM - ৫:০০ PM" }, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                    Text(text = "অফিস কর্মঘণ্টা", color = TextGray, fontSize = 9.sp)
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.PhoneCallback, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = item.contactPhone, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                    Text(text = "হেল্পলাইন", color = TextGray, fontSize = 9.sp)
                                }
                            }
                        }

                        // Basic Details Description
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "কার্যালয় পরিচিতি", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = item.description, color = TextWhite, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                        }

                        // Union & Representatives
                        if (item.unionName.isNotEmpty() || extraData.any { it.contains("প্রতিনিধিবর্গ") }) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "🏛️ ইউনিয়ন ও জনপ্রতিনিধি ডিরেক্টরি", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    if (item.unionName.isNotEmpty()) {
                                        Text(text = "ইউনিয়ন: ${item.unionName}", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    if (item.statusOrSchedule.isNotEmpty()) {
                                        Text(text = "দায়িত্বপ্রাপ্ত/চেয়ারম্যান: ${item.statusOrSchedule}", color = NeonTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    val reps = extraData.firstOrNull { it.contains("প্রতিনিধিবর্গ:") }
                                    if (reps != null) {
                                        val repsText = reps.replace("প্রতিনিধিবর্গ: ", "")
                                        Text(text = "মেম্বার ও প্রতিনিধি তালিকা: $repsText", color = TextGray, fontSize = 11.sp, lineHeight = 16.sp)
                                    }
                                }
                            }
                        }

                        // Extra Data (Website, Email, Services, Documents)
                        if (extraData.size >= 4) {
                            // Online details card
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(text = "অনলাইন কন্টাক্ট ইনফো", color = NeonTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                        Text(text = "ওয়েবসাইট:", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = extraData[0],
                                            color = NeonCyan,
                                            fontSize = 11.sp,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    try {
                                                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://${extraData[0]}"))
                                                        context.startActivity(webIntent)
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "লিংক ওপেন করা যায়নি।", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(16.dp))
                                        Text(text = "ইমেইল:", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(text = extraData[1], color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                    }
                                }
                            }

                            // Dynamic Services List Card
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "প্রধান নাগরিক সেবাসমূহ (Services)", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    val servicesList = extraData[3].replace("সেবাসমূহ: ", "").split(", ")
                                    servicesList.forEach { service ->
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                                            Text(text = service, color = TextWhite, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            // Required documents
                            if (extraData.size >= 5) {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(text = "প্রয়োজনীয় কাগজপত্র (Documents Required)", color = RedEmergency, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        val docsList = extraData[4].replace("প্রয়োজনীয় কাগজপত্র: ", "").split(", ")
                                        docsList.forEach { doc ->
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = RedEmergency, modifier = Modifier.size(14.dp))
                                                Text(text = doc, color = TextWhite, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Call direct buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GlowButton(
                                text = "কল দিন",
                                onClick = {
                                    val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.contactPhone}"))
                                    context.startActivity(callIntent)
                                },
                                containerColor = NeonTeal,
                                textColor = DarkNavyBackground,
                                modifier = Modifier.weight(1f)
                            )

                            GlowButton(
                                text = "ম্যাপ লোকেশন",
                                onClick = {
                                    val geoUri = Uri.parse("geo:0,0?q=${Uri.encode(item.title + ", " + item.location)}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                                        `package` = "com.google.android.apps.maps"
                                    }
                                    try {
                                        context.startActivity(mapIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "ম্যাপ ওপেন করা যায়নি।", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                containerColor = NeonCyan,
                                textColor = DarkNavyBackground,
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                    }
                }
            }
        )
    }

    // Add Government Office Dialog
    if (showAddDialog) {
        var officeTitle by remember { mutableStateOf("") }
        var officeLocation by remember { mutableStateOf("") }
        var officeDesc by remember { mutableStateOf("") }
        var officePhone by remember { mutableStateOf("") }
        var officerName by remember { mutableStateOf("") }
        var officeHours by remember { mutableStateOf("") }
        var officeUnion by remember { mutableStateOf("") }
        var membersList by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(text = "নতুন সরকারি দপ্তর যোগ করুন", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    GlassTextField(value = officeTitle, onValueChange = { officeTitle = it }, label = "দপ্তরের নাম (যেমন: চাঁদপুরা ইউনিয়ন পরিষদ)", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = officeUnion, onValueChange = { officeUnion = it }, label = "ইউনিয়ন (ঐচ্ছিক - যেমন: চাঁদপুরা ইউনিয়ন)", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = officeLocation, onValueChange = { officeLocation = it }, label = "ঠিকানা", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = officeDesc, onValueChange = { officeDesc = it }, label = "বিবরণ ও সেবাসমূহ", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = officePhone, onValueChange = { officePhone = it }, label = "হেল্পライン/ফোন নম্বর", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = officerName, onValueChange = { officerName = it }, label = "দায়িত্বপ্রাপ্ত কর্মকর্তা/চেয়ারম্যান নাম", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = membersList, onValueChange = { membersList = it }, label = "মেম্বার ও জনপ্রতিনিধি তালিকা (কমা দিয়ে লিখুন)", placeholder = "মেম্বার ১, মেম্বার ২...", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = officeHours, onValueChange = { officeHours = it }, label = "অফিস কর্মঘণ্টা (যেমন: ৯:০০ - ৫:০০)", modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (officeTitle.isEmpty() || officeLocation.isEmpty() || officeDesc.isEmpty()) {
                            Toast.makeText(context, "অনুগ্রহ করে সব তথ্য সঠিক দিন!", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        val newItem = DirectoryItem(
                            id = "gov_${System.currentTimeMillis()}",
                            category = "gov",
                            title = officeTitle,
                            subtitle = "দপ্তর - $selectedCategory",
                            description = officeDesc,
                            location = officeLocation,
                            contactPhone = officePhone,
                            priceOrFee = officeHours.ifEmpty { "৯:০০ AM - ৫:০০ PM" },
                            statusOrSchedule = officerName,
                            rating = 4.5f,
                            status = if (isAdmin) "APPROVED" else "PENDING",
                            unionName = officeUnion,
                            contributor = if (isAdmin) "Admin Manual" else "User Submission",
                            extraDataJson = "||${officeHours.ifEmpty { "৯:০০ AM - ৫:০০ PM" }}|$officeDesc|প্রতিনিধিবর্গ: ${membersList.ifEmpty { "কোনো প্রতিনিধি কন্টাক্ট এন্ট্রি করা হয়নি" }}"
                        )
                        viewModel.addDirectoryItem(newItem)
                        showAddDialog = false
                        if (isAdmin) {
                            Toast.makeText(context, "সাফল্য! সরকারি দপ্তর সরাসরি যোগ করা হয়েছে।", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "সাফল্য! অ্যাডমিন অনুমোদনের পর তথ্য প্রদর্শিত হবে।", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Text(text = "যোগ করুন", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(text = "বাতিল", color = TextGray)
                }
            },
            containerColor = DarkNavySurface
        )
    }
}
