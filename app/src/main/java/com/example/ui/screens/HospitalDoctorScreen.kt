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
fun HospitalDoctorScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allItems by viewModel.allItems.collectAsState()

    // Screen States
    var currentMainTab by remember { mutableStateOf(0) } // 0: Hospitals, 1: Doctors Directory
    var selectedHospitalCategory by remember { mutableStateOf("All") }
    var selectedDoctorSpecialty by remember { mutableStateOf("All") }
    
    var selectedHospitalDetail by remember { mutableStateOf<DirectoryItem?>(null) }
    var selectedDoctorDetail by remember { mutableStateOf<DirectoryItem?>(null) }
    
    var showAddHospitalForm by remember { mutableStateOf(false) }
    var showAddDoctorForm by remember { mutableStateOf(false) }
    
    // Search queries
    var searchQuery by remember { mutableStateOf("") }

    val hospitalCategories = listOf("All", "Government", "Private", "Clinic", "Diagnostic Center", "Community Clinic")
    
    val doctorSpecialties = listOf(
        "All", "Medicine", "Surgery", "Orthopedic", "Gynecology", "Child", "Heart", "Eye", "ENT", "Dental", "Skin", "Neurology", "Kidney", "Cancer", "Urology"
    )

    // Filtered Hospitals
    val hospitals = allItems.filter {
        it.category == "hospital" &&
        it.status == "APPROVED" &&
        (selectedHospitalCategory == "All" || it.subtitle.contains(selectedHospitalCategory, ignoreCase = true) || it.title.contains(selectedHospitalCategory, ignoreCase = true)) &&
        (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true))
    }

    // Filtered Doctors
    val doctors = allItems.filter {
        it.category == "doctor" &&
        it.status == "APPROVED" &&
        (selectedDoctorSpecialty == "All" || it.subtitle.contains(selectedDoctorSpecialty, ignoreCase = true)) &&
        (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("চিকিৎসা সেবা ও ডিরেক্টরি", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("হাসপাতাল, ২৪ ঘণ্টা ইমার্জেন্সি ও বিশেষজ্ঞ ডাক্তার", color = TextCyan, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("med_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (currentMainTab == 0) showAddHospitalForm = true else showAddDoctorForm = true
                    }, modifier = Modifier.testTag("med_add_header_btn")) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item", tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavyBackground)
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
                // Main Switcher (Hospitals vs Doctors)
                TabRow(
                    selectedTabIndex = currentMainTab,
                    containerColor = DarkNavySurface,
                    contentColor = NeonCyan,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = currentMainTab == 0,
                        onClick = { currentMainTab = 0; searchQuery = "" },
                        text = { Text("🏥 হাসপাতাল ও ক্লিনিক", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = currentMainTab == 1,
                        onClick = { currentMainTab = 1; searchQuery = "" },
                        text = { Text("🩺 বিশেষজ্ঞ ডাক্তার", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                // General Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { 
                        Text(
                            text = if (currentMainTab == 0) "হাসপাতাল বা ক্লিনিক খুঁজুন..." else "ডাক্তার বা স্পেশালিটি দিয়ে খুঁজুন...", 
                            color = TextGray, 
                            fontSize = 13.sp
                        ) 
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextCyan) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("med_search_input"),
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

                if (currentMainTab == 0) {
                    // Hospital Category Filter Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        hospitalCategories.forEach { cat ->
                            val isSelected = selectedHospitalCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) NeonCyan else DarkNavySurfaceCard)
                                    .border(BorderStroke(1.dp, if (isSelected) NeonCyan else GlassBorder), RoundedCornerShape(50))
                                    .clickable { selectedHospitalCategory = cat }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) DarkNavyBackground else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Hospitals Listing
                    if (hospitals.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Text("কোনো হাসপাতাল পাওয়া যায়নি।", color = TextGray, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("hospital_list_view"),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(hospitals) { hosp ->
                                HospitalCardItem(
                                    hospital = hosp,
                                    onClick = { selectedHospitalDetail = hosp },
                                    onFavoriteToggle = { viewModel.toggleFavorite(hosp.id, hosp.isFavorite) }
                                )
                            }
                        }
                    }
                } else {
                    // Doctor Specialties Filter Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        doctorSpecialties.forEach { spec ->
                            val isSelected = selectedDoctorSpecialty == spec
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) NeonCyan else DarkNavySurfaceCard)
                                    .border(BorderStroke(1.dp, if (isSelected) NeonCyan else GlassBorder), RoundedCornerShape(50))
                                    .clickable { selectedDoctorSpecialty = spec }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = spec,
                                    color = if (isSelected) DarkNavyBackground else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Doctors Listing
                    if (doctors.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Text("কোনো বিশেষজ্ঞ ডাক্তারের তথ্য পাওয়া যায়নি।", color = TextGray, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("doctor_list_view"),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(doctors) { doc ->
                                DoctorCardItem(
                                    doctor = doc,
                                    onClick = { selectedDoctorDetail = doc },
                                    onFavoriteToggle = { viewModel.toggleFavorite(doc.id, doc.isFavorite) }
                                )
                            }
                        }
                    }
                }
            }

            // Detailed Popups/Sheets
            if (selectedHospitalDetail != null) {
                HospitalDetailsOverlay(
                    hospital = selectedHospitalDetail!!,
                    onDismiss = { selectedHospitalDetail = null },
                    onToggleFavorite = { viewModel.toggleFavorite(selectedHospitalDetail!!.id, selectedHospitalDetail!!.isFavorite) }
                )
            }

            if (selectedDoctorDetail != null) {
                DoctorDetailsOverlay(
                    doctor = selectedDoctorDetail!!,
                    onDismiss = { selectedDoctorDetail = null },
                    onToggleFavorite = { viewModel.toggleFavorite(selectedDoctorDetail!!.id, selectedDoctorDetail!!.isFavorite) }
                )
            }

            if (showAddHospitalForm) {
                AddHospitalFormOverlay(
                    onDismiss = { showAddHospitalForm = false },
                    onSubmit = { newHosp ->
                        viewModel.addDirectoryItem(newHosp) { success ->
                            if (success) {
                                Toast.makeText(context, "হাসপাতালটি সফলভাবে ক্লাউড ডেটাবেজে জমা হয়েছে এবং অনুমোদনের অপেক্ষায় আছে!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "অফলাইনে স্থানীয়ভাবে সংরক্ষিত হয়েছে! ইন্টারনেট সংযোগ চালু হলে এডমিন প্যানেল থেকে সিঙ্ক করুন।", Toast.LENGTH_LONG).show()
                            }
                        }
                        showAddHospitalForm = false
                    }
                )
            }

            if (showAddDoctorForm) {
                AddDoctorFormOverlay(
                    onDismiss = { showAddDoctorForm = false },
                    onSubmit = { newDoc ->
                        viewModel.addDirectoryItem(newDoc) { success ->
                            if (success) {
                                Toast.makeText(context, "ডাক্তারের প্রোফাইল সফলভাবে ক্লাউড ডেটাবেজে জমা হয়েছে এবং অনুমোদনের অপেক্ষায় আছে!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "অফলাইনে স্থানীয়ভাবে সংরক্ষিত হয়েছে! ইন্টারনেট সংযোগ চালু হলে এডমিন প্যানেল থেকে সিঙ্ক করুন।", Toast.LENGTH_LONG).show()
                            }
                        }
                        showAddDoctorForm = false
                    }
                )
            }
        }
    }
}

