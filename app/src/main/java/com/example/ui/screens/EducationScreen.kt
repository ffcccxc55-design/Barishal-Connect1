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
fun EducationScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allItems by viewModel.allItems.collectAsState()

    var selectedSubCategory by remember { mutableStateOf("All") } // School, College, University, Madrasa, Technical Institute
    var selectedSchoolDetail by remember { mutableStateOf<DirectoryItem?>(null) }
    var showAddSchoolForm by remember { mutableStateOf(false) }

    // Search filters
    var searchQuery by remember { mutableStateOf("") }
    var selectedDistrict by remember { mutableStateOf("All") }
    var selectedUpazila by remember { mutableStateOf("All") }
    var selectedBoard by remember { mutableStateOf("All") }

    var districtExpanded by remember { mutableStateOf(false) }
    var upazilaExpanded by remember { mutableStateOf(false) }
    var boardExpanded by remember { mutableStateOf(false) }

    val boards = listOf("All", "Barishal", "Dhaka", "Madrasah Board", "Technical Board")
    val districts = listOf("All", "Barishal", "Bhola", "Jhalakathi", "Patuakhali", "Barguna", "Pirojpur")
    val upazilas = listOf("All", "Sadar", "Nalchity", "Char Fasson", "Amtali", "Kuakata")

    // Filtered institutions
    val schools = allItems.filter {
        it.category == "school" &&
        it.status == "APPROVED" &&
        (selectedSubCategory == "All" || it.subtitle.contains(selectedSubCategory, ignoreCase = true)) &&
        (selectedDistrict == "All" || it.district == selectedDistrict) &&
        (selectedUpazila == "All" || it.upazila == selectedUpazila) &&
        (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true))
    }

    val tabs = listOf("All", "School", "College", "University", "Madrasa", "Technical")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("শিক্ষা প্রতিষ্ঠান ডিরেক্টরি", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("স্কুল, কলেজ, মাদ্রাসা ও বিশ্ববিদ্যালয় ডিরেক্টরি", color = TextCyan, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("edu_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddSchoolForm = true }, modifier = Modifier.testTag("edu_add_header_btn")) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add School", tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavyBackground)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSchoolForm = true },
                containerColor = NeonCyan,
                contentColor = DarkNavyBackground,
                icon = { Icon(Icons.Default.School, contentDescription = null) },
                text = { Text("প্রতিষ্ঠান যোগ করুন", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("edu_fab_add")
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
                // Categories Horizontal Scroll Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEach { tab ->
                        val isSelected = selectedSubCategory == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) NeonCyan else DarkNavySurfaceCard)
                                .border(BorderStroke(1.dp, if (isSelected) NeonCyan else GlassBorder), RoundedCornerShape(50))
                                .clickable { selectedSubCategory = tab }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tab,
                                color = if (isSelected) DarkNavyBackground else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // General Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("প্রতিষ্ঠানের নাম বা EIIN কোড দিয়ে খুঁজুন...", color = TextGray, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextCyan) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edu_search_input"),
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

                // Advanced filters Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Board Filter
                    FilterChipButton(
                        text = "বোর্ড: $selectedBoard",
                        onClick = { boardExpanded = true }
                    ) {
                        DropdownMenu(expanded = boardExpanded, onDismissRequest = { boardExpanded = false }, modifier = Modifier.background(DarkNavySurface)) {
                            boards.forEach { brd ->
                                DropdownMenuItem(text = { Text(brd, color = Color.White) }, onClick = { selectedBoard = brd; boardExpanded = false })
                            }
                        }
                    }

                    // District Filter
                    FilterChipButton(
                        text = "জেলা: $selectedDistrict",
                        onClick = { districtExpanded = true }
                    ) {
                        DropdownMenu(expanded = districtExpanded, onDismissRequest = { districtExpanded = false }, modifier = Modifier.background(DarkNavySurface)) {
                            districts.forEach { dist ->
                                DropdownMenuItem(text = { Text(dist, color = Color.White) }, onClick = { selectedDistrict = dist; districtExpanded = false })
                            }
                        }
                    }

                    // Upazila Filter
                    FilterChipButton(
                        text = "উপজেলা: $selectedUpazila",
                        onClick = { upazilaExpanded = true }
                    ) {
                        DropdownMenu(expanded = upazilaExpanded, onDismissRequest = { upazilaExpanded = false }, modifier = Modifier.background(DarkNavySurface)) {
                            upazilas.forEach { upa ->
                                DropdownMenuItem(text = { Text(upa, color = Color.White) }, onClick = { selectedUpazila = upa; upazilaExpanded = false })
                            }
                        }
                    }
                }

                // Schools lists
                if (schools.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("কোনো শিক্ষা প্রতিষ্ঠান খুঁজে পাওয়া যায়নি।", color = TextGray, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("school_list_view"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(schools) { school ->
                            SchoolCardItem(
                                school = school,
                                onClick = { selectedSchoolDetail = school },
                                onFavoriteToggle = { viewModel.toggleFavorite(school.id, school.isFavorite) }
                            )
                        }
                    }
                }
            }

            // Sheets & overlays
            if (selectedSchoolDetail != null) {
                SchoolDetailsOverlay(
                    school = selectedSchoolDetail!!,
                    onDismiss = { selectedSchoolDetail = null }
                )
            }

            if (showAddSchoolForm) {
                AddSchoolFormOverlay(
                    onDismiss = { showAddSchoolForm = false },
                    onSubmit = { newSchool ->
                        viewModel.addDirectoryItem(newSchool)
                        Toast.makeText(context, "প্রতিষ্ঠানটি সফলভাবে পেন্ডিং তালিকায় জমা হয়েছে! এডমিন যাচাইয়ের পর প্রকাশিত হবে।", Toast.LENGTH_LONG).show()
                        showAddSchoolForm = false
                    }
                )
            }
        }
    }
}

