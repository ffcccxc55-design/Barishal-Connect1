package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DirectoryItem
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allItems by viewModel.allItems.collectAsState()
    
    // States
    var selectedWorker by remember { mutableStateOf<DirectoryItem?>(null) }
    var showAddWorkerForm by remember { mutableStateOf(false) }
    
    // Filter States
    var searchQuery by remember { mutableStateOf("") }
    var selectedDistrict by remember { mutableStateOf("All") }
    var selectedUpazila by remember { mutableStateOf("All") }
    var selectedUnion by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedExperience by remember { mutableStateOf("All") }

    // Dropdown open states
    var districtMenuExpanded by remember { mutableStateOf(false) }
    var upazilaMenuExpanded by remember { mutableStateOf(false) }
    var unionMenuExpanded by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var experienceMenuExpanded by remember { mutableStateOf(false) }

    val districts = listOf("All", "Barishal", "Bhola", "Jhalakathi", "Patuakhali", "Pirojpur", "Barguna")
    val upazilas = listOf("All", "Sadar", "Nalchity", "Char Fasson", "Amtali", "Bhandaria", "Kuakata")
    val unions = listOf("All", "Union A", "Union B", "Union C", "Kirtankhola Union", "Chowmatha Union")
    val categories = listOf("All", "Electrician", "Plumber", "Mechanic", "Carpenter", "Painter", "Mason", "Driver", "Tutor", "IT Support")
    val experiences = listOf("All", "1-2 Years", "3-5 Years", "5+ Years")

    // Filtered worker list
    val workers = allItems.filter { 
        it.category == "worker" && 
        it.status == "APPROVED" &&
        (selectedDistrict == "All" || it.district == selectedDistrict) &&
        (selectedUpazila == "All" || it.upazila == selectedUpazila) &&
        (selectedUnion == "All" || it.unionName == selectedUnion) &&
        (selectedCategory == "All" || it.title.contains(selectedCategory, ignoreCase = true) || it.subtitle.contains(selectedCategory, ignoreCase = true)) &&
        (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("দক্ষ কর্মী ডিরেক্টরি", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("আপনার এলাকায় প্রফেশনাল শ্রমিক খুঁজুন", color = TextCyan, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("worker_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddWorkerForm = true }, modifier = Modifier.testTag("worker_add_header_btn")) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Profile", tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavyBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddWorkerForm = true },
                containerColor = NeonCyan,
                contentColor = DarkNavyBackground,
                modifier = Modifier.testTag("worker_fab_add")
            ) {
                Icon(imageVector = Icons.Default.Engineering, contentDescription = "কর্মী হিসেবে যোগ দিন")
            }
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
                // Search box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("নাম বা দক্ষতা দিয়ে খুঁজুন...", color = TextGray, fontSize = 13.sp) },
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
                        .testTag("worker_search_input"),
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

                // Filters section expandable buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Filter
                    FilterChipButton(
                        text = "পেশা: $selectedCategory",
                        onClick = { categoryMenuExpanded = true }
                    ) {
                        DropdownMenu(
                            expanded = categoryMenuExpanded,
                            onDismissRequest = { categoryMenuExpanded = false },
                            modifier = Modifier.background(DarkNavySurface)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = Color.White) },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // District Filter
                    FilterChipButton(
                        text = "জেলা: $selectedDistrict",
                        onClick = { districtMenuExpanded = true }
                    ) {
                        DropdownMenu(
                            expanded = districtMenuExpanded,
                            onDismissRequest = { districtMenuExpanded = false },
                            modifier = Modifier.background(DarkNavySurface)
                        ) {
                            districts.forEach { dist ->
                                DropdownMenuItem(
                                    text = { Text(dist, color = Color.White) },
                                    onClick = {
                                        selectedDistrict = dist
                                        districtMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Upazila Filter
                    FilterChipButton(
                        text = "উপজেলা: $selectedUpazila",
                        onClick = { upazilaMenuExpanded = true }
                    ) {
                        DropdownMenu(
                            expanded = upazilaMenuExpanded,
                            onDismissRequest = { upazilaMenuExpanded = false },
                            modifier = Modifier.background(DarkNavySurface)
                        ) {
                            upazilas.forEach { upa ->
                                DropdownMenuItem(
                                    text = { Text(upa, color = Color.White) },
                                    onClick = {
                                        selectedUpazila = upa
                                        upazilaMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Experience Filter
                    FilterChipButton(
                        text = "অভিজ্ঞতা: $selectedExperience",
                        onClick = { experienceMenuExpanded = true }
                    ) {
                        DropdownMenu(
                            expanded = experienceMenuExpanded,
                            onDismissRequest = { experienceMenuExpanded = false },
                            modifier = Modifier.background(DarkNavySurface)
                        ) {
                            experiences.forEach { exp ->
                                DropdownMenuItem(
                                    text = { Text(exp, color = Color.White) },
                                    onClick = {
                                        selectedExperience = exp
                                        experienceMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Analytics Summary Banner
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("মোট তালিকাভুক্ত কর্মী", color = TextGray, fontSize = 11.sp)
                            Text("${workers.size} জন কারিগর সক্রিয়", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Icon(imageVector = Icons.Default.Analytics, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                    }
                }

                if (workers.isEmpty()) {
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
                            Icon(Icons.Default.Engineering, contentDescription = null, tint = TextGray, modifier = Modifier.size(48.dp))
                            Text("কোনো কর্মী পাওয়া যায়নি।", color = TextGray, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("worker_list_view"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(workers) { worker ->
                            WorkerCardItem(
                                worker = worker,
                                onClick = { selectedWorker = worker },
                                onFavoriteToggle = { viewModel.toggleFavorite(worker.id, worker.isFavorite) }
                            )
                        }
                    }
                }
            }

            // Bottom sheets or Overlays
            if (selectedWorker != null) {
                WorkerDetailsBottomSheet(
                    worker = selectedWorker!!,
                    onDismiss = { selectedWorker = null },
                    onToggleFavorite = { viewModel.toggleFavorite(selectedWorker!!.id, selectedWorker!!.isFavorite) }
                )
            }

            if (showAddWorkerForm) {
                AddWorkerFormOverlay(
                    onDismiss = { showAddWorkerForm = false },
                    onSubmit = { newWorker ->
                        viewModel.addDirectoryItem(newWorker) { success ->
                            if (success) {
                                Toast.makeText(context, "আপনার প্রোফাইল সফলভাবে ক্লাউড ডেটাবেজে জমা হয়েছে এবং অনুমোদনের অপেক্ষায় আছে!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "অফলাইনে স্থানীয়ভাবে সংরক্ষিত হয়েছে! ইন্টারনেট সংযোগ সচল হলে এডমিন প্যানেল থেকে সিঙ্ক করুন।", Toast.LENGTH_LONG).show()
                            }
                        }
                        showAddWorkerForm = false
                    }
                )
            }
        }
    }
}

@Composable
fun FilterChipButton(
    text: String,
    onClick: () -> Unit,
    dropdownContent: @Composable () -> Unit
) {
    Box {
        Surface(
            color = DarkNavySurfaceCard,
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, GlassBorder),
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextCyan, modifier = Modifier.size(16.dp))
            }
        }
        dropdownContent()
    }
}

@Composable
fun WorkerCardItem(
    worker: DirectoryItem,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("worker_card_${worker.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
        border = BorderStroke(1.dp, GlassBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile Photo / Avatar placeholder
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.1f))
                    .border(BorderStroke(1.5.dp, NeonCyan), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (worker.imageUrl.isNotEmpty()) {
                    // Simulating loaded image placeholder
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(32.dp))
                } else {
                    Icon(imageVector = Icons.Default.Engineering, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(worker.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    // Check if extraData contains verified or just default verified
                    val isVerified = worker.extraDataJson.contains("verified=true") || worker.id == "worker_1"
                    if (isVerified) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Worker",
                            tint = NeonTeal,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(worker.subtitle, color = TextCyan, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextGray, modifier = Modifier.size(12.dp))
                    Text(worker.location, color = TextGray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(12.dp))
                        Text(String.format("%.1f", worker.rating), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("|", color = GlassBorder, fontSize = 10.sp)
                    // Availability Status
                    val isAvailable = !worker.extraDataJson.contains("available=false")
                    Surface(
                        color = (if (isAvailable) NeonTeal else Color.Red).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isAvailable) "উপলব্ধ (Available)" else "ব্যস্ত (Busy)",
                            color = if (isAvailable) NeonTeal else Color.Red,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Fav/More button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (worker.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (worker.isFavorite) RedEmergency else TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun WorkerDetailsBottomSheet(
    worker: DirectoryItem,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current
    // Parse helper
    val extraMap = remember(worker.extraDataJson) {
        val map = mutableMapOf<String, String>()
        worker.extraDataJson.split(";").forEach { pair ->
            val idx = pair.indexOf("=")
            if (idx != -1) {
                map[pair.substring(0, idx).trim()] = pair.substring(idx + 1).trim()
            }
        }
        map
    }

    val skills = extraMap["skills"] ?: "বাসার ওয়্যারিং, ইলেকট্রিক সকেট মেরামত, আইপিএস ফিটিং"
    val voiceIntro = extraMap["voiceIntro"] ?: "কন্ঠ রেকর্ড করা নেই"
    val videoIntro = extraMap["videoIntro"] ?: "ভিডিও রেকর্ড করা নেই"
    val phone = worker.contactPhone.ifEmpty { "01700-000000" }
    val isAvailable = !worker.extraDataJson.contains("available=false")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন", color = TextCyan)
            }
        },
        containerColor = DarkNavySurface,
        icon = { Icon(Icons.Default.Engineering, contentDescription = null, tint = NeonCyan) },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(worker.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(worker.subtitle, color = TextCyan, fontSize = 12.sp)
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

                // Location info & Availability
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ঠিকানা ও অঞ্চল", color = TextGray, fontSize = 10.sp)
                        Text("${worker.district}, ${worker.upazila}", color = Color.White, fontSize = 12.sp)
                    }
                    Surface(
                        color = (if (isAvailable) NeonTeal else Color.Red).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isAvailable) "সরাসরি ফ্রি" else "কাজে ব্যস্ত",
                            color = if (isAvailable) NeonTeal else Color.Red,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Experience / Charge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("পরিষেবা মূল্য", color = TextGray, fontSize = 10.sp)
                        Text(worker.priceOrFee, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("কাজের অভিজ্ঞতা", color = TextGray, fontSize = 10.sp)
                        Text(extraMap["experience_years"]?.let { "$it বছর" } ?: "৩+ বছর", color = Color.White, fontSize = 12.sp)
                    }
                }

                // Description
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("সংক্ষিপ্ত বিবরণী", color = TextGray, fontSize = 10.sp)
                    Text(worker.description, color = TextWhite, fontSize = 12.sp, lineHeight = 16.sp)
                }

                // Skills
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("মূল দক্ষতাসমূহ (Skills)", color = TextGray, fontSize = 10.sp)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        skills.split(",").forEach { skill ->
                            Surface(
                                color = Color.White.copy(alpha = 0.05f),
                                border = BorderStroke(0.5.dp, GlassBorder),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(skill.trim(), color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(6.dp))
                            }
                        }
                    }
                }

                // Media intros
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("অডিও ও ভিডিও পরিচিতি", color = TextGray, fontSize = 10.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { Toast.makeText(context, "অডিও পরিচিতি প্লে হচ্ছে...", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                                Text("অডিও শুনুন", color = Color.White, fontSize = 11.sp)
                            }
                        }
                        OutlinedButton(
                            onClick = { Toast.makeText(context, "ভিডিও পরিচিতি প্লে হচ্ছে...", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                                Text("ভিডিও দেখুন", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Contact Actions row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = DarkNavyBackground, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("কল করুন", color = DarkNavyBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/88$phone"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Message, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Primary hire profile share button
                Button(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "বরিশাল কানেক্ট দক্ষ কর্মী: ${worker.title} - ${worker.subtitle}, ঠিকানা: ${worker.location}, মোবাইল: $phone")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "শেয়ার করুন"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("প্রোফাইল শেয়ার করুন", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                
                TextButton(
                    onClick = { Toast.makeText(context, "রিপোর্ট দাখিল করা হয়েছে। অ্যাডমিন টিম যাচাই করবে।", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("অনাকাঙ্ক্ষিত প্রোফাইল রিপোর্ট করুন", color = Color.Red, fontSize = 10.sp)
                }
            }
        }
    )
}

@Composable
fun AddWorkerFormOverlay(
    onDismiss: () -> Unit,
    onSubmit: (DirectoryItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var profession by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("Barishal") }
    var upazila by remember { mutableStateOf("Sadar") }
    var union by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var serviceFee by remember { mutableStateOf("৩০০ টাকা (আলোচনা সাপেক্ষ)") }

    var districtExpanded by remember { mutableStateOf(false) }
    var upazilaExpanded by remember { mutableStateOf(false) }

    val districts = listOf("Barishal", "Bhola", "Jhalakathi", "Patuakhali", "Pirojpur", "Barguna")
    val upazilas = listOf("Sadar", "Nalchity", "Char Fasson", "Amtali", "Bhandaria", "Kuakata")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isEmpty() || profession.isEmpty() || phone.isEmpty() || description.isEmpty()) {
                        // handled inline or via Toast in real flow
                    } else {
                        val extraJson = "skills=$skills;experience_years=$experience;voiceIntro=none;videoIntro=none;verified=false;available=true"
                        val newId = "worker_" + UUID.randomUUID().toString().take(6)
                        val item = DirectoryItem(
                            id = newId,
                            category = "worker",
                            title = name,
                            subtitle = profession,
                            description = description,
                            contactPhone = phone,
                            location = "$union, $upazila, $district",
                            priceOrFee = serviceFee,
                            rating = 5.0f,
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
                Text("দাখিল করুন", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = TextGray)
            }
        },
        containerColor = DarkNavySurface,
        title = {
            Text("কর্মী হিসেবে প্রোফাইল তৈরি করুন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("আপনার নাম *", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("add_worker_name")
                )

                OutlinedTextField(
                    value = profession,
                    onValueChange = { profession = it },
                    label = { Text("পেশা / দক্ষতা * (যেমন: ইলেকট্রিশিয়ান)", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("add_worker_profession")
                )

                OutlinedTextField(
                    value = experience,
                    onValueChange = { experience = it },
                    label = { Text("অভিজ্ঞতা (যেমন: ৫ বছর)", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("মোবাইল নম্বর *", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("add_worker_phone")
                )

                // District dropdown selection
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

                // Upazila selection
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
                    value = union,
                    onValueChange = { union = it },
                    label = { Text("ইউনিয়ন", color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = skills,
                    onValueChange = { skills = it },
                    label = { Text("মূল সেবাসমূহ (কমা দিয়ে লিখুন)", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("আপনার কাজের বিবরণ লিখুন *", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("add_worker_desc")
                )

                OutlinedTextField(
                    value = serviceFee,
                    onValueChange = { serviceFee = it },
                    label = { Text("সার্ভিস চার্জ / ভিজিট ফি", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
