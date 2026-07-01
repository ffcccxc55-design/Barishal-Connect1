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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DirectoryItem
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.GlowButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val bkashNum by viewModel.bkashNumber.collectAsState()
    val nagadNum by viewModel.nagadNumber.collectAsState()
    val rocketNum by viewModel.rocketNumber.collectAsState()
    val bankAcc by viewModel.bankAccount.collectAsState()
    val allItems by viewModel.allItems.collectAsState()
    val isAdmin by viewModel.isAdminLoggedIn.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedBloodGroup by remember { mutableStateOf("সব (All)") }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }

    val bloodGroups = listOf("সব (All)", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    // Filter dynamic blood donors
    val dynamicDonors = remember(allItems, selectedBloodGroup, searchQuery, isAdmin) {
        allItems.filter { item ->
            item.category == "blood" &&
            (isAdmin || item.status == "APPROVED") &&
            (selectedBloodGroup == "সব (All)" || item.title.contains(selectedBloodGroup)) &&
            (searchQuery.isEmpty() || item.subtitle.contains(searchQuery, ignoreCase = true) || item.location.contains(searchQuery, ignoreCase = true))
        }
    }

    // Default pre-population if fresh
    LaunchedEffect(allItems) {
        if (allItems.none { it.category == "blood" }) {
            viewModel.addDirectoryItem(
                DirectoryItem(
                    id = "donor_1",
                    category = "blood",
                    title = "আরিফুল ইসলাম [A+]",
                    subtitle = "A+ (Positive)",
                    description = "জরুরি প্রয়োজনে বরিশাল সদরের যেকোনো স্থানে রক্ত দিতে ইচ্ছুক। লাস্ট ডোনেশন: ৩ মাস পূর্বে।",
                    location = "সদর রোড, বরিশাল",
                    contactPhone = "01711-223344",
                    rating = 5.0f,
                    status = "APPROVED"
                )
            )
            viewModel.addDirectoryItem(
                DirectoryItem(
                    id = "donor_2",
                    category = "blood",
                    title = "মো: জাহিদ হাসান [O+]",
                    subtitle = "O+ (Positive)",
                    description = "বরিশাল শহর ও আশেপাশের হাসপাতালে রক্ত দিতে প্রস্তুত। বাইক আছে, যেকোনো সময়ে কল দিতে পারেন।",
                    location = "রূপাতলী, বরিশাল",
                    contactPhone = "01815-998877",
                    rating = 5.0f,
                    status = "APPROVED"
                )
            )
            viewModel.addDirectoryItem(
                DirectoryItem(
                    id = "donor_3",
                    category = "blood",
                    title = "ফারজানা রহমান [B+]",
                    subtitle = "B+ (Positive)",
                    description = "জরুরি প্রয়োজনে রক্তদানে ইচ্ছুক। দিনে যাতায়াত সুবিধা থাকলে যেকোনো হাসপাতালে যাবো।",
                    location = "চৌমাথা, বরিশাল",
                    contactPhone = "01912-554433",
                    rating = 5.0f,
                    status = "APPROVED"
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "সাহায্য ও রক্তদান", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("donation_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showRegisterDialog = true }, modifier = Modifier.testTag("register_donor_button")) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Register as Donor", tint = NeonCyan)
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
            // 1. Hurricane Relief / Donation Banner
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(imageVector = Icons.Default.VolunteerActivism, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(32.dp))
                                Text(text = "বরিশাল বন্যা ও পুনর্বাসন তহবিল", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Text(
                            text = "সাম্প্রতিক প্রলয়ঙ্করী ঝড়ে ক্ষতিগ্রস্ত ভোলা, পটুয়াখালী ও বরগুনার উপকূলবর্তী চরাঞ্চলের ভাসমান মানুষের জন্য শুকনো খাবার ও পুনর্বাসন সহায়তা তহবিল সংগ্রহ করা হচ্ছে। আপনার সাহায্য পৌঁছে দিন।",
                            color = TextGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        // Payment Numbers Rows
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = DarkNavySurface,
                                border = BorderStroke(1.dp, GlassBorder),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = "বিকাশ (bKash)", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        IconButton(
                                            onClick = {
                                                val num = bkashNum.ifEmpty { "01700-000000" }
                                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(num))
                                                Toast.makeText(context, "বিকাশ নম্বরটি কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonCyan, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                    Text(text = bkashNum.ifEmpty { "01700-000000" }, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "কিউআর (QR) কোড দেখুন",
                                        color = TextGray,
                                        fontSize = 8.sp,
                                        modifier = Modifier.clickable { showQrDialog = true }.padding(top = 2.dp)
                                    )
                                }
                            }
                            
                            Surface(
                                color = DarkNavySurface,
                                border = BorderStroke(1.dp, GlassBorder),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = "নগদ (Nagad)", color = RedEmergency, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        IconButton(
                                            onClick = {
                                                val num = nagadNum.ifEmpty { "01800-000000" }
                                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(num))
                                                Toast.makeText(context, "নগদ নম্বরটি কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = RedEmergency, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                    Text(text = nagadNum.ifEmpty { "01800-000000" }, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "কিউআর (QR) কোড দেখুন",
                                        color = TextGray,
                                        fontSize = 8.sp,
                                        modifier = Modifier.clickable { showQrDialog = true }.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        Surface(
                            color = DarkNavySurface,
                            border = BorderStroke(1.dp, GlassBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "ব্যাংক একাউন্ট এবং অন্যান্য তথ্য:", color = NeonTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = bankAcc.ifEmpty { "সোনালী ব্যাংক, বরিশাল শাখা\nঅ্যাকাউন্ট: ১২৩৪৫৬৭৮৯০" }, color = Color.White, fontSize = 12.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }

            // Transaction Verification Submit Form Card
            item {
                var donorPhoneInput by remember { mutableStateOf("") }
                var amountInput by remember { mutableStateOf("") }
                var trxIdInput by remember { mutableStateOf("") }
                var selectedMethod by remember { mutableStateOf("bKash") }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                            Text(text = "সহায়তার তথ্য সাবমিট করুন (Verify Payment)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "উপরে উল্লেখিত নম্বরে টাকা পাঠানোর পর নিচের ফর্মে আপনার মোবাইল নম্বর, টাকার পরিমাণ ও ট্রানজেকশন আইডি (TrxID) দিয়ে সাবমিট করুন।",
                            color = TextGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("bKash", "Nagad", "Rocket").forEach { m ->
                                val sel = selectedMethod == m
                                Surface(
                                    onClick = { selectedMethod = m },
                                    color = if (sel) NeonTeal else DarkNavySurfaceCard,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (sel) NeonTeal else GlassBorder),
                                    modifier = Modifier.weight(1f).clickable { selectedMethod = m }
                                ) {
                                    Text(
                                        text = m,
                                        color = if (sel) DarkNavyBackground else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth()
                                    )
                                }
                            }
                        }

                        GlassTextField(
                            value = donorPhoneInput,
                            onValueChange = { donorPhoneInput = it },
                            label = "আপনার মোবাইল নম্বর",
                            placeholder = "যেমন: ০১৭১২৩৪৫৬৭৮",
                            testTag = "donation_sender_phone"
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                GlassTextField(
                                    value = amountInput,
                                    onValueChange = { amountInput = it },
                                    label = "টাকার পরিমাণ",
                                    placeholder = "যেমন: ৫০০",
                                    testTag = "donation_amount_input"
                                )
                            }
                            Box(modifier = Modifier.weight(1.2f)) {
                                GlassTextField(
                                    value = trxIdInput,
                                    onValueChange = { trxIdInput = it },
                                    label = "ট্রানজেকশন আইডি (TrxID)",
                                    placeholder = "যেমন: KJS82GD8",
                                    testTag = "donation_trx_id_input"
                                )
                            }
                        }

                        GlowButton(
                            text = "তথ্য সাবমিট করুন (Submit)",
                            onClick = {
                                if (donorPhoneInput.isEmpty() || amountInput.isEmpty() || trxIdInput.isEmpty()) {
                                    Toast.makeText(context, "অনুগ্রহ করে সব তথ্য সঠিক উপায়ে পূরণ করুন!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.addDonationActivity(selectedMethod, donorPhoneInput, amountInput, trxIdInput)
                                    donorPhoneInput = ""
                                    amountInput = ""
                                    trxIdInput = ""
                                    Toast.makeText(context, "আপনার সহায়তার তথ্য সফলভাবে দাখিল করা হয়েছে। অ্যাডমিন প্যানেল থেকে ভেরিফাই করা হবে। ধন্যবাদ!", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "donation_submit_btn"
                        )
                    }
                }
            }

            // 2. Search & Filter Header
            item {
                SectionHeader(title = "জরুরি রক্তদাতা ডিরেক্টরি", subtitle = "রক্তের গ্রুপ ও এলাকা দিয়ে সরাসরি রক্তদাতাদের খুঁজুন")
            }

            // Search Bar & Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Search Location TextField
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextGray)
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(text = "এলাকা দিয়ে খুঁজুন (যেমন: রূপাতলী)...", color = TextGray, fontSize = 12.sp)
                                }
                                androidx.compose.foundation.text.BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = Color.White, modifier = Modifier.clickable { searchQuery = "" })
                            }
                        }
                    }

                    // Blood Group selection list
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(bloodGroups) { grp ->
                            val sel = selectedBloodGroup == grp
                            Surface(
                                onClick = { selectedBloodGroup = grp },
                                color = if (sel) RedEmergency else DarkNavySurfaceCard,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, if (sel) RedEmergency else GlassBorder)
                            ) {
                                Text(
                                    text = grp,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Dynamic Donors List
            if (dynamicDonors.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, tint = TextGray, modifier = Modifier.size(36.dp))
                            Text(text = "কোনো রক্তদাতা খুঁজে পাওয়া যায়নি", color = TextGray, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(dynamicDonors) { item ->
                    val isApproved = item.status == "APPROVED"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("donor_card_${item.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                        border = BorderStroke(1.dp, if (isApproved) GlassBorder else RedEmergency.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Blood Group Badge circle
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(RedEmergency.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.title.substringAfter("[").substringBefore("]").trim(),
                                            color = RedEmergency,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }

                                    Column {
                                        Text(text = item.title.substringBefore("[").trim(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(text = item.location, color = TextCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(
                                        onClick = {
                                            val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.contactPhone}"))
                                            context.startActivity(dial)
                                        },
                                        modifier = Modifier
                                            .background(NeonTeal.copy(alpha = 0.15f), CircleShape)
                                            .size(36.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Call, contentDescription = "Call Donor", tint = NeonTeal, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            // Description Bio
                            Text(text = item.description, color = TextGray, fontSize = 11.sp, lineHeight = 15.sp)

                            // Admin Approvals buttons inline
                            if (isAdmin && item.status == "PENDING") {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.updateDirectoryItemStatus(item.id, "APPROVED")
                                            Toast.makeText(context, "রক্তদাতা সফলভাবে অনুমোদিত হয়েছে!", Toast.LENGTH_SHORT).show()
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
                                        Text(text = "বাতিল", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Register Blood Donor Form Dialog
    if (showRegisterDialog) {
        var donorName by remember { mutableStateOf("") }
        var donorGrp by remember { mutableStateOf("A+") }
        var donorLoc by remember { mutableStateOf("") }
        var donorPhone by remember { mutableStateOf("") }
        var donorBio by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRegisterDialog = false },
            title = { Text(text = "রক্তদাতা হিসেবে রেজিস্ট্রেশন", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    GlassTextField(value = donorName, onValueChange = { donorName = it }, label = "আপনার নাম", modifier = Modifier.fillMaxWidth())
                    
                    Text(text = "রক্তের গ্রুপ", color = RedEmergency, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(bloodGroups.filter { it != "সব (All)" }) { g ->
                            val s = donorGrp == g
                            Surface(
                                onClick = { donorGrp = g },
                                color = if (s) RedEmergency else DarkNavySurfaceCard,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = g, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    GlassTextField(value = donorLoc, onValueChange = { donorLoc = it }, label = "বর্তমান ঠিকানা ও এলাকা", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = donorPhone, onValueChange = { donorPhone = it }, label = "মোবাইল নম্বর", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = donorBio, onValueChange = { donorBio = it }, label = "সংক্ষিপ্ত বিবরণ ও লাস্ট রক্তদানের তারিখ", modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (donorName.isEmpty() || donorLoc.isEmpty() || donorPhone.isEmpty()) {
                            Toast.makeText(context, "অনুগ্রহ করে নাম, ঠিকানা ও মোবাইল লিখুন!", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        val newItem = DirectoryItem(
                            id = "donor_${System.currentTimeMillis()}",
                            category = "blood",
                            title = "$donorName [$donorGrp]",
                            subtitle = "$donorGrp (Positive)",
                            description = donorBio.ifEmpty { "প্রয়োজনে রক্তদানে ইচ্ছুক।" },
                            location = donorLoc,
                            contactPhone = donorPhone,
                            rating = 5.0f,
                            status = "PENDING"
                        )
                        viewModel.addDirectoryItem(newItem)
                        showRegisterDialog = false
                        Toast.makeText(context, "দাখিল হয়েছে! অ্যাডমিন অনুমোদনের পর ডিরেক্টরিতে আসবে।", Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text(text = "নিবন্ধন করুন", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegisterDialog = false }) {
                    Text(text = "বাতিল", color = TextGray)
                }
            },
            containerColor = DarkNavySurface
        )
    }

    // QR Code visual dialog
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text(text = "সহজ উপায়ে দান করুন", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Simulated QR Code vector
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing simulated QR lines with canvas
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val cellSize = size.width / 5
                            drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(cellSize * 1.5f, cellSize * 1.5f))
                            drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(cellSize * 1.5f, cellSize * 1.5f), topLeft = androidx.compose.ui.geometry.Offset(size.width - cellSize * 1.5f, 0f))
                            drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(cellSize * 1.5f, cellSize * 1.5f), topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - cellSize * 1.5f))
                            
                            // Center random QR blocks
                            drawRect(color = Color.DarkGray, size = androidx.compose.ui.geometry.Size(cellSize, cellSize), topLeft = androidx.compose.ui.geometry.Offset(cellSize * 2f, cellSize * 2f))
                            drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(cellSize, cellSize), topLeft = androidx.compose.ui.geometry.Offset(cellSize * 3f, cellSize * 2.5f))
                            drawRect(color = Color.DarkGray, size = androidx.compose.ui.geometry.Size(cellSize, cellSize), topLeft = androidx.compose.ui.geometry.Offset(cellSize, cellSize * 3f))
                        }
                    }
                    Text(
                        text = "মোবাইল ব্যাংকিং অ্যাপ দিয়ে কিউআর কোড স্ক্যান করে সরাসরি সাহায্য পাঠান।",
                        color = TextGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showQrDialog = false }) {
                    Text(text = "ঠিক আছে", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkNavySurface
        )
    }
}
