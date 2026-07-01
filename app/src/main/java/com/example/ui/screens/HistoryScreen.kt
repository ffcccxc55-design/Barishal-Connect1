package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.GlowButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isAdmin by viewModel.isAdminLoggedIn.collectAsState()
    val scrollState = rememberScrollState()

    // Geographic options for Barishal Connect
    val districts = listOf("বরিশাল (Barishal)", "পটুয়াখালী (Patuakhali)", "ভোলা (Bhola)", "পিরোজপুর (Pirojpur)", "ঝালকাঠি (Jhalokathi)", "বরগুনা (Barguna)")
    val upazilas = mapOf(
        "বরিশাল (Barishal)" to listOf("সদর (Sadar)", "বাকেরগঞ্জ (Bakerganj)", "বাবুগঞ্জ (Babuganj)", "উজিরপুর (Uzirpur)", "গৌরনদী (Gournadi)"),
        "পটুয়াখালী (Patuakhali)" to listOf("সদর (Sadar)", "কলাপাড়া (Kalapara)", "গলাচিপা (Galachipa)", "বাউফল (Bauphal)"),
        "ভোলা (Bhola)" to listOf("সদর (Sadar)", "চরফ্যাশন (Char Fasson)", "লালমোহন (Lalmohan)", "বোরহানউদ্দিন (Borhanuddin)")
    )
    val unions = mapOf(
        "সদর (Sadar)" to listOf("চাঁদপুরা (Chandpura)", "চরকরমজী (Char Karomji)", "জাগুয়া (Jagua)", "কাশি Settlements")
    )

    // Current selection levels
    var selectedDistrict by remember { mutableStateOf("বরিশাল (Barishal)") }
    var selectedUpazila by remember { mutableStateOf("সদর (Sadar)") }
    var selectedUnion by remember { mutableStateOf("চাঁদপুরা (Chandpura)") }

    // Dynamic History Database stored as Local States (representing real dynamic admin data)
    var customHistoryRecords by remember { mutableStateOf(mutableMapOf<String, HistoryRecord>()) }
    var showAddRegionDialog by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }

    // Helper to get active location key
    val locationKey = "$selectedDistrict|$selectedUpazila|$selectedUnion"

    // Default pre-populated record if empty
    val defaultRecord = remember(locationKey) {
        HistoryRecord(
            historyText = "বরিশাল বা বাকলা-চন্দ্রদ্বীপ সুপ্রাচীন ঐতিহাসিক নদীবিধৌত অঞ্চল। এটি মগ দস্যুদের হাত থেকে বাঁচাতে এককালে দুর্গ হিসেবে ব্যবহার হতো। কীর্তনখোলার কোল ঘেঁষে গড়ে ওঠা এই অঞ্চল সংস্কৃতি, ঐতিহ্য ও সাহিত্যের সুতিকাগার।",
            freedomWarText = "১৯৭১ সালের মুক্তিযুদ্ধে বরিশাল ৯ নং সেক্টরের অধীন ছিল। ক্যাপ্টেন বেগ এবং বীর মুক্তিযোদ্ধাদের নেতৃত্বে বরিশাল ও পটুয়াখালীর উপকূলবর্তী নদী অঞ্চলে পাকিস্তানি হানাদার বাহিনীদের বিরুদ্ধে তীব্র প্রতিরোধ গড়ে তোলা হয়। ৯ই ডিসেম্বর বরিশাল শহর পাকিস্তানি বাহিনী মুক্ত হয়।",
            personalitiesText = "১. শেরে বাংলা এ. কে. ফজলুল হক (বাংলার বাঘ)\n২. কবি জীবনানন্দ দাশ (রূপসী বাংলার কবি)\n৩. কবি সুফিয়া কামাল\n৪. কুসুমকুমারী দাশ (কবি জীবনানন্দ দাশের মা)",
            cultureText = "নদীমাতৃক কীর্তন এবং জারি-সারি গান, ভাসমান ঐতিহ্যবাহী হাট ও কাস্তে বালাম চালের চাষাবাদ এখানকার প্রাচীন ঐতিহ্য।",
            timelineText = "১৭৯৭: বাকলা-চন্দ্রদ্বীপকে বরিশাল জেলা হিসেবে ঘোষণা করা হয়।\n১৯৭১: স্বাধীনতা যুদ্ধে বীরত্বপূর্ণ লড়াই ও শত্রুমুক্তকরণ।\n১৯৯৩: বরিশাল স্বতন্ত্র বিভাগ হিসেবে মর্যাদা পায়।",
            booksText = "১. বাকলা-চন্দ্রদ্বীপের ইতিহাস - সতীশচন্দ্র মিত্র\n২. আমাদের মুক্তিযুদ্ধ - শাহজাহান মিয়া\n৩. রূপসী বাংলা - জীবনানন্দ দাশ"
        )
    }

    val activeRecord = customHistoryRecords[locationKey] ?: defaultRecord

    // Input States for Editing
    var editHistory by remember(activeRecord, isEditMode) { mutableStateOf(activeRecord.historyText) }
    var editFreedom by remember(activeRecord, isEditMode) { mutableStateOf(activeRecord.freedomWarText) }
    var editPersonalities by remember(activeRecord, isEditMode) { mutableStateOf(activeRecord.personalitiesText) }
    var editCulture by remember(activeRecord, isEditMode) { mutableStateOf(activeRecord.cultureText) }
    var editTimeline by remember(activeRecord, isEditMode) { mutableStateOf(activeRecord.timelineText) }
    var editBooks by remember(activeRecord, isEditMode) { mutableStateOf(activeRecord.booksText) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "ইতিহাস ও ঐতিহ্য ডিরেক্টরি", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("history_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = {
                            if (isEditMode) {
                                // Save edit
                                val updated = activeRecord.copy(
                                    historyText = editHistory,
                                    freedomWarText = editFreedom,
                                    personalitiesText = editPersonalities,
                                    cultureText = editCulture,
                                    timelineText = editTimeline,
                                    booksText = editBooks
                                )
                                val newMap = customHistoryRecords.toMutableMap()
                                newMap[locationKey] = updated
                                customHistoryRecords = newMap
                                isEditMode = false
                                Toast.makeText(context, "ঐতিহাসিক তথ্য সফলভাবে আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                            } else {
                                isEditMode = true
                            }
                        }, modifier = Modifier.testTag("history_edit_toggle")) {
                            Icon(
                                imageVector = if (isEditMode) Icons.Default.Save else Icons.Default.Edit,
                                contentDescription = if (isEditMode) "Save" else "Edit",
                                tint = NeonCyan
                            )
                        }
                        
                        IconButton(onClick = { showAddRegionDialog = true }) {
                            Icon(imageVector = Icons.Default.AddHomeWork, contentDescription = "Add Region", tint = NeonTeal)
                        }
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            SectionHeader(title = "ঐতিহ্যবাহী বাকলা-চন্দ্রদ্বীপ", subtitle = "বিভাগ, জেলা, উপজেলা এবং ইউনিয়ন ভিত্তিক সঠিক ইতিহাস জানুন")

            // Geographic Cascading Selectors
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "অঞ্চল নির্বাচন করুন (Filters)", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    // 1. District Selector Row
                    Column {
                        Text(text = "জেলা (District):", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            items(districts) { d ->
                                val sel = selectedDistrict == d
                                Surface(
                                    onClick = {
                                        selectedDistrict = d
                                        selectedUpazila = upazilas[d]?.firstOrNull() ?: "সদর (Sadar)"
                                        selectedUnion = unions[selectedUpazila]?.firstOrNull() ?: "চাঁদপুরা (Chandpura)"
                                    },
                                    color = if (sel) NeonCyan else DarkNavySurface,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, if (sel) NeonCyan else GlassBorder)
                                ) {
                                    Text(text = d.split(" ").first(), color = if (sel) DarkNavyBackground else Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 2. Upazila Selector Row
                    val activeUpazilas = upazilas[selectedDistrict] ?: listOf("সদর (Sadar)")
                    Column {
                        Text(text = "উপজেলা (Upazila):", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            items(activeUpazilas) { u ->
                                val sel = selectedUpazila == u
                                Surface(
                                    onClick = {
                                        selectedUpazila = u
                                        selectedUnion = unions[u]?.firstOrNull() ?: "চাঁদপুরা (Chandpura)"
                                    },
                                    color = if (sel) NeonTeal else DarkNavySurface,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, if (sel) NeonTeal else GlassBorder)
                                ) {
                                    Text(text = u, color = if (sel) DarkNavyBackground else Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 3. Union Selector Row
                    val activeUnions = unions[selectedUpazila] ?: listOf("চাঁদপুরা (Chandpura)", "চরকরমজী", "জাগুয়া")
                    Column {
                        Text(text = "ইউনিয়ন (Union):", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                            items(activeUnions) { un ->
                                val sel = selectedUnion == un
                                Surface(
                                    onClick = { selectedUnion = un },
                                    color = if (sel) ElectricBlue else DarkNavySurface,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, if (sel) ElectricBlue else GlassBorder)
                                ) {
                                    Text(text = un, color = if (sel) Color.White else Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Historical Display Sections or Edit Fields
            if (isEditMode) {
                // Editing Form
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(text = "ঐতিহাসিক ডাটা সম্পাদনা", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                        GlassTextField(value = editHistory, onValueChange = { editHistory = it }, label = "ইতিহাস বিবরণ", modifier = Modifier.fillMaxWidth())
                        GlassTextField(value = editFreedom, onValueChange = { editFreedom = it }, label = "মুক্তিযুদ্ধের ইতিহাস ও অবদান", modifier = Modifier.fillMaxWidth())
                        GlassTextField(value = editPersonalities, onValueChange = { editPersonalities = it }, label = "বিখ্যাত ব্যক্তিত্বসমূহ", modifier = Modifier.fillMaxWidth())
                        GlassTextField(value = editCulture, onValueChange = { editCulture = it }, label = "ঐতিহ্যবাহী সংস্কৃতি ও উৎসব", modifier = Modifier.fillMaxWidth())
                        GlassTextField(value = editTimeline, onValueChange = { editTimeline = it }, label = "ইতিহাসের উল্লেখযোগ্য কালরেখা", modifier = Modifier.fillMaxWidth())
                        GlassTextField(value = editBooks, onValueChange = { editBooks = it }, label = "প্রাসঙ্গিক বই ও তথ্যসূত্র", modifier = Modifier.fillMaxWidth())

                        GlowButton(
                            text = "সংরক্ষণ করুন",
                            onClick = {
                                val updated = activeRecord.copy(
                                    historyText = editHistory,
                                    freedomWarText = editFreedom,
                                    personalitiesText = editPersonalities,
                                    cultureText = editCulture,
                                    timelineText = editTimeline,
                                    booksText = editBooks
                                )
                                val newMap = customHistoryRecords.toMutableMap()
                                newMap[locationKey] = updated
                                customHistoryRecords = newMap
                                isEditMode = false
                                Toast.makeText(context, "ঐতিহাসিক তথ্য সফলভাবে সংরক্ষণ হয়েছে!", Toast.LENGTH_SHORT).show()
                            },
                            containerColor = NeonTeal,
                            textColor = DarkNavyBackground,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // Read Only Presentation Sections
                SectionHeader(title = "${selectedDistrict.split(" ").first()} - $selectedUpazila - $selectedUnion", subtitle = "এখানকার সুপ্রাচীন সমৃদ্ধ গৌরবময় অতীত")

                // 1. History
                HistoryContentCard(
                    title = "অঞ্চলের ইতিহাস ও নামকরণ",
                    text = activeRecord.historyText,
                    icon = Icons.Default.MenuBook,
                    color = NeonCyan
                )

                // 2. Freedom War
                HistoryContentCard(
                    title = "মুক্তিযুদ্ধ ও গৌরবময় অবদান",
                    text = activeRecord.freedomWarText,
                    icon = Icons.Default.Campaign,
                    color = RedEmergency
                )

                // 3. Famous Personalities
                HistoryContentCard(
                    title = "বিখ্যাত ব্যক্তিত্ব (Famous Personalities)",
                    text = activeRecord.personalitiesText,
                    icon = Icons.Default.Group,
                    color = NeonTeal
                )

                // 4. Traditional Culture
                HistoryContentCard(
                    title = "ঐতিহ্যবাহী সংস্কৃতি ও লোকাচার",
                    text = activeRecord.cultureText,
                    icon = Icons.Default.MusicNote,
                    color = ElectricBlue
                )

                // 5. Timeline
                HistoryContentCard(
                    title = "ঐতিহাসিক টাইমলাইন ও ঘটনাপ্রবাহ",
                    text = activeRecord.timelineText,
                    icon = Icons.Default.Schedule,
                    color = Color.White
                )

                // 6. Books & References
                HistoryContentCard(
                    title = "সহায়ক বই, দলিল ও তথ্যসূত্র",
                    text = activeRecord.booksText,
                    icon = Icons.Default.Bookmark,
                    color = NeonCyan
                )

                // Action buttons: Share history
                GlowButton(
                    text = "ঐতিহাসিক তথ্য শেয়ার করুন",
                    onClick = {
                        val shareText = "জানুন ${selectedDistrict.split(" ").first()} জেলার $selectedUpazila উপজেলার $selectedUnion ইউনিয়নের গৌরবময় ইতিহাস ও ঐতিহ্য!\n\n${activeRecord.historyText}\n\nশেয়ার্ড বাই বরিশাল কানেক্ট।"
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share History"))
                    },
                    containerColor = NeonCyan,
                    textColor = DarkNavyBackground,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Add Custom Region Dialog
    if (showAddRegionDialog) {
        var addDistrict by remember { mutableStateOf("") }
        var addUpazila by remember { mutableStateOf("") }
        var addUnion by remember { mutableStateOf("") }
        var addHistory by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddRegionDialog = false },
            title = { Text(text = "নতুন অঞ্চলের ইতিহাস যোগ করুন", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    GlassTextField(value = addDistrict, onValueChange = { addDistrict = it }, label = "জেলার নাম (যেমন: ঝালকাঠি)", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = addUpazila, onValueChange = { addUpazila = it }, label = "উপজেলার নাম", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = addUnion, onValueChange = { addUnion = it }, label = "ইউনিয়নের নাম", modifier = Modifier.fillMaxWidth())
                    GlassTextField(value = addHistory, onValueChange = { addHistory = it }, label = "ইতিহাসের মূল বিবরণী", modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (addDistrict.isEmpty() || addUpazila.isEmpty() || addUnion.isEmpty() || addHistory.isEmpty()) {
                            Toast.makeText(context, "অনুগ্রহ করে সব তথ্য সঠিক দিন!", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                        val key = "$addDistrict|$addUpazila|$addUnion"
                        val record = HistoryRecord(
                            historyText = addHistory,
                            freedomWarText = "তথ্য শীঘ্রই হালনাগাদ করা হবে।",
                            personalitiesText = "বিখ্যাত ব্যক্তিত্বের তথ্য অনুপস্থিত।",
                            cultureText = "সংস্কৃতির বিবরণ যুক্ত করুন।",
                            timelineText = "কালরেখা যুক্ত করুন।",
                            booksText = "তথ্যসূত্র যুক্ত করুন।"
                        )
                        val newMap = customHistoryRecords.toMutableMap()
                        newMap[key] = record
                        customHistoryRecords = newMap
                        showAddRegionDialog = false
                        Toast.makeText(context, "নতুন আঞ্চলিক ইতিহাস সফলভাবে প্রকাশিত হয়েছে!", Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text(text = "প্রকাশ করুন", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRegionDialog = false }) {
                    Text(text = "বাতিল", color = TextGray)
                }
            },
            containerColor = DarkNavySurface
        )
    }
}

@Composable
fun HistoryContentCard(
    title: String,
    text: String,
    icon: ImageVector,
    color: Color
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
                Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = text,
                color = TextWhite.copy(alpha = 0.85f),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

data class HistoryRecord(
    val historyText: String,
    val freedomWarText: String,
    val personalitiesText: String,
    val cultureText: String,
    val timelineText: String,
    val booksText: String
)