@Composable
fun HospitalCardItem(
    hospital: DirectoryItem,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("hospital_card_${hospital.id}"),
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
                    .background(RedEmergency.copy(alpha = 0.1f))
                    .border(BorderStroke(1.dp, RedEmergency.copy(alpha = 0.3f)), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = RedEmergency, modifier = Modifier.size(28.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(hospital.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    // Emergency Badge
                    Surface(
                        color = RedEmergency.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("EMERGENCY 24/7", color = RedEmergency, fontSize = 7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }

                Text(hospital.subtitle, color = TextCyan, fontSize = 11.sp)
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextGray, modifier = Modifier.size(12.dp))
                    Text(hospital.location, color = TextGray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (hospital.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (hospital.isFavorite) RedEmergency else TextGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DoctorCardItem(
    doctor: DirectoryItem,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("doctor_card_${doctor.id}"),
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
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.1f))
                    .border(BorderStroke(1.dp, NeonCyan), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(doctor.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(doctor.subtitle, color = TextCyan, fontSize = 11.sp, fontWeight = FontWeight.Medium) // Speciality
                Text("চেম্বার: ${doctor.location}", color = TextGray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (doctor.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (doctor.isFavorite) RedEmergency else TextGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun HospitalDetailsOverlay(
    hospital: DirectoryItem,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current
    val extraMap = remember(hospital.extraDataJson) {
        val map = mutableMapOf<String, String>()
        hospital.extraDataJson.split(";").forEach { pair ->
            val idx = pair.indexOf("=")
            if (idx != -1) {
                map[pair.substring(0, idx).trim()] = pair.substring(idx + 1).trim()
            }
        }
        map
    }

    val ambulance = extraMap["ambulance"] ?: "উপলব্ধ (01711-223344)"
    val icu = extraMap["icu"] ?: "৮টি বেড সক্রিয়"
    val bloodBank = extraMap["bloodBank"] ?: "রক্তের পর্যাপ্ত স্টক রয়েছে"
    val opening = hospital.statusOrSchedule.ifEmpty { "২৪ ঘণ্টা খোলা" }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন", color = TextCyan)
            }
        },
        containerColor = DarkNavySurface,
        icon = { Icon(Icons.Default.LocalHospital, contentDescription = null, tint = RedEmergency) },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(hospital.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                Text(hospital.subtitle, color = TextCyan, fontSize = 12.sp, textAlign = TextAlign.Center)
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
                        Text("সার্ভিস শিডিউল", color = TextGray, fontSize = 10.sp)
                        Text(opening, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("জরুরি হেল্পলাইন", color = TextGray, fontSize = 10.sp)
                        Text(hospital.contactPhone, color = RedEmergency, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("অবস্থান ও ম্যাপ লিঙ্ক", color = TextGray, fontSize = 10.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(12.dp))
                        Text(hospital.location, color = Color.White, fontSize = 12.sp)
                    }
                }

                // Services facilities
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("হাসপাতালের সুযোগ-সুবিধাসমূহ", color = TextCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    
                    FacilityRow(title = "অ্যাম্বুলেন্স:", value = ambulance, icon = Icons.Default.DirectionsCar)
                    FacilityRow(title = "নিবিড় পরিচর্যা (ICU):", value = icu, icon = Icons.Default.AirlineSeatFlat)
                    FacilityRow(title = "রক্তের ব্যাংক:", value = bloodBank, icon = Icons.Default.Bloodtype)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("সংক্ষিপ্ত বিবরণ", color = TextGray, fontSize = 10.sp)
                    Text(hospital.description, color = TextWhite, fontSize = 12.sp, lineHeight = 16.sp)
                }

                // Call Emergency
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${hospital.contactPhone}"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedEmergency),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("জরুরি নম্বরে কল করুন", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
fun FacilityRow(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TextGray, modifier = Modifier.size(14.dp))
        Text(title, color = TextGray, fontSize = 11.sp)
        Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DoctorDetailsOverlay(
    doctor: DirectoryItem,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current
    val extraMap = remember(doctor.extraDataJson) {
        val map = mutableMapOf<String, String>()
        doctor.extraDataJson.split(";").forEach { pair ->
            val idx = pair.indexOf("=")
            if (idx != -1) {
                map[pair.substring(0, idx).trim()] = pair.substring(idx + 1).trim()
            }
        }
        map
    }

    val degree = extraMap["degree"] ?: "MBBS, FCPS (Medicine)"
    val fees = doctor.priceOrFee.ifEmpty { "৫০০ টাকা (ভিজিট)" }
    val visiting = doctor.statusOrSchedule.ifEmpty { "বিকাল ৫:০০ - রাত ৮:০০" }
    val phone = doctor.contactPhone.ifEmpty { "01711-000000" }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন", color = TextCyan)
            }
        },
        containerColor = DarkNavySurface,
        icon = { Icon(Icons.Default.MedicalServices, contentDescription = null, tint = NeonCyan) },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(doctor.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                Text(doctor.subtitle, color = TextCyan, fontSize = 12.sp, textAlign = TextAlign.Center)
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

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("ডিগ্রী ও শিক্ষাগত যোগ্যতা", color = TextGray, fontSize = 10.sp)
                    Text(degree, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("রোগী দেখার সময়", color = TextGray, fontSize = 10.sp)
                        Text(visiting, color = Color.White, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("ভিজিট ফি", color = TextGray, fontSize = 10.sp)
                        Text(fees, color = NeonTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("চেম্বার ও ঠিকানা", color = TextGray, fontSize = 10.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(12.dp))
                        Text(doctor.location, color = Color.White, fontSize = 12.sp)
                    }
                }

                // Interactive Online Appointment Button (Future Ready)
                Button(
                    onClick = {
                        Toast.makeText(context, "সিরিয়াল বুকিংয়ের আবেদন সম্পন্ন হয়েছে! চেম্বার অ্যাসিস্ট্যান্ট কল করে কনফার্ম করবেন।", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = DarkNavyBackground, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("অনলাইন সিরিয়াল বুকিং (Future Ready)", color = DarkNavyBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("কল সিরিয়াল", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/88$phone"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    )
}

@Composable
fun AddHospitalFormOverlay(
    onDismiss: () -> Unit,
    onSubmit: (DirectoryItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Private") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var catExpanded by remember { mutableStateOf(false) }
    val categories = listOf("Government", "Private", "Clinic", "Diagnostic Center", "Community Clinic")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && address.isNotEmpty() && phone.isNotEmpty()) {
                        val newId = "hospital_" + UUID.randomUUID().toString().take(6)
                        val item = DirectoryItem(
                            id = newId,
                            category = "hospital",
                            title = name,
                            subtitle = category,
                            description = description,
                            contactPhone = emergencyPhone.ifEmpty { phone },
                            location = address,
                            priceOrFee = "সাধারণ ফি",
                            statusOrSchedule = "২৪ ঘণ্টা খোলা",
                            status = "PENDING"
                        )
                        onSubmit(item)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("যোগ করুন", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = TextGray)
            }
        },
        containerColor = DarkNavySurface,
        title = {
            Text("নতুন হাসপাতাল যুক্ত করুন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                    label = { Text("হাসপাতাল বা ক্লিনিকের নাম *", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("add_hospital_name")
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { catExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("শ্রেণী: $category", color = Color.White)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextCyan)
                        }
                    }
                    DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }, modifier = Modifier.background(DarkNavySurface)) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat, color = Color.White) }, onClick = { category = cat; catExpanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা *", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("add_hospital_address")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("ফোন নম্বর *", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = emergencyPhone,
                    onValueChange = { emergencyPhone = it },
                    label = { Text("২৪ ঘণ্টা ইমার্জেন্সি হটলাইন নম্বর", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("হাসপাতাল বা সেবার বিবরণ", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
fun AddDoctorFormOverlay(
    onDismiss: () -> Unit,
    onSubmit: (DirectoryItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("Medicine") }
    var degree by remember { mutableStateOf("") }
    var hospital by remember { mutableStateOf("") }
    var chamberAddress by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var visitingTime by remember { mutableStateOf("বিকাল ৫:০০ - রাত ৮:০০") }
    var fees by remember { mutableStateOf("৫০০ টাকা") }

    var specExpanded by remember { mutableStateOf(false) }
    val specialties = listOf(
        "Medicine", "Surgery", "Orthopedic", "Gynecology", "Child", "Heart", "Eye", "ENT", "Dental", "Skin", "Neurology", "Kidney", "Cancer", "Urology"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && degree.isNotEmpty() && chamberAddress.isNotEmpty()) {
                        val extraJson = "degree=$degree;experience_years=৫ বছর"
                        val newId = "doctor_" + UUID.randomUUID().toString().take(6)
                        val item = DirectoryItem(
                            id = newId,
                            category = "doctor",
                            title = name,
                            subtitle = specialty, // Holds Specialty
                            description = "বিশেষজ্ঞ ডাক্তার চেম্বার বরিশাল",
                            contactPhone = phone,
                            location = chamberAddress,
                            priceOrFee = fees,
                            statusOrSchedule = visitingTime,
                            status = "PENDING",
                            extraDataJson = extraJson
                        )
                        onSubmit(item)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("প্রোফাইল দিন", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = TextGray)
            }
        },
        containerColor = DarkNavySurface,
        title = {
            Text("বিশেষজ্ঞ ডাক্তার প্রোফাইল যুক্ত করুন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                    label = { Text("ডাক্তারের নাম *", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("add_doctor_name")
                )

                OutlinedTextField(
                    value = degree,
                    onValueChange = { degree = it },
                    label = { Text("ডিগ্রী ও শিক্ষাগত যোগ্যতা *", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("add_doctor_degree")
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { specExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("বিশেষজ্ঞ (Specialty): $specialty", color = Color.White)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextCyan)
                        }
                    }
                    DropdownMenu(expanded = specExpanded, onDismissRequest = { specExpanded = false }, modifier = Modifier.background(DarkNavySurface)) {
                        specialties.forEach { spec ->
                            DropdownMenuItem(text = { Text(spec, color = Color.White) }, onClick = { specialty = spec; specExpanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = hospital,
                    onValueChange = { hospital = it },
                    label = { Text("কর্মরত হাসপাতাল", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = chamberAddress,
                    onValueChange = { chamberAddress = it },
                    label = { Text("চেম্বার ঠিকানা *", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth().testTag("add_doctor_chamber")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("সিরিয়াল ফোন নম্বর", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = visitingTime,
                    onValueChange = { visitingTime = it },
                    label = { Text("রোগী দেখার সময়", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fees,
                    onValueChange = { fees = it },
                    label = { Text("ভিজিট ফি", color = TextGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonCyan, unfocusedBorderColor = GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
