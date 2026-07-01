package com.example.ui.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowButton
import com.example.ui.components.GlassTextField
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgricultureScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: ফসল গাইড (Crops), 1: ঋতুভিত্তিক চাষ (Monsoon/Summer/Winter), 2: সার ও ওষুধ (Pest & Fertilizer)
    var selectedCrop by remember { mutableStateOf<CropInfo?>(null) }
    var selectedSeasonFilter by remember { mutableStateOf("সব ঋতু") }

    // List of standard crop info data matching requirements
    val cropsList = listOf(
        CropInfo(
            id = "crop_1",
            name = "বিআরআরআই ধান-২৮ (BRRI Dhan-28)",
            category = "Rice",
            season = "বোরো / শীতকাল (Boro / Winter)",
            suitableSoil = "দোআঁশ ও কাদা মাটি (Clayey Loam)",
            growingTime = "১৪০ - ১৪৫ দিন",
            harvestTime = "এপ্রিল - মে মাস",
            description = "বোরো মৌসুমের অত্যন্ত জনপ্রিয় ও রোগ প্রতিরোধ ক্ষমতা সম্পন্ন আধুনিক ধানের জাত।",
            cultivationMethod = "ধানের বীজ তলায় বীজ বপন করার পর ৩০-৩৫ দিন বয়সের চারা জমিতে রোপণ করতে হবে। সারি থেকে সারির দূরত্ব হবে ২০ সেমি।",
            landPrep = "৪-৫ টি আড়াআড়ি চাষ ও মই দিয়ে জমি কাদা ও মই দিয়ে সমান করতে হবে। শেষ চাষে প্রয়োজনীয় টিএসপি ও পটাশ সার মিশিয়ে দিতে হবে।",
            fertilizer = "ইউরিয়া: ৮ কেজি, টিএসপি: ৫ কেজি, এমওপি: ৪ কেজি (প্রতি বিঘা জমিতে তিন কিস্তিতে প্রয়োগ করতে হবে)।",
            waterRequirement = "জমিতে সর্বদা ২-৩ সেমি পানি ধরে রাখতে হবে। দানাদার ধান পাকার ১০ দিন আগে পানি নিষ্কাশন করতে হবে।",
            disease = "ব্লাস্ট রোগ এবং ব্যাকটেরিয়াজনিত পাতা পোড়া রোগ (Blast & Leaf Blight)",
            solution = "ব্লাস্টের জন্য ট্রাইসাইক্লাজোল জাতীয় ট্রুপার বা নাটিভো স্প্রে করতে হবে। পাতাপোড়া রোগের জন্য পটাশ সার ব্যবহার করুন।",
            estimatedCost = "১২,০০০ - ১৪,০০০ টাকা / বিঘা",
            expectedProfit = "২৫,০০০ - ২৮,০০০ টাকা / বিঘা",
            nearbyMarket = "নতুন বাজার ও চৌমাথা মোকাম, বরিশাল",
            govAdvice = "ইউরিয়া সার অতিরিক্ত ব্যবহারে ব্লাস্ট রোগ বেড়ে যায়। তাই সুষম সার প্রয়োগে সরকারি নির্দেশিকা অনুসরণ করুন।",
            videoUrl = "https://youtube.com/agriculture_dhan",
            imageUrl = "🌾"
        ),
        CropInfo(
            id = "crop_2",
            name = "তাজা পাট (Jute - Tosha)",
            category = "Jute",
            season = "খরিপ-১ / গ্রীষ্মকাল (Kharif / Summer)",
            suitableSoil = "উর্বর বেলে দোআঁশ মাটি (Silty Loam)",
            growingTime = "১২০ - ১৩০ দিন",
            harvestTime = "জুলাই - আগস্ট মাস",
            description = "বাংলাদেশের সোনালী আঁশ। বরিশাল এবং ফরিদপুর অঞ্চলের অন্যতম লাভজনক অর্থকরী ফসল।",
            cultivationMethod = "বীজ সারিতে বা ছিটিয়ে বপন করা যায়। সারিতে বপন করলে সারি থেকে সারির দূরত্ব ৩০ সেমি এবং গাছ থেকে গাছের দূরত্ব ১০ সেমি রাখতে হবে।",
            landPrep = "গভীর চাষ করে মাটি ঝুরঝুরে করতে হবে। শেষ চাষের সময় মাটির সাথে গোবর সার মেশালে ভালো ফলন পাওয়া যায়।",
            fertilizer = "ইউরিয়া: ৭ কেজি, টিএসপি: ৩ কেজি, এমওপি: ৩.৫ কেজি প্রতি বিঘায়।",
            waterRequirement = "পাট গাছের প্রাথমিক অবস্থায় খরা সহ্য ক্ষমতা কম। জমিতে পরিমিত রস থাকতে হবে, তবে জলাবদ্ধতা রাখা যাবে না।",
            disease = "কান্ড পচা রোগ এবং পাটের বিছা পোকা (Stem Rot & Jute Hairy Caterpillar)",
            solution = "কান্ডপচা দমনে ম্যানকোজেব জাতীয় ছত্রাকনাশক এবং বিছা পোকা দমনে রিকর্ড বা ডাইমেক্রন কীটনাশক প্রয়োগ করতে হবে।",
            estimatedCost = "৮,০০০ - ১০,০০০ টাকা / বিঘা",
            expectedProfit = "১৮,০০০ - ২২,০০০ টাকা / বিঘা",
            nearbyMarket = "পোর্ট রোড পাট আড়ত, বরিশাল",
            govAdvice = "পাট পচানোর জন্য রিবন রেটিং বা উন্নত বায়ো-পদ্ধতি ব্যবহার করলে পাটের আঁশের মান ও মূল্য বহুগুণ বৃদ্ধি পায়।",
            videoUrl = "https://youtube.com/jute_farming",
            imageUrl = "🌿"
        ),
        CropInfo(
            id = "crop_3",
            name = "বরিশালের মিঠা তরমুজ (Watermelon - Black Baby)",
            category = "Watermelon",
            season = "বসন্ত ও গ্রীষ্মকাল (Spring / Summer)",
            suitableSoil = "চর অঞ্চলের পলিযুক্ত বেলে মাটি (Sandy Loam)",
            growingTime = "৮৫ - ৯০ দিন",
            harvestTime = "মার্চ - এপ্রিল মাস",
            description = "চর অঞ্চলের বিশেষ সোনার ফসল। তরমুজ চাষে বরিশাল ও পটুয়াখালীর চর এলাকায় লাভজনক ফলন হয়।",
            cultivationMethod = "মাদা পদ্ধতিতে চাষ করা ভালো। প্রতি মাদায় ৩-৪ টি বীজ ২ সেমি গভীরে রোপণ করতে হবে। চারা গজালে ১-২ টি সবল চারা রেখে বাকিগুলো কেটে দিন।",
            landPrep = "গভীর চাষ দিয়ে মাদা তৈরি করে প্রতি মাদায় ৫ কেজি পচা গোবর ও সুষম সার মিশিয়ে অন্তত ১০ দিন ফেলে রাখতে হবে।",
            fertilizer = "জৈব সার: ৪ কেজি, খৈল: ২৫০ গ্রাম, টিএসপি: ২০০ গ্রাম, এমওপি: ১৫০ গ্রাম প্রতি মাদায়।",
            waterRequirement = "লতা ও ফল বৃদ্ধির সময়ে নিয়মিত সেচ প্রয়োজন। ফল পাকার ২০ দিন আগে সেচ দেওয়া সম্পূর্ণ বন্ধ করতে হবে।",
            disease = "এনথ্রাকনোজ ও ডাউনি মিলডিউ এবং মাছি পোকা (Anthracnose & Fruit Fly)",
            solution = "ডাউনি মিলডিউ দমনে রিডোমিল গোল্ড স্প্রে করুন। ফল মাছি দমনে সেক্স ফেরোমন ফাঁদ ব্যবহার করুন।",
            estimatedCost = "১৫,০০০ - ১৮,০০০ টাকা / বিঘা",
            expectedProfit = "৩৫,০০০ - ৪২,০০০ টাকা / বিঘা",
            nearbyMarket = "রুপাতলী ফল বাজার ও পাইকারি মোকাম",
            govAdvice = "তরমুজের আকার বড় করার জন্য অতিরিক্ত হরমোন স্প্রে করবেন না, এটি ফলটির স্বাদ নষ্ট করে ও স্বাস্থ্যের জন্য ক্ষতিকর।",
            videoUrl = "https://youtube.com/watermelon_barishal",
            imageUrl = "🍉"
        ),
        CropInfo(
            id = "crop_4",
            name = "মিষ্টি সুপারি (Betel Nut)",
            category = "Betel Leaf & Nut",
            season = "বারোমাসি (All Season)",
            suitableSoil = "উঁচু ও মাঝারি উর্বর দোআঁশ মাটি",
            growingTime = "৫ - ৬ বছর (ফলন শুরু)",
            harvestTime = "অক্টোবর - জানুয়ারি মাস",
            description = "বরিশাল বিভাগের অন্যতম ঐতিহ্যবাহী প্রধান অর্থকরী ক্যাশ ক্রপ। সুপারি বাগানে সাথী ফসল হিসেবে গোলপাতা বা পান চাষ করা যায়।",
            cultivationMethod = "৬-৯ মাস বয়সের চারা ২.৫ মিটার দূরত্বে গর্ত তৈরি করে রোপণ করতে হবে। গর্তে পর্যাপ্ত জৈব সার ও খৈল প্রয়োগ করতে হবে।",
            landPrep = "বাগান এলাকা আগাছামুক্ত রাখতে হবে। পানি নিষ্কাশনের জন্য নালা তৈরি করা আবশ্যক।",
            fertilizer = "প্রতিটি ফলন্ত গাছে বছরে গোবর: ১০ কেজি, ইউরিয়া: ৩৫০ গ্রাম, টিএসপি: ২৫০ গ্রাম, এমওপি: ৪০০ গ্রাম দিতে হবে।",
            waterRequirement = "খরা মৌসুমে ১০-১৫ দিন পর পর সেচ দেওয়া প্রয়োজন। বর্ষায় জমে থাকা পানি দ্রুত বের করে দিতে হবে।",
            disease = "কুঁড়ি পচা রোগ এবং সুপারি ফ্যাকাশে হওয়া রোগ (Bud Rot)",
            solution = "কুঁড়িপচা রোগ দেখা দিলে বোর্দো মিক্সচার বা ডাইথেন এম-৪৫ গাছের ডগায় স্প্রে করতে হবে।",
            estimatedCost = "৪,০০০ - ৫,০০০ টাকা (বার্ষিক রক্ষণাবেক্ষণ / বিঘা)",
            expectedProfit = "৩০,০০০ - ৩৫,০০০ টাকা / বিঘা (বার্ষিক আয়)",
            nearbyMarket = "উজিরপুর ও স্বরূপকাঠি সুপারি হাট",
            govAdvice = "সুপারি বাগানের চারিদিকে নারিকেল ও পান গাছের মিশ্র চাষ করলে মাটির পুষ্টিগুণ ভালো থাকে ও অতিরিক্ত আয় নিশ্চিত হয়।",
            videoUrl = "https://youtube.com/betel_nut",
            imageUrl = "🥥"
        ),
        CropInfo(
            id = "crop_5",
            name = "মিষ্টি পেয়ারা (Guava - Swarupkathi)",
            category = "Guava",
            season = "বর্ষাকাল (Monsoon)",
            suitableSoil = "উর্বর বেলে দোআঁশ বা পলি দোআঁশ",
            growingTime = "১ - ২ বছর (ফলন শুরু)",
            harvestTime = "জুন - আগস্ট মাস",
            description = "স্বরূপকাঠির ঐতিহ্যবাহী বিশ্ববিখ্যাত পেয়ারা। খালের ওপর ভাসমান হাটে এই পেয়ারা কেনাবেচা হয়।",
            cultivationMethod = "কলমের চারা রোপণ করলে দ্রুত ফলন পাওয়া যায়। সারি পদ্ধতিতে ৩ মিটার পর পর গর্ত খুঁড়ে চারা রোপণ করতে হবে।",
            landPrep = "জমি চাষ দিয়ে সমান করতে হবে। পেয়ারা গাছের গোড়ায় যেন কোনোভাবেই পানি না জমে সেই ব্যবস্থা করতে হবে।",
            fertilizer = "ইউরিয়া: ৩০০ গ্রাম, টিএসপি: ২৫০ গ্রাম, পটাশ: ৩০০ গ্রাম বার্ষিক প্রতি গাছে দুই কিস্তিতে।",
            waterRequirement = "গ্রীষ্মকালে খরায় গাছে পর্যাপ্ত সেচ দিতে হবে। ফল কাটার সময়ে অতিরিক্ত আর্দ্রতা ফল ফাটার কারণ হতে পারে।",
            disease = "এনথ্রাকনোজ ও ছত্রাকজনিত ফল পচা রোগ (Fruit Rot)",
            solution = "ম্যানকোজেব বা টিল্ট ২৫০ ইসি ছত্রাকনাশক ১০ দিন পর পর ২-৩ বার স্প্রে করতে হবে।",
            estimatedCost = "৭,০০০ - ৯,০০০ টাকা / বিঘা",
            expectedProfit = "২০,০০০ - ২৫,০০০ টাকা / বিঘা",
            nearbyMarket = "ভীমরুলী ভাসমান বাজার, ঝালকাঠি",
            govAdvice = "রোগমুক্ত পেয়ারা পেতে কচি পেয়ারাতে পলিথিন ব্যাগিং (Fruit Bagging) করুন। এতে কীটনাশক ছাড়াই শতভাগ ভালো পেয়ারা পাওয়া যায়।",
            videoUrl = "https://youtube.com/guava_barishal",
            imageUrl = "🍏"
        )
    )

    // Filter crops based on search query and season selection
    val filteredCrops = cropsList.filter { crop ->
        val matchesSearch = crop.name.contains(searchQuery, ignoreCase = true) ||
                            crop.category.contains(searchQuery, ignoreCase = true) ||
                            crop.suitableSoil.contains(searchQuery, ignoreCase = true)
        val matchesSeason = when (selectedSeasonFilter) {
            "সব ঋতু" -> true
            "গ্রীষ্মকাল" -> crop.season.contains("গ্রীষ্মকাল") || crop.season.contains("Summer")
            "শীতকাল" -> crop.season.contains("শীতকাল") || crop.season.contains("Winter") || crop.season.contains("Boro")
            "বর্ষাকাল" -> crop.season.contains("বর্ষাকাল") || crop.season.contains("Monsoon")
            else -> true
        }
        matchesSearch && matchesSeason
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("স্মার্ট কৃষি ও ফসল ডিরেক্টরি", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("বরিশাল কৃষি তথ্য ও সহায়তা কেন্দ্র", color = TextCyan, fontSize = 10.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedCrop != null) {
                            selectedCrop = null
                        } else {
                            onBack()
                        }
                    }, modifier = Modifier.testTag("agri_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "কৃষি ডেটাবেজ সফলভাবে রিফ্রেশ হয়েছে!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = "Sync", tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavyBackground)
            )
        },
        containerColor = DarkNavyBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedCrop != null) {
                // --- CROP DETAILS VIEW ---
                val crop = selectedCrop!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Hero header
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(NeonCyan.copy(alpha = 0.1f), shape = CircleShape)
                                        .border(BorderStroke(1.dp, NeonCyan.copy(alpha = 0.2f)), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(crop.imageUrl, fontSize = 32.sp)
                                }
                                Column {
                                    Text(text = crop.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                    Text(text = "শ্রেণী: ${crop.category} • উপযুক্ত মাটি: ${crop.suitableSoil}", color = TextCyan, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Economic projection card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = NeonTeal.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, NeonTeal.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "💰 আনুমানিক লাভ ও খরচের খতিয়ান (বিঘা প্রতি)",
                                    color = NeonTeal,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("আনুমানিক উৎপাদন খরচ", color = TextGray, fontSize = 10.sp)
                                        Text(crop.estimatedCost, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("প্রত্যাশিত নীট লাভ", color = NeonTeal, fontSize = 10.sp)
                                        Text(crop.expectedProfit, color = NeonTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Detailed information sections
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AgriDetailSection(title = "🌱 চাষাবাদ পদ্ধতি ও চারা রোপণ", description = crop.cultivationMethod)
                            AgriDetailSection(title = "🚜 জমি প্রস্তুতি", description = crop.landPrep)
                            AgriDetailSection(title = "🧪 রাসায়নিক ও জৈব সার প্রয়োগ", description = crop.fertilizer)
                            AgriDetailSection(title = "💧 সেচ ও পানি নিষ্কাশন ব্যবস্থা", description = crop.waterRequirement)
                            AgriDetailSection(title = "🐛 ক্ষতিকর পোকা, রোগবালাই ও দমন পদ্ধতি", description = "${crop.disease}\nসমাধান: ${crop.solution}")
                            AgriDetailSection(title = "🌾 ফসল কাটার সময় ও প্রত্যাশিত ফলন", description = "সময়কাল: ${crop.growingTime} (${crop.harvestTime})")
                            AgriDetailSection(title = "📢 সরকারি কৃষি দপ্তরের পরামর্শ ও সতর্কবার্তা", description = crop.govAdvice, color = NeonCyan)
                            AgriDetailSection(title = "🏪 নিকটবর্তী পাইকারি বাজার / আড়ত", description = crop.nearbyMarket)
                        }
                    }

                    // Video Link Button
                    item {
                        Button(
                            onClick = {
                                Toast.makeText(context, "ভিডিও টিউটোরিয়াল লোড হচ্ছে...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                                Text("চাষাবাদের ভিডিও নির্দেশিকা দেখুন (YouTube)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            } else {
                // --- CROP LIST & DASHBOARD VIEW ---
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Search Bar and Season filter Row
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = "ফসল বা রোগবালাই খুঁজুন...",
                            placeholder = "যেমন: ধান, তরমুজ, ব্লাস্ট",
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextGray) },
                            testTag = "agri_search_input"
                        )

                        // Horizontal season pills selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("সব ঋতু", "গ্রীষ্মকাল", "শীতকাল", "বর্ষাকাল").forEach { season ->
                                val isSel = selectedSeasonFilter == season
                                Button(
                                    onClick = { selectedSeasonFilter = season },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSel) NeonCyan else DarkNavySurfaceCard,
                                        contentColor = if (isSel) DarkNavyBackground else Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(text = season, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Multi-Tab navigation matching HTML/Android M3
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = DarkNavySurface,
                        contentColor = NeonCyan,
                        edgePadding = 16.dp
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("ফসল গাইড", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("রোগবালাই সমাধান", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("সার নির্দেশিকা ও শেষ চাষ", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = { Text("সরকারি কৃষি স্কিমসমূহ", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    // Tab contents based on selection
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        when (selectedTab) {
                            0 -> {
                                // 1. Crop Guide list
                                if (filteredCrops.isEmpty()) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Agriculture, contentDescription = null, tint = TextGray, modifier = Modifier.size(48.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("কোনো ফসল পাওয়া যায়নি!", color = TextGray, fontSize = 12.sp)
                                    }
                                } else {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(filteredCrops) { crop ->
                                            GlassCard(
                                                onClick = { selectedCrop = crop },
                                                modifier = Modifier.fillMaxWidth().testTag("crop_card_${crop.id}")
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .background(NeonCyan.copy(alpha = 0.08f), shape = CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(crop.imageUrl, fontSize = 24.sp)
                                                    }
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(text = crop.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                        Text(text = "উপযুক্ত মাটি: ${crop.suitableSoil}", color = TextGray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                            modifier = Modifier.padding(top = 4.dp)
                                                        ) {
                                                            Text(text = "মৌসুম: ${crop.season.take(8)}...", color = TextCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                            Text(text = "লাভ: ${crop.expectedProfit}", color = NeonTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                    Icon(Icons.Default.ChevronRight, contentDescription = "View", tint = NeonCyan)
                                                }
                                            }
                                        }
                                        item {
                                            Spacer(modifier = Modifier.height(20.dp))
                                        }
                                    }
                                }
                            }

                            1 -> {
                                // 2. Pest & Disease Solutions
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    cropsList.forEach { crop ->
                                        item {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                                border = BorderStroke(1.dp, GlassBorder)
                                            ) {
                                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(crop.imageUrl, fontSize = 18.sp)
                                                        Text(crop.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    }
                                                    Divider(color = GlassBorder.copy(alpha = 0.5f))
                                                    Text("🐛 সাধারণ রোগবালাই: ${crop.disease}", color = RedEmergency, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    Text("🔬 দমন সমাধান: ${crop.solution}", color = TextWhite, fontSize = 11.sp, lineHeight = 16.sp)
                                                }
                                            }
                                        }
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(20.dp))
                                    }
                                }
                            }

                            2 -> {
                                // 3. Fertilizer guide
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    cropsList.forEach { crop ->
                                        item {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                                border = BorderStroke(1.dp, GlassBorder)
                                            ) {
                                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(crop.imageUrl, fontSize = 18.sp)
                                                        Text(crop.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    }
                                                    Divider(color = GlassBorder.copy(alpha = 0.5f))
                                                    Text("🚜 জমি প্রস্তুতি ও চাষ:", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    Text(crop.landPrep, color = TextWhite, fontSize = 11.sp, lineHeight = 15.sp)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("🧪 সুষম সার নির্দেশিকা:", color = NeonTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    Text(crop.fertilizer, color = TextWhite, fontSize = 11.sp, lineHeight = 15.sp)
                                                }
                                            }
                                        }
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(20.dp))
                                    }
                                }
                            }

                            3 -> {
                                // 4. Government agricultural Programs
                                val schemes = listOf(
                                    AgriScheme("কৃষি পুনর্বাসন ও প্রণোদনা কর্মসূচি", "বন্যায় ক্ষতিগ্রস্ত প্রান্তিক কৃষকদের মাঝে সম্পূর্ণ বিনামূল্যে উচ্চ ফলনশীল বীজ ও সুষম রাসায়নিক সার বিতরণ স্কিম।", "কৃষি সম্প্রসারণ অধিদপ্তর, বরিশাল"),
                                    AgriScheme("খামার যান্ত্রিকীকরণ প্রকল্প (৫০% ভর্তুকি)", "আধুনিক কম্বাইন হারভেস্টার, পাওয়ার টিলার ও রিপার মেশিন ক্রয়ে সরকার পক্ষ থেকে ৫০% সরাসরি ভর্তুকি প্রদান প্রোগ্রাম।", "কৃষি মন্ত্রণালয়, বাংলাদেশ"),
                                    AgriScheme("কৃষক কার্ডের মাধ্যমে স্মার্ট লোন", "মাত্র ৪% সুদ হারে সোনালী ও বাংলাদেশ কৃষি ব্যাংকের মাধ্যমে বিনাজামানতে ক্ষুদ্র কৃষি ঋণ ও নগদ লোন সুবিধা।", "বাংলাদেশ ব্যাংক নির্দেশিকা"),
                                    AgriScheme("ভাসমান পেয়ারা চাষ সংরক্ষণ তহবিল", "স্বরূপকাঠি ও ঝালকাঠির ঐতিহ্যবাহী ভাসমান পেয়ারা বাজার ও চাষীদের ফসল সুরক্ষায় বিশেষ তহবিল প্রণোদনা প্রকল্প।", "বিভাগীয় কমিশনার কার্যালয়, বরিশাল")
                                )

                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(schemes) { scheme ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                            border = BorderStroke(1.dp, GlassBorder)
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(scheme.title, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(scheme.desc, color = TextWhite, fontSize = 11.sp, lineHeight = 16.sp)
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("প্রদানকারী: ${scheme.authority}", color = TextGray, fontSize = 9.sp)
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(NeonTeal.copy(alpha = 0.15f))
                                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("আবেদন সচল", color = NeonTeal, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(20.dp))
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

@Composable
fun AgriDetailSection(
    title: String,
    description: String,
    color: Color = NeonTeal
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = title, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = description, color = TextWhite, fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}

data class CropInfo(
    val id: String,
    val name: String,
    val category: String,
    val season: String,
    val suitableSoil: String,
    val growingTime: String,
    val harvestTime: String,
    val description: String,
    val cultivationMethod: String,
    val landPrep: String,
    val fertilizer: String,
    val waterRequirement: String,
    val disease: String,
    val solution: String,
    val estimatedCost: String,
    val expectedProfit: String,
    val nearbyMarket: String,
    val govAdvice: String,
    val videoUrl: String,
    val imageUrl: String
)

data class AgriScheme(
    val title: String,
    val desc: String,
    val authority: String
)
