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
fun BusinessDirectoryScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allItems by viewModel.allItems.collectAsState()
    val isAdmin by viewModel.isAdminLoggedIn.collectAsState()

    // Categories
    val categories = listOf("সব (All)", "রেস্টুরেন্ট", "হোটেল (Hotel)", "ক্লিনিক ও ল্যাব", "ফার্মেসী", "কম্পিউটার ও মোবাইল", "ব্যাংক ও বীমা", "সুপার শপ")
    var selectedCategory by remember { mutableStateOf("সব (All)") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var activeBusiness by remember { mutableStateOf<DirectoryItem?>(null) }

    // Filter businesses
    val businesses = remember(allItems, selectedCategory, searchQuery, isAdmin) {
        allItems.filter { item ->
            item.category == "business" &&
            (isAdmin || item.status == "APPROVED") &&
            (selectedCategory == "সব (All)" || item.subtitle.contains(selectedCategory) || item.description.contains(selectedCategory)) &&
            (searchQuery.isEmpty() || item.title.contains(searchQuery, ignoreCase = true) || item.description.contains(searchQuery, ignoreCase = true))
        }
    }

    // Default pre-population if fresh
    LaunchedEffect(allItems) {
        if (allItems.none { it.category == "business" }) {
            viewModel.addDirectoryItem(
                DirectoryItem(
                    id = "biz_1",
                    category = "business",
                    title = "হ্যান্ডি কড়াই রেস্টুরেন্ট (Handi Korai)",
                    subtitle = "রেস্টুরেন্ট - সদর রোড, বরিশাল",
                    description = "বরিশাল শহরের ঐতিহ্যবাহী ও অভিজাত চেইন রেস্টুরেন্ট। দারুণ পরিবেশ এবং মুখরোচক কাচ্চি বিরিয়ানি ও কাবাবের জন্য বিখ্যাত।",
                    location = "সদর রোড, বরিশাল সিটি (Sadar Road)",
                    contactPhone = "01722-112233",
                    rating = 4.7f,
                    priceOrFee = "সকাল ১০:০০ - রাত ১১:০০", // Opening Hours
                    statusOrSchedule = "মো: জাহিদুল ইসলাম (মালিক)", // Owner name
                    imageUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&q=80&w=600",
                    extraDataJson = "https://facebook.com/handikorai|https://wa.me/8801722112233|handikorai.com|সেবাসমূহ: ডাইন-ইন, ক্যাটারিং, হোম ডেলিভারি, পার্টি আয়োজন|রিভিউ: খাবার অত্যন্ত সুস্বাদু এবং স্টাফদের ব্যবহার চমৎকার!"
                )
            )
            viewModel.addDirectoryItem(
                DirectoryItem(
                    id = "biz_2",
                    category = "biz_2", // We'll query using 'category = "business"'
                    title = "গ্র্যান্ড পার্ক হোটেল (Grand Park)",
                    subtitle = "হোটেল (Hotel) - বেলস পার্ক, বরিশাল",
                    description = "বিলাসবহুল ৪ তারকা মানের হোটেল। চমৎকার স্যুইট, ইনডোর সুইমিং পুল, মাল্টি-কুইজিন রেস্টুরেন্ট এবং জিম সুবিধা সমৃদ্ধ।",
                    location = "বেলস পার্ক রোড, বরিশাল",
                    contactPhone = "01912-998877",
                    rating = 4.9f,
                    priceOrFee = "২৪ ঘণ্টা খোলা",
                    statusOrSchedule = "গ্র্যান্ড পার্ক গ্রুপ লিমিটেড",
                    imageUrl = "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&q=80&w=600",
                    extraDataJson = "https://facebook.com/grandparkbarishal|https://wa.me/8801912998877|hotelgrandparkbarisal.com|সেবাসমূহ: বিলাসবহুল রুম বুকিং, কনফারেন্স হল, রুফটপ বুফে রেস্টুরেন্ট, কার পার্কিং|রিভিউ: বরিশালের সেরা হোটেল, অত্যন্ত পরিপাটি এবং নিরাপদ।"
                )
            )
        }
    }

    // Force category corrections in background
    LaunchedEffect(allItems) {
        allItems.forEach { item ->
            if (item.id == "biz_2" && item.category != "business") {
                viewModel.deleteDirectoryItem(item.id)
                viewModel.addDirectoryItem(item.copy(category = "business"))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "স্থানীয় ব্যবসা ডিরেক্টরি", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("business_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(imageVector = Icons.Default.AddBusiness, contentDescription = "Add Business", tint = NeonCyan)
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
            SectionHeader(title = "স্মার্ট বিজনেস ডিরেক্টরি", subtitle = "বিভাগের সকল রেস্টুরেন্ট, হোটেল, ক্লিনিক এবং অন্যান্য শপের প্রোফাইল")

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
                            Text(text = "ব্যবসা, ব্র্যান্ড বা সেবা খুঁজুন...", color = TextGray, fontSize = 13.sp)
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

            // Categories list selector
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

            // Business list
            if (businesses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = TextGray, modifier = Modifier.size(48.dp))
                        Text(text = "কোনো ব্যবসা বা প্রতিষ্ঠান খুঁজে পাওয়া যায়নি", color = TextGray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("business_items_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(businesses) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeBusiness = item }
                                .testTag("business_card_${item.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Business Logo Profile Picture fallback
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(listOf(DarkNavySurface, NeonCyan.copy(alpha = 0.2f)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(30.dp))
                                }

                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = item.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "মালিক/ম্যানেজার: ${item.statusOrSchedule}", color = TextCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = TextGray, modifier = Modifier.size(12.dp))
                                        Text(text = item.location, color = TextGray, fontSize = 11.sp)
                                    }
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(12.dp))
                                        Text(text = "${item.rating} / 5.0", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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

    // Detail Business profile modal popup
    if (activeBusiness != null) {
        val item = activeBusiness!!
        val extraData = remember(item) {
            item.extraDataJson.split("|")
        }

        AlertDialog(
            onDismissRequest = { activeBusiness = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavyBackground),
            content = {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(text = "ব্যবসা প্রোফাইল (Business Profile)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                            navigationIcon = {
                                IconButton(onClick = { activeBusiness = null }) {
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
                        // Business Banner / Cover Photo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            if (item.imageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(item.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.linearGradient(listOf(DarkNavySurface, ElectricBlue.copy(alpha = 0.3f)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = TextGray, modifier = Modifier.size(48.dp))
                                }
                            }
                        }

                        // Business Header
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = item.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            Text(text = "মালিক/পরিচালক: ${item.statusOrSchedule}", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                                Text(text = item.location, color = TextGray, fontSize = 12.sp)
                            }
                        }

                        // Quick Hours / Rating card
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
                                    Text(text = item.priceOrFee.ifEmpty { "সকাল ১০:০০ - রাত ১০:০০" }, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                    Text(text = "খোলার সময়সূচী", color = TextGray, fontSize = 9.sp)
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "${item.rating} / 5.0", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                    Text(text = "ক্রেতা রেটিং", color = TextGray, fontSize = 9.sp)
                                }
                            }
                        }

                        // Description
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "ব্যবসায়িক বিবরণী (About)", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = item.description, color = TextWhite, fontSize = 12.sp, lineHeight = 18.sp)
                            }
                        }

                        // Contact links (WhatsApp, Facebook, Phone, Website)
                        if (extraData.isNotEmpty()) {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(text = "যোগাযোগ ও সোশ্যাল প্রোফাইল", color = NeonTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    
                                    if (extraData.getOrNull(0)?.isNotEmpty() == true) {
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                                            Text(text = "ফেসবুক পেজ:", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "পেইজ লিংক ক্লিক করুন",
                                                color = NeonCyan,
                                                fontSize = 11.sp,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        try {
                                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(extraData[0]))
                                                            context.startActivity(intent)
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "লিংক ওপেন করা যায়নি।", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    if (extraData.getOrNull(1)?.isNotEmpty() == true) {
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(imageVector = Icons.Default.Forum, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(16.dp))
                                            Text(text = "হোয়াটসঅ্যাপ:", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "সরাসরি মেসেজ দিন",
                                                color = NeonTeal,
                                                fontSize = 11.sp,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        try {
                                                            val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse(extraData[1]))
                                                            context.startActivity(waIntent)
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "হোয়াটসঅ্যাপ ওপেন করা যায়নি।", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                        Text(text = "হটলাইন নম্বর:", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(text = item.contactPhone, color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                    }
                                }
                            }

                            // Services card
                            if (extraData.size >= 4) {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(text = "ব্যবসায়িক সেবাসমূহ (Services)", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        val serviceItems = extraData[3].replace("সেবাসমূহ: ", "").split(", ")
                                        serviceItems.forEach { svc ->
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                                                Text(text = svc, color = TextWhite, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            // Customer reviews
                            if (extraData.size >= 5) {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(text = "গ্রাহক মতামত ও রিভিউ (Reviews)", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(text = extraData[4].replace("রিভিউ: ", ""), color = TextWhite.copy(alpha = 0.9f), fontSize = 12.sp, lineHeight = 16.sp)
                                    }
                                }
                            }
                        }

                        // Bottom call to actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GlowButton(
                                text = "সরাসরি কল দিন",
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.contactPhone}"))
                                    context.startActivity(intent)
                                },
                                containerColor = NeonTeal,
                                textColor = DarkNavyBackground,
                                modifier = Modifier.weight(1f)
                            )

                            GlowButton(
                                text = "গুগল ম্যাপ",
                                onClick = {
                                    val geo = Uri.parse("geo:0,0?q=${Uri.encode(item.title + ", " + item.location)}")
                                    val intent = Intent(Intent.ACTION_VIEW, geo).apply {
                                        `package` = "com.google.android.apps.maps"
                                    }
                                    try {
                                        context.startActivity(intent)
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

    // Add Business Form Dialog
    if (showAddDialog) {
        var bizTitle by remember { mutableStateOf("") }
        var bizSubCat by remember { mutableStateOf(categories[1]) }
        var bizLocation by remember { mutableStateOf("") }
        var bizDesc by remember { mutableStateOf("") }
        var bizPhone by remember { mutableStateOf("") }
        var bizOwner by remember { mutableStateOf("") }
        var bizHours by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(text = "আপনার ব্যবসা যোগ করুন", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    GlassTextField(value = bizTitle, onValueChange = { bizTitle = it }, label = "ব্যবসা / প্রতিষ্ঠানের নাম", modifier = Modifier.fillMaxWidth())
                    
                    Text(text = "ক্যাটাগরি", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories.filter { it != "সব (All)" }) { c ->
                            val sel = bizSubCat == c
                            Surface(
                                onClick = { bizSubCat = c },
                                color = if (sel) NeonCyan else DarkNavySurfaceCard,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = c,
                                    color = if (sel) DarkNavyBackground else Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    GlassTextField(value = bizLocation, onValueChange = { bizLocation = it }, label = "ঠিকানা", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = bizDesc, onValueChange = { bizDesc = it }, label = "সেবা ও পণ্যের বিবরণ", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = bizPhone, onValueChange = { bizPhone = it }, label = "যোগাযোগ মোবাইল নম্বর", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = bizOwner, onValueChange = { bizOwner = it }, label = "মালিকের নাম", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = bizHours, onValueChange = { bizHours = it }, label = "খোলা থাকার কর্মঘণ্টা", modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (bizTitle.isEmpty() || bizLocation.isEmpty() || bizDesc.isEmpty()) {
                            Toast.makeText(context, "অনুগ্রহ করে সব তথ্য সঠিক দিন!", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        val newItem = DirectoryItem(
                            id = "biz_${System.currentTimeMillis()}",
                            category = "business",
                            title = bizTitle,
                            subtitle = "$bizSubCat - $bizLocation",
                            description = bizDesc,
                            location = bizLocation,
                            contactPhone = bizPhone,
                            priceOrFee = bizHours,
                            statusOrSchedule = bizOwner,
                            rating = 4.5f,
                            status = "PENDING",
                            contributor = "User Submission"
                        )
                        viewModel.addDirectoryItem(newItem)
                        showAddDialog = false
                        Toast.makeText(context, "সাফল্য! অ্যাডমিন রিভিউয়ের পর আপনার ব্যবসা প্রোফাইল প্রকাশিত হবে।", Toast.LENGTH_LONG).show()
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
