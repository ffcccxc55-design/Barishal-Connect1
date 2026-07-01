package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DirectoryItem
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    category: String,
    viewModel: BarishalViewModel,
    onBack: () -> Unit,
    onItemClick: (DirectoryItem) -> Unit
) {
    val allItems by viewModel.allItems.collectAsState()
    val filteredList = remember(allItems, category) {
        allItems.filter { it.category == category }
    }

    val categoryTitle = remember(category) {
        getCategoryBanglaTitle(category)
    }
    val categorySubtitle = remember(category) {
        getCategoryBanglaSubtitle(category)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = categoryTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("category_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            SectionHeader(title = categoryTitle, subtitle = categorySubtitle)

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TextGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "কোন ডেটা পাওয়া যায়নি", color = TextGray, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("category_items_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredList) { item ->
                        SearchResultRow(item = item, onClick = { onItemClick(item) })
                    }
                }
            }
        }
    }
}

fun getCategoryBanglaTitle(category: String): String {
    return when (category) {
        "road" -> "স্মার্ট রোড আপডেট"
        "worker" -> "কর্মী ডিরেক্টরি"
        "job" -> "স্থানীয় চাকরি"
        "hospital" -> "হাসপাতাল তালিকা"
        "doctor" -> "বিশেষজ্ঞ ডাক্তার চেম্বার"
        "school" -> "শিক্ষা প্রতিষ্ঠান"
        "tourist" -> "দর্শনীয় স্থান"
        "bus" -> "বাস সময়সূচী"
        "launch" -> "লঞ্চ সময়সূচী"
        "market" -> "বাজার দর আপডেট"
        "gov" -> "সরকারি দপ্তরসমূহ"
        "emergency" -> "জরুরি হেল্পলাইন নম্বর"
        else -> category.replaceFirstChar { it.uppercase() }
    }
}

fun getCategoryBanglaSubtitle(category: String): String {
    return when (category) {
        "road" -> "বিভাগের প্রধান সড়কসমূহের ট্রাফিক অবস্থা ও নির্দেশনা"
        "worker" -> "বাসা-বাড়ির কাজ বা টেকনিক্যাল কাজের দক্ষ শ্রমিকবৃন্দ"
        "job" -> "বরিশাল বিভাগের সাম্প্রতিক স্থানীয় চাকরির নিয়োগ"
        "hospital" -> "২৪ ঘণ্টা জরুরি সেবার সরকারি ও বেসরকারি হাসপাতাল"
        "doctor" -> "সেরা সার্জন ও মেডিসিন বিশেষজ্ঞদের চেম্বার ও কন্টাক্ট"
        "school" -> "বিভাগের শীর্ষস্থানীয় কলেজ, মাদ্রাসা ও বিশ্ববিদ্যালয়"
        "tourist" -> "ভ্রমণ পিপাসুদের জন্য দক্ষিণাঞ্চলের বিখ্যাত স্পটসমূহ"
        "bus" -> "পদ্মা সেতু হয়ে দূরপাল্লার বাসের সময় ও টিকিট মূল্য"
        "launch" -> "ঢাকা-বরিশাল রুটের আধুনিক যাত্রীবাহী লঞ্চের সময়"
        "market" -> "নিত্যপ্রয়োজনীয় জিনিসপত্রের প্রতিদিনের সঠিক বাজারমূল্য"
        "gov" -> "বিভাগীয় ও জেলা পর্যায়ের মূল প্রশাসনিক দপ্তরসমূহ"
        "emergency" -> "যেকোনো বিপদে পুলিশ, ফায়ার ও উদ্ধারকারী দল"
        else -> "Barishal Division Information Directory"
    }
}
