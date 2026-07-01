package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DirectoryItem
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowButton
import com.example.ui.components.GlassTextField
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel

// Local Mock Gallery Photo Items for Transport Photo manual selection
data class TransportGalleryItem(
    val titleBn: String,
    val titleEn: String,
    val url: String
)

val transportGalleryPhotos = listOf(
    TransportGalleryItem("বিলাসবহুল স্লিপার বাস 🚌", "Luxury Sleeper Bus 🚌", "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=500"),
    TransportGalleryItem("ভিআইপি ট্রিপল ডেক লঞ্চ 🚢", "VIP Triple Deck Launch 🚢", "https://images.unsplash.com/photo-1516690561799-46d8f74f9abf?w=500"),
    TransportGalleryItem("রেন্ট-এ-কার প্রাইভেটকার 🚗", "Rent-a-Car Sedan 🚗", "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=500"),
    TransportGalleryItem("রাইড শেয়ারিং মোটরসাইকেল 🏍️", "Ride Sharing Motorcycle 🏍️", "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=500"),
    TransportGalleryItem("জরুরি লাইফ সাপোর্ট অ্যাম্বুলেন্স 🚑", "Emergency Life Support Ambulance 🚑", "https://images.unsplash.com/photo-1587749981861-41619623d30b?w=500"),
    TransportGalleryItem("অটোরিকশা ও ইজিবাইক 🛺", "Auto Rickshaw & Easybike 🛺", "https://images.unsplash.com/photo-1566008889962-402943753e4c?w=500")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allItems by viewModel.allItems.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val isBengali by viewModel.isBengali.collectAsState()

    // Filters
    var fromQuery by remember { mutableStateOf("") }
    var toQuery by remember { mutableStateOf("") }
    var selectedVehicleType by remember { mutableStateOf("All") } // All, Bus, Launch, Private Car, Motorcycle, Rent-a-Car, Ambulance, Easybike

    // Dialogs
    var showAddDialog by remember { mutableStateOf(false) }
    var showGalleryPicker by remember { mutableStateOf(false) }

    // Selected photo state (NO URL text inputs!)
    var selectedPhoto by remember { mutableStateOf(transportGalleryPhotos[0]) }

    // Pre-populate Database with beautiful default Transport Items if none exist
    LaunchedEffect(allItems) {
        val hasTransport = allItems.any { it.category == "transport" }
        if (!hasTransport) {
            val defaultServices = listOf(
                DirectoryItem(
                    id = "trans_1",
                    category = "transport",
                    title = "সাকুরা পরিবহন (Sakura Paribahan AC)",
                    subtitle = "বরিশাল - ঢাকা (পদ্মা সেতু হয়ে)",
                    description = "বিলাসবহুল স্লিপার ও রিক্লাইনিং সিট। অত্যন্ত নিরাপদ ও আরামদায়ক সেবা।",
                    priceOrFee = "৮০০ BDT",
                    statusOrSchedule = "Bus",
                    contactPhone = "01711-224499",
                    location = "নথুল্লাবাদ বাস টার্মিনাল, বরিশাল",
                    imageUrl = "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=500",
                    status = "APPROVED",
                    contributor = "System"
                ),
                DirectoryItem(
                    id = "trans_2",
                    category = "transport",
                    title = "এমভি মানামী (MV Manami VIP)",
                    subtitle = "বরিশাল লঞ্চ ঘাট - ঢাকা সদরঘাট",
                    description = "ডাবল এসি কেবিন ও সোফা সিট সুবিধা। সুস্বাদু খাবার লাউঞ্জ সম্পন্ন।",
                    priceOrFee = "ডেক: ৩৫০ BDT | সিঙ্গেল: ১৫০০ BDT",
                    statusOrSchedule = "Launch",
                    contactPhone = "01711-558899",
                    location = "বরিশাল লঞ্চ ঘাট",
                    imageUrl = "https://images.unsplash.com/photo-1516690561799-46d8f74f9abf?w=500",
                    status = "APPROVED",
                    contributor = "System"
                ),
                DirectoryItem(
                    id = "trans_3",
                    category = "transport",
                    title = "আল-আমিন অ্যাম্বুলেন্স সার্ভিস (Emergency Ambulance)",
                    subtitle = "বরিশাল - দেশের যেকোনো জেলা",
                    description = "২৪ ঘণ্টা জরুরি রোগী ও লাশ বহনের জন্য অক্সিজেন ও লাইফ সাপোর্ট সম্পন্ন আধুনিক অ্যাম্বুলেন্স।",
                    priceOrFee = "১২০০০ BDT (ঢাকা রুট)",
                    statusOrSchedule = "Ambulance",
                    contactPhone = "01511-778899",
                    location = "শেরে বাংলা মেডিকেল কলেজ হাসপাতাল গেট",
                    imageUrl = "https://images.unsplash.com/photo-1587749981861-41619623d30b?w=500",
                    status = "APPROVED",
                    contributor = "System"
                ),
                DirectoryItem(
                    id = "trans_4",
                    category = "transport",
                    title = "রহমান রেন্ট-এ-কার (Barishal Rent-a-Car)",
                    subtitle = "যেকোনো গন্তব্যে যাতায়াত",
                    description = "প্রাইভেটকার, নোয়াহ মাইক্রোবাস ও হাইস গাড়ি দৈনিক বা ট্রিপ ভিত্তিক ভাড়ায় পাওয়া যায়। দক্ষ চালক।",
                    priceOrFee = "৩০০০ BDT / দিন",
                    statusOrSchedule = "Rent-a-Car",
                    contactPhone = "01911-334455",
                    location = "সদর রোড, বরিশাল",
                    imageUrl = "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=500",
                    status = "APPROVED",
                    contributor = "System"
                ),
                DirectoryItem(
                    id = "trans_5",
                    category = "transport",
                    title = "করিম মিয়া রাইড শেয়ারিং (Motorcycle Ride)",
                    subtitle = "চৌমাথা মোড় - রূপাতলী স্ট্যান্ড",
                    description = "তাৎক্ষণিক মোটরবাইক রাইড শেয়ারিং সেবা। দ্রুত ও নিরাপদ যাতায়াত নিশ্চিত। হেলমেট সুবিধা আছে।",
                    priceOrFee = "৮০ BDT",
                    statusOrSchedule = "Motorcycle",
                    contactPhone = "01819-112233",
                    location = "চৌমাথা মোড়, বরিশাল সিটি",
                    imageUrl = "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=500",
                    status = "APPROVED",
                    contributor = "System"
                )
            )
            defaultServices.forEach { viewModel.addDirectoryItem(it) }
        }
    }

    // Filter List based on categories and search query
    val filteredServices = remember(allItems, fromQuery, toQuery, selectedVehicleType, isAdminLoggedIn) {
        allItems.filter { item ->
            item.category == "transport" &&
            (isAdminLoggedIn || item.status == "APPROVED") &&
            (selectedVehicleType == "All" || item.statusOrSchedule.equals(selectedVehicleType, ignoreCase = true)) &&
            (fromQuery.isEmpty() || item.subtitle.contains(fromQuery, ignoreCase = true) || item.title.contains(fromQuery, ignoreCase = true)) &&
            (toQuery.isEmpty() || item.subtitle.contains(toQuery, ignoreCase = true) || item.description.contains(toQuery, ignoreCase = true))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isBengali) "যাতায়াত ও রাইড শেয়ারিং" else "Transport & Ride Sharing",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = if (isBengali) "বাস, লঞ্চ, রাইড শেয়ার ও প্রাইভেটকার বুকিং" else "Buses, Launches, Ride Share & Rentals",
                            color = TextCyan,
                            fontSize = 10.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("transport_back_button")) {
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
                    IconButton(onClick = { showAddDialog = true }, modifier = Modifier.testTag("transport_add_btn")) {
                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Add Service", tint = NeonCyan, modifier = Modifier.size(28.dp))
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
            SectionHeader(
                title = if (isBengali) "পরিবহন ও রাইড বুকিং ডিরেক্টরি" else "Transport & Ride Booking Directory",
                subtitle = if (isBengali) "সরাসরি ফোন ও লোকেশনের মাধ্যমে বুক করুন এবং সেবা শেয়ার করুন।" else "Book directly via phone call & address location."
            )

            // Search Filter Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = fromQuery,
                            onValueChange = { fromQuery = it },
                            label = { Text(if (isBengali) "কোথা থেকে / নাম" else "From / Name", color = TextCyan, fontSize = 11.sp) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "swap",
                            tint = NeonCyan,
                            modifier = Modifier
                                .clickable {
                                    val tmp = fromQuery
                                    fromQuery = toQuery
                                    toQuery = tmp
                                }
                                .size(24.dp)
                        )

                        OutlinedTextField(
                            value = toQuery,
                            onValueChange = { toQuery = it },
                            label = { Text(if (isBengali) "কোথায় যাবেন" else "Destination / To", color = TextCyan, fontSize = 11.sp) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Vehicle Category selection row
            val vehicleTypes = listOf(
                Pair("All", if (isBengali) "সব গাড়ি" else "All Vehicles"),
                Pair("Bus", if (isBengali) "বাস 🚌" else "Bus 🚌"),
                Pair("Launch", if (isBengali) "লঞ্চ 🚢" else "Launch 🚢"),
                Pair("Private Car", if (isBengali) "প্রাইভেটকার 🚗" else "Private Car 🚗"),
                Pair("Motorcycle", if (isBengali) "মোটরসাইকেল 🏍️" else "Motorcycle 🏍️"),
                Pair("Rent-a-Car", if (isBengali) "রেন্ট-এ-কার 🔑" else "Rent-a-Car 🔑"),
                Pair("Ambulance", if (isBengali) "অ্যাম্বুলেন্স 🚑" else "Ambulance 🚑"),
                Pair("Easybike", if (isBengali) "ইজিবাইক 🛺" else "Easybike 🛺")
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(vehicleTypes) { type ->
                    val isSel = selectedVehicleType == type.first
                    Surface(
                        onClick = { selectedVehicleType = type.first },
                        color = if (isSel) NeonCyan else DarkNavySurfaceCard,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, if (isSel) NeonCyan else GlassBorder),
                        modifier = Modifier.testTag("vehicle_tab_${type.first}")
                    ) {
                        Text(
                            text = type.second,
                            color = if (isSel) DarkNavyBackground else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Result lists
            if (filteredServices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null, tint = TextGray, modifier = Modifier.size(48.dp))
                        Text(
                            text = if (isBengali) "কোনো গাড়ি বা পরিবহন সেবা খুঁজে পাওয়া যায়নি!" else "No vehicles or transport services found!",
                            color = TextGray,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("transport_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredServices) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("transport_card_${item.id}"),
                            colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(NeonCyan.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val icon = when (item.statusOrSchedule.lowercase()) {
                                                "bus" -> Icons.Default.DirectionsBus
                                                "launch" -> Icons.Default.DirectionsBoat
                                                "motorcycle" -> Icons.Default.TwoWheeler
                                                "ambulance" -> Icons.Default.LocalHospital
                                                "easybike" -> Icons.Default.ElectricCar
                                                else -> Icons.Default.DirectionsCar
                                            }
                                            Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                                        }

                                        Column {
                                            Text(
                                                text = item.title,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = "${if (isBengali) "ধরণ:" else "Type:"} ${item.statusOrSchedule}",
                                                    color = TextCyan,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(text = "•", color = TextGray, fontSize = 11.sp)
                                                Text(
                                                    text = item.subtitle,
                                                    color = TextGray,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    // Approved status badge for admins
                                    if (isAdminLoggedIn) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (item.status == "APPROVED") NeonTeal.copy(alpha = 0.15f) else Color.Yellow.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = item.status,
                                                color = if (item.status == "APPROVED") NeonTeal else Color.Yellow,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Photo displays
                                if (item.imageUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                                    )
                                }

                                Divider(color = GlassBorder, thickness = 0.5.dp)

                                // Route details, Description & Location info
                                Text(
                                    text = item.description,
                                    color = TextWhite,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(text = if (isBengali) "ভাড়া / খরচ" else "Fare / Rate", color = TextGray, fontSize = 10.sp)
                                        Text(text = item.priceOrFee, color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp), horizontalAlignment = Alignment.End) {
                                        Text(text = if (isBengali) "লোকেশন/ঠিকানা" else "Address/Location", color = TextGray, fontSize = 10.sp)
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = RedEmergency, modifier = Modifier.size(12.dp))
                                            Text(text = item.location, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GlowButton(
                                        text = if (isBengali) "📞 বুকিং কল দিন" else "📞 Call for Booking",
                                        onClick = {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.contactPhone}"))
                                            context.startActivity(dialIntent)
                                        },
                                        containerColor = NeonTeal,
                                        textColor = DarkNavyBackground,
                                        modifier = Modifier.height(36.dp)
                                    )

                                    if (isAdminLoggedIn || item.contributor != "System") {
                                        IconButton(
                                            onClick = {
                                                viewModel.deleteDirectoryItem(item.id)
                                                Toast.makeText(context, if (isBengali) "সফলভাবে মুছে ফেলা হয়েছে!" else "Successfully deleted!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.testTag("delete_transport_${item.id}")
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedEmergency)
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

    // --- ADD SERVICE DIALOG ---
    if (showAddDialog) {
        var serviceName by remember { mutableStateOf("") }
        var serviceRouteFromTo by remember { mutableStateOf("") }
        var vehicleTypeInput by remember { mutableStateOf("Private Car") }
        var serviceFare by remember { mutableStateOf("") }
        var servicePhone by remember { mutableStateOf("") }
        var serviceLocation by remember { mutableStateOf("") }
        var serviceDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = if (isBengali) "নতুন পরিবহন বা রাইড সার্ভিস যুক্ত করুন" else "Add New Transport / Ride Service",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    GlassTextField(
                        value = serviceName,
                        onValueChange = { serviceName = it },
                        label = if (isBengali) "নাম/সেবা সরবরাহকারী" else "Name / Provider",
                        modifier = Modifier.fillMaxWidth()
                    )

                    GlassTextField(
                        value = serviceRouteFromTo,
                        onValueChange = { serviceRouteFromTo = it },
                        label = if (isBengali) "রুট (যেমন: বরিশাল - রূপাতলী)" else "Route (e.g., Barishal - Rupsha)",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = if (isBengali) "গাড়ির ধরণ নির্বাচন করুন" else "Select Vehicle Type",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    val selectorTypes = listOf("Bus", "Launch", "Private Car", "Motorcycle", "Rent-a-Car", "Ambulance", "Easybike")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(selectorTypes) { type ->
                            val sel = vehicleTypeInput == type
                            Surface(
                                onClick = { vehicleTypeInput = type },
                                color = if (sel) NeonCyan else DarkNavySurfaceCard,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = type,
                                    color = if (sel) DarkNavyBackground else Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    GlassTextField(
                        value = serviceFare,
                        onValueChange = { serviceFare = it },
                        label = if (isBengali) "ভাড়া / খরচ (যেমন: ৫০০ টাকা)" else "Fare / Rental Rate (e.g., 500 BDT)",
                        modifier = Modifier.fillMaxWidth()
                    )

                    GlassTextField(
                        value = servicePhone,
                        onValueChange = { servicePhone = it },
                        label = if (isBengali) "মোবাইল নম্বর" else "Contact Phone Number",
                        modifier = Modifier.fillMaxWidth()
                    )

                    GlassTextField(
                        value = serviceLocation,
                        onValueChange = { serviceLocation = it },
                        label = if (isBengali) "ঠিকানা / স্ট্যান্ড লোকেশন" else "Address / Stand Location",
                        modifier = Modifier.fillMaxWidth()
                    )

                    GlassTextField(
                        value = serviceDesc,
                        onValueChange = { serviceDesc = it },
                        label = if (isBengali) "বিস্তারিত বিবরণ" else "Service Description",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Direct Manual Gallery Photo Selector
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showGalleryPicker = true }
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AsyncImage(
                                model = selectedPhoto.url,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBengali) "গ্যালারি থেকে ফটো নির্বাচন করুন 📸" else "Select Photo from Gallery 📸",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBengali) selectedPhoto.titleBn else selectedPhoto.titleEn,
                                    color = TextCyan,
                                    fontSize = 10.sp
                                )
                            }
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = NeonCyan)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (serviceName.isEmpty() || serviceRouteFromTo.isEmpty() || servicePhone.isEmpty() || serviceLocation.isEmpty()) {
                            Toast.makeText(context, if (isBengali) "অনুগ্রহ করে সকল তথ্য সঠিক দিন!" else "Please fill all required fields!", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        val newItem = DirectoryItem(
                            id = "trans_${System.currentTimeMillis()}",
                            category = "transport",
                            title = serviceName,
                            subtitle = serviceRouteFromTo,
                            description = serviceDesc.ifEmpty { if (isBengali) "আরামদায়ক ও নিখুঁত পরিবহন সেবা।" else "Reliable and comfortable transportation service." },
                            location = serviceLocation,
                            contactPhone = servicePhone,
                            priceOrFee = serviceFare.ifEmpty { if (isBengali) "আলোচনা সাপেক্ষে" else "Negotiable" },
                            statusOrSchedule = vehicleTypeInput,
                            imageUrl = selectedPhoto.url,
                            status = if (isAdminLoggedIn) "APPROVED" else "PENDING",
                            contributor = if (isAdminLoggedIn) "Admin Manual" else "User Submission"
                        )
                        viewModel.addDirectoryItem(newItem)
                        showAddDialog = false
                        if (isAdminLoggedIn) {
                            Toast.makeText(context, if (isBengali) "সফল! সরাসরি যুক্ত করা হয়েছে।" else "Success! Added directly.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, if (isBengali) "সাফল্য! অ্যাডমিন অনুমোদনের পর প্রদর্শিত হবে।" else "Success! Displayed after admin approval.", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Text(text = if (isBengali) "দাখিল করুন" else "Submit", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(text = if (isBengali) "বাতিল" else "Cancel", color = TextGray)
                }
            },
            containerColor = DarkNavySurface
        )
    }

    // Gallery Picker Modal
    if (showGalleryPicker) {
        AlertDialog(
            onDismissRequest = { showGalleryPicker = false },
            title = {
                Text(
                    text = if (isBengali) "📸 গ্যালারি ফটো নির্বাচন করুন" else "📸 Select Gallery Photo",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBengali) "আপনার ফোন গ্যালারি থেকে উপযুক্ত ছবি চয়ন করুন:" else "Pick an appropriate photo from your device gallery:",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(280.dp)
                    ) {
                        items(transportGalleryPhotos) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clickable {
                                        selectedPhoto = item
                                        showGalleryPicker = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = DarkNavyBackground),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = item.url,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter)
                                            .background(Color.Black.copy(alpha = 0.65f))
                                            .padding(4.dp)
                                    ) {
                                        Text(
                                            text = if (isBengali) item.titleBn else item.titleEn,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGalleryPicker = false }) {
                    Text(text = if (isBengali) "বাতিল" else "Cancel", color = TextGray)
                }
            },
            containerColor = DarkNavySurface
        )
    }
}
