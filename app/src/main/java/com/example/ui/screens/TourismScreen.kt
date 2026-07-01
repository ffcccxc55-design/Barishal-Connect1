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
import androidx.compose.ui.graphics.vector.ImageVector
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
fun TourismScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allItems by viewModel.allItems.collectAsState()
    val isAdmin by viewModel.isAdminLoggedIn.collectAsState()

    // 7 categories
    val categories = listOf("দর্শনীয় স্থান", "ঐতিহাসিক স্থান", "নদী ও লেক", "পার্ক ও বিনোদন", "পিকনিক স্পট", "ধর্মীয় স্থান", "জাদুঘর")
    var selectedCategory by remember { mutableStateOf("দর্শনীয় স্থান") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var activeDetailItem by remember { mutableStateOf<DirectoryItem?>(null) }

    // Filter tourist category items matching selected subcategory
    val touristItems = remember(allItems, selectedCategory, searchQuery, isAdmin) {
        allItems.filter { item ->
            item.category == "tourist" && 
            (isAdmin || item.status == "APPROVED") &&
            (item.subtitle.contains(selectedCategory) || selectedCategory == "দর্শনীয় স্থান" || item.description.contains(selectedCategory)) &&
            (searchQuery.isEmpty() || item.title.contains(searchQuery, ignoreCase = true) || item.description.contains(searchQuery, ignoreCase = true))
        }
    }

    // Default static items prepopulated if database is fresh
    LaunchedEffect(allItems) {
        if (allItems.none { it.category == "tourist" }) {
            viewModel.addDirectoryItem(
                DirectoryItem(
                    id = "tourist_1",
                    category = "tourist",
                    title = "কুয়াকাটা সমুদ্র সৈকত (Kuakata Beach)",
                    subtitle = "ঐতিহাসিক স্থান / দর্শনীয় স্থান - পটুয়াখালী",
                    description = "সূর্যোদয় ও সূর্যাস্তের অপরূপ লীলাভূমি সাগরকন্যা কুয়াকাটা। বরিশাল বিভাগের সবচেয়ে আকর্ষণীয় এবং জনপ্রিয় ভ্রমণ গন্তব্য।",
                    location = "কলাপাড়া, পটুয়াখালী",
                    contactPhone = "01711-223344",
                    rating = 4.8f,
                    priceOrFee = "ফ্রি এন্ট্রি",
                    statusOrSchedule = "২৪ ঘণ্টা খোলা",
                    imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&q=80&w=600",
                    extraDataJson = "কুয়াকাটা সমুদ্র সৈকত| paying bridge| payra| Payra Bridge| Kuakata Grand Hotel| Forest Cafe| Kuakata General Hospital| Kuakata Police Station"
                )
            )
            viewModel.addDirectoryItem(
                DirectoryItem(
                    id = "tourist_2",
                    category = "tourist",
                    title = "ভীমরুলী ভাসমান পেয়ারা বাজার",
                    subtitle = "দর্শনীয় স্থান / পিকনিক স্পট - ঝালকাঠি",
                    description = "কীর্তিপাশা খালের ওপর নৌকার ওপরে বসে বেচা-কেনার অসাধারণ দৃশ্য। বাংলার ভেনিস নামে পরিচিত ভীমরুলী পেয়ারা বাজার বর্ষাকালে জমজমাট থাকে।",
                    location = "ভীমরুলী, ঝালকাঠি",
                    contactPhone = "01912-334455",
                    rating = 4.6f,
                    priceOrFee = "নৌকা ভাড়া: ৩০০-৫০০ টাকা",
                    statusOrSchedule = "সকাল ৬:০০ - বিকাল ৫:০০",
                    imageUrl = "https://images.unsplash.com/photo-1596436889106-be35e843f974?auto=format&fit=crop&q=80&w=600",
                    extraDataJson = "ভাসমান পেয়ারা বাজার| ভীমরুলী নদী| ভীমরুলী খাল| ভীমরুলী পেয়ারা বাগান| ঝালকাঠি সদর হোটেল| ভীমরুলী ক্যাফে| ঝালকাঠি সদর হাসপাতাল| ঝালকাঠি সদর থানা"
                )
            )
            viewModel.addDirectoryItem(
                DirectoryItem(
                    id = "tourist_3",
                    category = "tourist",
                    title = "গুটিয়া মসজিদ ও বায়তুল আমান কমপ্লেক্স",
                    subtitle = "ধর্মীয় স্থান - উজিরপুর, বরিশাল",
                    description = "অনিন্দ্য সুন্দর ২০ গম্বুজ বিশিষ্ট সুউচ্চ মিনার সমৃদ্ধ দৃষ্টিনন্দন মসজিদ কমপ্লেক্স, যাতে রয়েছে সুদৃশ্য বাগান, ফলদ বৃক্ষ এবং বড় একটি দীঘি।",
                    location = "গুটিয়া, উজিরপুর, বরিশাল",
                    contactPhone = "01815-556677",
                    rating = 4.9f,
                    priceOrFee = "ফ্রি এন্ট্রি",
                    statusOrSchedule = "সকাল ৫:০০ - রাত ৯:০০",
                    imageUrl = "https://images.unsplash.com/photo-1564507592333-c60657eea523?auto=format&fit=crop&q=80&w=600",
                    extraDataJson = "গুটিয়া মসজিদ| বায়তুল আমান বাগান| গুটিয়া লেক| গুটিয়া মিনার| হোটেল শেরাটন বরিশাল| গুটিয়া ফুড প্যালেস| উজিরপুর উপজেলা হাসপাতাল| উজিরপুর মডেল থানা"
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "ভ্রমণ ও পর্যটন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("tourism_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }, modifier = Modifier.testTag("tourism_add_button")) {
                        Icon(imageVector = Icons.Default.AddLocation, contentDescription = "Add Spot", tint = NeonCyan)
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
            SectionHeader(title = "বরিশাল ট্যুরিজম ডিরেক্টরি", subtitle = "বাংলার ভেনিস ও রূপসী দক্ষিণাঞ্চলের সৌন্দর্য দর্শন করুন")

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
                            Text(text = "স্পটের নাম বা স্থান খুঁজুন...", color = TextGray, fontSize = 13.sp)
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

            // Categories Row
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
                        border = BorderStroke(1.dp, if (isSel) NeonCyan else GlassBorder),
                        modifier = Modifier.testTag("tourist_category_$cat")
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

            // Spot List
            if (touristItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Landscape, contentDescription = null, tint = TextGray, modifier = Modifier.size(48.dp))
                        Text(text = "কোনো ট্যুরিস্ট স্পট খুঁজে পাওয়া যায়নি", color = TextGray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("tourism_items_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(touristItems) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeDetailItem = item }
                                .testTag("tourist_spot_${item.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
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
                                            Icon(imageVector = Icons.Default.Photo, contentDescription = null, tint = TextGray, modifier = Modifier.size(36.dp))
                                        }
                                    }

                                    // Status Badge for Admin Approval
                                    if (item.status == "PENDING") {
                                        Surface(
                                            color = RedEmergency,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.padding(10.dp).align(Alignment.TopStart)
                                        ) {
                                            Text(
                                                text = "মডারেশন পেন্ডিং",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    // Rating
                                    Surface(
                                        color = DarkNavyBackground.copy(alpha = 0.7f),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.padding(10.dp).align(Alignment.BottomEnd)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(12.dp))
                                            Text(text = item.rating.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(text = item.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                                        Text(text = item.location, color = TextCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Text(
                                        text = item.description,
                                        color = TextGray,
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        lineHeight = 15.sp
                                    )

                                    if (isAdmin && item.status == "PENDING") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    viewModel.updateDirectoryItemStatus(item.id, "APPROVED")
                                                    Toast.makeText(context, "অনুমোদন করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(text = "অনুমোদন দিন", color = DarkNavyBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    viewModel.deleteDirectoryItem(item.id)
                                                    Toast.makeText(context, "বাতিল করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = RedEmergency),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(text = "বাতিল করুন", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
    }

    // Interactive Tourist Spot Detail Modal Dialog
    if (activeDetailItem != null) {
        val item = activeDetailItem!!
        val isFav = item.isFavorite
        val extraInfoList = remember(item) {
            if (item.extraDataJson.isNotEmpty()) item.extraDataJson.split("|") else emptyList()
        }

        AlertDialog(
            onDismissRequest = { activeDetailItem = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxSize()
                .background(DarkNavyBackground),
            content = {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(text = "ভ্রমণ স্পটের তথ্য", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                            navigationIcon = {
                                IconButton(onClick = { activeDetailItem = null }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    viewModel.toggleFavorite(item.id, item.isFavorite)
                                    Toast.makeText(context, if (isFav) "প্রিয় তালিকা থেকে সরানো হয়েছে" else "প্রিয় তালিকায় যুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
                                    activeDetailItem = activeDetailItem?.copy(isFavorite = !isFav)
                                }) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Save",
                                        tint = if (isFav) RedEmergency else Color.White
                                    )
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
                    ) {
                        // Big Cover Image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
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
                                    Icon(imageVector = Icons.Default.Landscape, contentDescription = null, tint = TextGray, modifier = Modifier.size(60.dp))
                                }
                            }
                        }

                        // Content Group
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = item.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                    Text(text = item.location, color = TextCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Quick Stats
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                    border = BorderStroke(1.dp, GlassBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = item.priceOrFee.ifEmpty { "ফ্রি এন্ট্রি" }, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                        Text(text = "প্রবেশ ফি", color = TextGray, fontSize = 9.sp)
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                    border = BorderStroke(1.dp, GlassBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = item.statusOrSchedule.ifEmpty { "২৪ ঘণ্টা খোলা" }, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                        Text(text = "সময়সূচী", color = TextGray, fontSize = 9.sp)
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                    border = BorderStroke(1.dp, GlassBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Navigation, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "১০-৪৫ কিমি", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                        Text(text = "সিটি দূরত্ব", color = TextGray, fontSize = 9.sp)
                                    }
                                }
                            }

                            // Detail Description
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "বিবরণ ও ইতিহাস (History & Info)", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = item.description, color = TextWhite, fontSize = 12.sp, lineHeight = 18.sp)
                                }
                            }

                            // Nearby Places Section (Simulated from metadata tags)
                            if (extraInfoList.size > 1) {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(text = "আশেপাশের প্রয়োজনীয় স্থানসমূহ (Nearby Places)", color = NeonTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            NearbyItemRow(label = "প্রধান হোটেল:", value = extraInfoList.getOrNull(4) ?: "কুয়াকাটা গ্র্যান্ড হোটেল", icon = Icons.Default.Hotel, color = NeonCyan)
                                            NearbyItemRow(label = "রেস্টুরেন্ট:", value = extraInfoList.getOrNull(5) ?: "ফরেস্ট ক্যাফে", icon = Icons.Default.Restaurant, color = ElectricBlue)
                                            NearbyItemRow(label = "নিকটস্থ হাসপাতাল:", value = extraInfoList.getOrNull(6) ?: "কুয়াকাটা জেনারেল হাসপাতাল", icon = Icons.Default.LocalHospital, color = RedEmergency)
                                            NearbyItemRow(label = "পুলিশ স্টেশন:", value = extraInfoList.getOrNull(7) ?: "কুয়াকাটা ট্যুরিস্ট পুলিশ", icon = Icons.Default.LocalPolice, color = NeonTeal)
                                        }
                                    }
                                }
                            }

                            // User Reviews & Rating
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "পর্যটক রেটিং ও মতামত (Reviews)", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                        Text(text = "${item.rating} / 5.0 (${(5..45).random()} টি রিভিউ)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 4.dp))

                                    // Preloaded static review
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text(text = "সাকিব আহমেদ", color = NeonTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(text = "★★★★★", color = NeonCyan, fontSize = 10.sp)
                                        }
                                        Text(text = "সত্যিই চমৎকার এবং দেখার মতো অসাধারণ জায়গা। পরিবার নিয়ে ভ্রমণের জন্য একদম উপযুক্ত পরিবেশ!", color = TextGray, fontSize = 11.sp)
                                    }
                                }
                            }

                            // CTA Action row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GlowButton(
                                    text = "কল দিন",
                                    onClick = {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.contactPhone.ifEmpty { "01711223344" }}"))
                                        context.startActivity(dialIntent)
                                    },
                                    containerColor = NeonTeal,
                                    textColor = DarkNavyBackground,
                                    modifier = Modifier.weight(1f)
                                )

                                GlowButton(
                                    text = "নেভিগেশন ম্যাপ",
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
            }
        )
    }

    // Add Tourist Spot Dialog Form
    if (showAddDialog) {
        var spotTitle by remember { mutableStateOf("") }
        var spotSubCat by remember { mutableStateOf(categories.first()) }
        var spotLocation by remember { mutableStateOf("") }
        var spotDesc by remember { mutableStateOf("") }
        var spotPhone by remember { mutableStateOf("") }
        var spotFee by remember { mutableStateOf("") }
        var spotHours by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(text = "নতুন ভ্রমণ স্থান যুক্ত করুন", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassTextField(value = spotTitle, onValueChange = { spotTitle = it }, label = "ট্যুরিস্ট স্পটের নাম", modifier = Modifier.fillMaxWidth())
                    
                    // Subcategory Selector
                    Text(text = "ভ্রমণ ক্যাটাগরি", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { c ->
                            val sel = spotSubCat == c
                            Surface(
                                onClick = { spotSubCat = c },
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

                    GlassTextField(value = spotLocation, onValueChange = { spotLocation = it }, label = "ঠিকানা ও জেলা", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = spotDesc, onValueChange = { spotDesc = it }, label = "বিস্তারিত বিবরণ ও ইতিহাস", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = spotPhone, onValueChange = { spotPhone = it }, label = "যোগাযোগ মোবাইল নম্বর", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = spotFee, onValueChange = { spotFee = it }, label = "প্রবেশ ফি (ফ্রি হলে ফাকা রাখুন)", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = spotHours, onValueChange = { spotHours = it }, label = "খোলা থাকার সময়সূচী", modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (spotTitle.isEmpty() || spotLocation.isEmpty() || spotDesc.isEmpty()) {
                            Toast.makeText(context, "অনুগ্রহ করে সব তথ্য সঠিক দিন!", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        val newItem = DirectoryItem(
                            id = "tourist_${System.currentTimeMillis()}",
                            category = "tourist",
                            title = spotTitle,
                            subtitle = "$spotSubCat - $spotLocation",
                            description = spotDesc,
                            location = spotLocation,
                            contactPhone = spotPhone,
                            priceOrFee = spotFee.ifEmpty { "ফ্রি এন্ট্রি" },
                            statusOrSchedule = spotHours.ifEmpty { "২৪ ঘণ্টা খোলা" },
                            rating = 4.5f,
                            status = "PENDING",
                            contributor = "User Submission"
                        )
                        viewModel.addDirectoryItem(newItem)
                        showAddDialog = false
                        Toast.makeText(context, "দাখিল করা হয়েছে! অ্যাডমিন অনুমোদনের পর প্রদর্শিত হবে।", Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text(text = "দাখিল করুন", color = NeonCyan, fontWeight = FontWeight.Bold)
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

@Composable
fun NearbyItemRow(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(text = label, color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
    }
}
