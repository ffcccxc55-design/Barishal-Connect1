package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import com.example.data.model.DirectoryItem
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allItems by viewModel.allItems.collectAsState()

    // Screen States
    var currentTab by remember { mutableStateOf(0) } // 0: Find Jobs, 1: My Applications & Applicants
    var selectedJob by remember { mutableStateOf<DirectoryItem?>(null) }
    var showPostJobForm by remember { mutableStateOf(false) }
    var showApplyForm by remember { mutableStateOf<DirectoryItem?>(null) }

    // Job Search Filters
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedDistrict by remember { mutableStateOf("All") }
    var selectedUpazila by remember { mutableStateOf("All") }
    var selectedSalaryRange by remember { mutableStateOf("All") }

    // Filter expansions
    var categoryExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }
    var upazilaExpanded by remember { mutableStateOf(false) }

    val categories = listOf("All", "IT & Software", "Education/Teaching", "Hospitality", "Sales & Marketing", "Construction", "Garments", "Healthcare", "Driver/Delivery")
    val districts = listOf("All", "Barishal", "Bhola", "Jhalakathi", "Patuakhali", "Barguna", "Pirojpur")
    val upazilas = listOf("All", "Sadar", "Nalchity", "Char Fasson", "Amtali", "Kuakata")

    // Filter jobs
    val jobs = allItems.filter {
        it.category == "job" &&
        it.status == "APPROVED" &&
        (selectedCategory == "All" || it.subtitle.contains(selectedCategory, ignoreCase = true) || it.title.contains(selectedCategory, ignoreCase = true)) &&
        (selectedDistrict == "All" || it.district == selectedDistrict) &&
        (selectedUpazila == "All" || it.upazila == selectedUpazila) &&
        (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("চাকরি ও নিয়োগ পোর্টাল", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("বরিশাল বিভাগের নতুন কর্মসংস্থান খুঁজুন", color = TextCyan, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("job_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showPostJobForm = true }, modifier = Modifier.testTag("job_add_header_btn")) {
                        Icon(imageVector = Icons.Default.PostAdd, contentDescription = "Post Job", tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavyBackground)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showPostJobForm = true },
                containerColor = NeonCyan,
                contentColor = DarkNavyBackground,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("নতুন নিয়োগ দিন", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("job_fab_post")
            )
        },
        containerColor = DarkNavyBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Navigation Tabs (Find Jobs vs Applications)
                TabRow(
                    selectedTabIndex = currentTab,
                    containerColor = DarkNavySurface,
                    contentColor = NeonCyan,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        text = { Text("💼 চাকরি খুঁজুন", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        text = { Text("📄 আবেদন ও প্রার্থী তালিকা", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                if (currentTab == 0) {
                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("পদবী, কোম্পানি বা দক্ষতা দিয়ে খুঁজুন...", color = TextGray, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextCyan) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextGray)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("job_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = GlassBorder,
                            focusedContainerColor = DarkNavySurface,
                            unfocusedContainerColor = DarkNavySurface
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Filters Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category Chip
                        FilterChipButton(
                            text = "ক্যাটাগরি: $selectedCategory",
                            onClick = { categoryExpanded = true }
                        ) {
                            DropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false },
                                modifier = Modifier.background(DarkNavySurface)
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, color = Color.White) },
                                        onClick = {
                                            selectedCategory = cat
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // District Chip
                        FilterChipButton(
                            text = "জেলা: $selectedDistrict",
                            onClick = { districtExpanded = true }
                        ) {
                            DropdownMenu(
                                expanded = districtExpanded,
                                onDismissRequest = { districtExpanded = false },
                                modifier = Modifier.background(DarkNavySurface)
                            ) {
                                districts.forEach { dist ->
                                    DropdownMenuItem(
                                        text = { Text(dist, color = Color.White) },
                                        onClick = {
                                            selectedDistrict = dist
                                            districtExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Upazila Chip
                        FilterChipButton(
                            text = "উপজেলা: $selectedUpazila",
                            onClick = { upazilaExpanded = true }
                        ) {
                            DropdownMenu(
                                expanded = upazilaExpanded,
                                onDismissRequest = { upazilaExpanded = false },
                                modifier = Modifier.background(DarkNavySurface)
                            ) {
                                upazilas.forEach { upa ->
                                    DropdownMenuItem(
                                        text = { Text(upa, color = Color.White) },
                                        onClick = {
                                            selectedUpazila = upa
                                            upazilaExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Jobs listing
                    if (jobs.isEmpty()) {
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
                                Icon(Icons.Default.WorkOutline, contentDescription = null, tint = TextGray, modifier = Modifier.size(48.dp))
                                Text("কোনো নিয়োগ বিজ্ঞপ্তি পাওয়া যায়নি।", color = TextGray, fontSize = 13.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("job_list_view"),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(jobs) { job ->
                                JobCardItem(
                                    job = job,
                                    onClick = { selectedJob = job },
                                    onFavoriteToggle = { viewModel.toggleFavorite(job.id, job.isFavorite) }
                                )
                            }
                        }
                    }
                } else {
                    // Applications & Applicants Management Panel
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "আমার আবেদনসমূহ (My Applications)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        // Static lists simulating applied status
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("লেকচারার (কম্পিউটার সায়েন্স)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Surface(
                                            color = NeonTeal.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("APPROVED & SHORTLISTED", color = NeonTeal, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    Text("প্রতিষ্ঠানের নাম: বরিশাল স্কলার্স কলেজ", color = TextCyan, fontSize = 11.sp)
                                    Text("আবেদনের তারিখ: ২৫ জুন ২০২৬", color = TextGray, fontSize = 10.sp)
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "কোম্পানির জন্য প্রাপ্ত প্রার্থী তালিকা (Applicants Received)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        // Static/Dynamic Mock Applicants List
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("রনি হালদার (Roni Halder)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("মোবাইল: 01712-998877", color = NeonCyan, fontSize = 11.sp)
                                    }
                                    Text("পদবী: আইটি অ্যাসিস্ট্যান্ট (কম্পিউটার ল্যাব)", color = TextWhite, fontSize = 11.sp)
                                    Text("অভিজ্ঞতা: ২ বছর | শিক্ষাগত যোগ্যতা: ডিপ্লোমা ইন ইঞ্জিনিয়ারিং", color = TextGray, fontSize = 10.sp)
                                    Text("বিবরণ: আমি বরিশালের রূপাতলী অঞ্চলের বাসিন্দা। হার্ডওয়্যার এবং নেটওয়ার্কিং কাজের ভালো দক্ষতা রয়েছে।", color = TextWhite.copy(alpha = 0.8f), fontSize = 11.sp, lineHeight = 15.sp)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:01712998877"))
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("কল করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { Toast.makeText(context, "প্রার্থীকে শর্টলিস্ট করা হয়েছে!", Toast.LENGTH_SHORT).show() },
                                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("শর্টলিস্ট", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sheets and Details Overlay
            if (selectedJob != null) {
                JobDetailsOverlay(
                    job = selectedJob!!,
                    onDismiss = { selectedJob = null },
                    onApply = { 
                        showApplyForm = selectedJob
                        selectedJob = null
                    }
                )
            }

            if (showApplyForm != null) {
                ApplyFormOverlay(
                    job = showApplyForm!!,
                    onDismiss = { showApplyForm = null },
                    onSubmit = {
                        Toast.makeText(context, "চাকরির আবেদনটি কোম্পানির কাছে সফলভাবে পাঠানো হয়েছে!", Toast.LENGTH_LONG).show()
                        showApplyForm = null
                    }
                )
            }

            if (showPostJobForm) {
                PostJobFormOverlay(
                    onDismiss = { showPostJobForm = false },
                    onSubmit = { newJob ->
                        viewModel.addDirectoryItem(newJob)
                        Toast.makeText(context, "আপনার নিয়োগ বিজ্ঞপ্তিটি সফলভাবে পেন্ডিং তালিকায় জমা হয়েছে! এডমিন যাচাইয়ের পর প্রকাশিত হবে।", Toast.LENGTH_LONG).show()
                        showPostJobForm = false
                    }
                )
            }
        }
    }
}

@Composable
fun JobCardItem(
    job: DirectoryItem,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("job_card_${job.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
        border = BorderStroke(1.dp, GlassBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = job.subtitle, // Holds Company Name
                        color = TextCyan,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (job.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (job.isFavorite) RedEmergency else TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Location
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextGray, modifier = Modifier.size(12.dp))
                    Text(job.location, color = TextGray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                // Salary
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(12.dp))
                    Text(job.priceOrFee, color = NeonTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = job.description,
                color = TextWhite.copy(alpha = 0.8f),
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Deadline / Status Schedule holds Deadline
                Text(
                    text = "আবেদনের শেষ তারিখ: ${job.statusOrSchedule}",
                    color = TextGray,
                    fontSize = 9.sp
                )

                Surface(
                    color = NeonCyan.copy(alpha = 0.1f),
                    border = BorderStroke(0.5.dp, NeonCyan.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "আবেদন করুন",
                        color = NeonCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun JobDetailsOverlay(
    job: DirectoryItem,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    val context = LocalContext.current
    // Parse helper
    val extraMap = remember(job.extraDataJson) {
        val map = mutableMapOf<String, String>()
        job.extraDataJson.split(";").forEach { pair ->
            val idx = pair.indexOf("=")
            if (idx != -1) {
                map[pair.substring(0, idx).trim()] = pair.substring(idx + 1).trim()
            }
        }
        map
    }

    val vacancy = extraMap["vacancy"] ?: "১ জন"
    val workingTime = extraMap["workingTime"] ?: "সকাল ৯:০০ - বিকাল ৫:০০"
    val phone = job.contactPhone.ifEmpty { "01711-000000" }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন", color = TextCyan)
            }
        },
        containerColor = DarkNavySurface,
        icon = { Icon(Icons.Default.Work, contentDescription = null, tint = NeonCyan) },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(job.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                Text(job.subtitle, color = TextCyan, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Divider(color = GlassBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("বেতন (Salary)", color = TextGray, fontSize = 10.sp)
                        Text(job.priceOrFee, color = NeonTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("শূন্যপদ (Vacancy)", color = TextGray, fontSize = 10.sp)
                        Text(vacancy, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("কাজের সময়", color = TextGray, fontSize = 10.sp)
                        Text(workingTime, color = Color.White, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("শেষ সময় (Deadline)", color = TextGray, fontSize = 10.sp)
                        Text(job.statusOrSchedule, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("কাজের স্থান ও ঠিকানা", color = TextGray, fontSize = 10.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(12.dp))
                        Text(job.location, color = Color.White, fontSize = 12.sp)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("নিয়োগ বিবরণী ও দায়িত্ব", color = TextGray, fontSize = 10.sp)
                    Text(job.description, color = TextWhite, fontSize = 12.sp, lineHeight = 16.sp)
                }

                // Contact Actions row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onApply,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = DarkNavyBackground, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("আবেদন করুন", color = DarkNavyBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("কল করুন", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Share button
                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "বরিশাল কানেক্ট নিয়োগ বিজ্ঞপ্তি: ${job.title} - ${job.subtitle}, বেতন: ${job.priceOrFee}, শেষ সময়: ${job.statusOrSchedule}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "শেয়ার করুন"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("শেয়ার করুন", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    )
}

@Composable
fun ApplyFormOverlay(
    job: DirectoryItem,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    var resumeName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (phone.isNotEmpty() && resumeName.isNotEmpty()) {
                        onSubmit()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("নিশ্চিত করুন", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = TextGray)
            }
        },
        containerColor = DarkNavySurface,
        title = {
            Text("চাকরির আবেদন ফরম", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("আবেদনের পদবী: ${job.title}", color = TextCyan, fontSize = 12.sp)
                Text("কোম্পানি: ${job.subtitle}", color = Color.White, fontSize = 11.sp)

                OutlinedTextField(
                    value = resumeName,
                    onValueChange = { resumeName = it },
                    label = { Text("আপনার সিভি/রেজুমে টাইটেল বা লিঙ্ক *", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("apply_resume_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর *", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("apply_phone_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("কভার লেটার বা অতিরিক্ত অভিজ্ঞতা লিখুন", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
fun PostJobFormOverlay(
    onDismiss: () -> Unit,
    onSubmit: (DirectoryItem) -> Unit
) {
    var companyName by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("৩০ জুলাই ২০২৬") }
    var vacancy by remember { mutableStateOf("১ জন") }
    var workingTime by remember { mutableStateOf("সকাল ৯:০০ - বিকাল ৫:০০") }

    var district by remember { mutableStateOf("Barishal") }
    var upazila by remember { mutableStateOf("Sadar") }
    var union by remember { mutableStateOf("") }

    var districtExpanded by remember { mutableStateOf(false) }
    var upazilaExpanded by remember { mutableStateOf(false) }

    val districts = listOf("Barishal", "Bhola", "Jhalakathi", "Patuakhali", "Barguna", "Pirojpur")
    val upazilas = listOf("Sadar", "Nalchity", "Char Fasson", "Amtali", "Kuakata")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (companyName.isEmpty() || jobTitle.isEmpty() || salary.isEmpty() || phone.isEmpty() || description.isEmpty()) {
                        // inline error
                    } else {
                        val extraJson = "vacancy=$vacancy;workingTime=$workingTime"
                        val newId = "job_" + UUID.randomUUID().toString().take(6)
                        val item = DirectoryItem(
                            id = newId,
                            category = "job",
                            title = jobTitle,
                            subtitle = companyName, // Holds Company Name
                            description = description,
                            contactPhone = phone,
                            location = "$location, $upazila, $district",
                            priceOrFee = salary, // Holds Salary
                            statusOrSchedule = deadline, // Holds Deadline
                            status = "PENDING",
                            contributor = "Sakib Ahmed",
                            district = district,
                            upazila = upazila,
                            unionName = union.ifEmpty { "Union A" },
                            extraDataJson = extraJson
                        )
                        onSubmit(item)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("বিজ্ঞপ্তি দিন", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = TextGray)
            }
        },
        containerColor = DarkNavySurface,
        title = {
            Text("নতুন নিয়োগ বিজ্ঞপ্তি দিন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("কোম্পানি বা নিয়োগকারীর নাম *", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("post_job_company")
                )

                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text("চাকরির পদবী * (যেমন: কম্পিউটার অপারেটর)", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("post_job_title")
                )

                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it },
                    label = { Text("মাসিক বেতন * (যেমন: ১৫,০০০ টাকা)", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("post_job_salary")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("যোগাযোগের মোবাইল নম্বর *", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("post_job_phone")
                )

                // District Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { districtExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("জেলা: $district", color = Color.White)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextCyan)
                        }
                    }
                    DropdownMenu(expanded = districtExpanded, onDismissRequest = { districtExpanded = false }, modifier = Modifier.background(DarkNavySurface)) {
                        districts.forEach { dist ->
                            DropdownMenuItem(text = { Text(dist, color = Color.White) }, onClick = { district = dist; districtExpanded = false })
                        }
                    }
                }

                // Upazila Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { upazilaExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("উপজেলা: $upazila", color = Color.White)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextCyan)
                        }
                    }
                    DropdownMenu(expanded = upazilaExpanded, onDismissRequest = { upazilaExpanded = false }, modifier = Modifier.background(DarkNavySurface)) {
                        upazilas.forEach { upa ->
                            DropdownMenuItem(text = { Text(upa, color = Color.White) }, onClick = { upazila = upa; upazilaExpanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("নির্দিষ্ট কর্মস্থল ঠিকানা (যেমন: সদর রোড, বরিশাল) *", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = vacancy,
                    onValueChange = { vacancy = it },
                    label = { Text("শূন্যপদ সংখ্যা (Vacancy)", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = workingTime,
                    onValueChange = { workingTime = it },
                    label = { Text("কাজের সময়সূচী", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("আবেদনের শেষ তারিখ (Deadline)", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("চাকরির বিস্তারিত বিবরণ ও শিক্ষাগত যোগ্যতা *", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("post_job_desc")
                )
            }
        }
    )
}