@Composable
fun SchoolCardItem(
    school: DirectoryItem,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("school_card_${school.id}"),
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
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonCyan.copy(alpha = 0.1f))
                    .border(BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f)), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.School, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(28.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(school.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(school.subtitle, color = TextCyan, fontSize = 11.sp)
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextGray, modifier = Modifier.size(12.dp))
                    Text(school.location, color = TextGray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (school.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (school.isFavorite) RedEmergency else TextGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SchoolDetailsOverlay(
    school: DirectoryItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val extraMap = remember(school.extraDataJson) {
        val map = mutableMapOf<String, String>()
        school.extraDataJson.split(";").forEach { pair ->
            val idx = pair.indexOf("=")
            if (idx != -1) {
                map[pair.substring(0, idx).trim()] = pair.substring(idx + 1).trim()
            }
        }
        map
    }

    val eiin = extraMap["eiin"] ?: "১০০২৮১"
    val board = extraMap["board"] ?: "Barishal"
    val principal = extraMap["principal"] ?: "প্রফেসর ড. মো: রফিকুল ইসলাম"
    val teachers = extraMap["teachers"] ?: "৪৫ জন"
    val students = extraMap["students"] ?: "১২০০ জন"
    val web = extraMap["web"] ?: "www.barishalschool.edu.bd"

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন", color = TextCyan)
            }
        },
        containerColor = DarkNavySurface,
        icon = { Icon(Icons.Default.School, contentDescription = null, tint = NeonCyan) },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(school.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                Text(school.subtitle, color = TextCyan, fontSize = 12.sp, textAlign = TextAlign.Center)
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
                        Text("EIIN কোড", color = TextGray, fontSize = 10.sp)
                        Text(eiin, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("শিক্ষা বোর্ড", color = TextGray, fontSize = 10.sp)
                        Text(board, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("শিক্ষক সংখ্যা", color = TextGray, fontSize = 10.sp)
                        Text(teachers, color = Color.White, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("শিক্ষার্থী সংখ্যা", color = TextGray, fontSize = 10.sp)
                        Text(students, color = Color.White, fontSize = 12.sp)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("প্রধান / অধ্যক্ষ", color = TextGray, fontSize = 10.sp)
                    Text(principal, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("অবস্থান ও ম্যাপ লিঙ্ক", color = TextGray, fontSize = 10.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(12.dp))
                        Text(school.location, color = Color.White, fontSize = 12.sp)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("বর্ণনা ও অর্জনসমূহ", color = TextGray, fontSize = 10.sp)
                    Text(school.description, color = TextWhite, fontSize = 12.sp, lineHeight = 16.sp)
                }

                // Interactive Web Link
                Button(
                    onClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$web"))
                        context.startActivity(browserIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = DarkNavyBackground, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("অফিশিয়াল ওয়েবসাইট ভিজিট করুন", color = DarkNavyBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${school.contactPhone}"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("যোগাযোগের জন্য কল করুন", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun AddSchoolFormOverlay(
    onDismiss: () -> Unit,
    onSubmit: (DirectoryItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("School") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var board by remember { mutableStateOf("Barishal") }
    var eiin by remember { mutableStateOf("") }
    var principal by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var catExpanded by remember { mutableStateOf(false) }
    var boardExpanded by remember { mutableStateOf(false) }

    val categories = listOf("School", "College", "University", "Madrasa", "Technical")
    val boards = listOf("Barishal", "Dhaka", "Madrasah Board", "Technical Board")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && address.isNotEmpty() && phone.isNotEmpty()) {
                        val extraJson = "eiin=$eiin;board=$board;principal=$principal;teachers=৪০ জন;students=১০০০ জন;web=$website"
                        val newId = "school_" + UUID.randomUUID().toString().take(6)
                        val item = DirectoryItem(
                            id = newId,
                            category = "school",
                            title = name,
                            subtitle = category,
                            description = description,
                            contactPhone = phone,
                            location = address,
                            priceOrFee = "সাধারণ",
                            statusOrSchedule = "ভর্তি চলছে",
                            status = "PENDING",
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
            Text("শিক্ষা প্রতিষ্ঠান যুক্ত করুন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                    label = { Text("প্রতিষ্ঠানের নাম *", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("add_school_name")
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { catExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("ধরন (Category): $category", color = Color.White)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextCyan)
                        }
                    }
                    DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }, modifier = Modifier.background(DarkNavySurface)) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat, color = Color.White) }, onClick = { category = cat; catExpanded = false })
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { boardExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("শিক্ষা বোর্ড: $board", color = Color.White)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextCyan)
                        }
                    }
                    DropdownMenu(expanded = boardExpanded, onDismissRequest = { boardExpanded = false }, modifier = Modifier.background(DarkNavySurface)) {
                        boards.forEach { brd ->
                            DropdownMenuItem(text = { Text(brd, color = Color.White) }, onClick = { board = brd; boardExpanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা *", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("add_school_address")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("ফোন নম্বর *", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = eiin,
                    onValueChange = { eiin = it },
                    label = { Text("EIIN কোড", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = principal,
                    onValueChange = { principal = it },
                    label = { Text("প্রধান / অধ্যক্ষের নাম", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("ওয়েবসাইট লিঙ্ক (যেমন: school.com)", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("প্রতিষ্ঠানের বর্ণনা ও অর্জনসমূহ", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
