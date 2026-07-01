package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun MarketScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    
    // Selectors
    var selectedDistrict by remember { mutableStateOf("Barishal") }
    var selectedMarket by remember { mutableStateOf("Port Road Mokam") }
    var selectedCategory by remember { mutableStateOf("সব") }
    var searchQuery by remember { mutableStateOf("") }
    
    // Dropdown visibility
    var showDistrictMenu by remember { mutableStateOf(false) }
    var showMarketMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    
    // Admin Override state
    var editingItem by remember { mutableStateOf<MarketItem?>(null) }
    var editPriceText by remember { mutableStateOf("") }
    
    val districts = listOf("Barishal", "Jhalakathi", "Pirojpur", "Patuakhali", "Bhola", "Barguna")
    val marketsMap = mapOf(
        "Barishal" to listOf("Port Road Mokam", "Natun Bazar", "Chowmatha Bazar", "Rupatali Bazar"),
        "Jhalakathi" to listOf("Sadar Bazar", "Nalchity Bazar", "Bhimruli Bazar"),
        "Pirojpur" to listOf("Town Bazar", "Swarupkathi Mokam"),
        "Patuakhali" to listOf("Sadar Bazar", "Chawk Bazar"),
        "Bhola" to listOf("Sadar Mokam", "Char Fasson Bazar"),
        "Barguna" to listOf("Sadar Bazar", "Amtali Bazar")
    )
    
    val markets = marketsMap[selectedDistrict] ?: listOf("Port Road Mokam")
    val categories = listOf("সব", "মাছ ও মাংস", "চাল ও ডাল", "শাকসবজি", "ফলমূল", "ডিম ও দুধ", "সার ও কীটনাশক", "নিত্যপ্রয়োজনীয়")
    
    // Dynamic List of Market Items
    var marketItems by remember {
        mutableStateOf(
            listOf(
                MarketItem("m1", "রুপালি ইলিশ (Hilsa - 1kg size)", "মাছ ও মাংস", 1350, 1400, "Port Road Mokam", "Barishal", listOf(1420, 1410, 1400, 1380, 1390, 1400, 1350)),
                MarketItem("m2", "মিনিকেট চাল (Miniket Rice)", "চাল ও ডাল", 68, 66, "Natun Bazar", "Barishal", listOf(64, 65, 65, 66, 66, 66, 68)),
                MarketItem("m3", "কাঁচা মরিচ (Green Chili)", "শাকসবজি", 120, 160, "Chowmatha Bazar", "Barishal", listOf(180, 170, 165, 160, 150, 160, 120)),
                MarketItem("m4", "ব্রয়লার মুরগি (Broiler Chicken)", "মাছ ও মাংস", 195, 190, "Port Road Mokam", "Barishal", listOf(185, 190, 190, 188, 192, 190, 195)),
                MarketItem("m5", "দেশি তরমুজ (Watermelon)", "ফলমূল", 220, 250, "Rupatali Bazar", "Barishal", listOf(280, 270, 260, 250, 250, 250, 220)),
                MarketItem("m6", "ডিম (Farm Egg - 12 pcs)", "ডিম ও দুধ", 145, 145, "Natun Bazar", "Barishal", listOf(142, 142, 145, 145, 145, 145, 145)),
                MarketItem("m7", "তরল গরুর দুধ (Fresh Cow Milk)", "ডিম ও দুধ", 80, 75, "Natun Bazar", "Barishal", listOf(75, 75, 75, 75, 75, 75, 80)),
                MarketItem("m8", "ইউরিয়া সার (Urea Fertilizer)", "সার ও কীটনাশক", 27, 27, "Chowmatha Bazar", "Barishal", listOf(25, 25, 27, 27, 27, 27, 27)),
                MarketItem("m9", "পেঁয়াজ (Onion - Local)", "নিত্যপ্রয়োজনীয়", 85, 80, "Natun Bazar", "Barishal", listOf(78, 79, 80, 80, 80, 80, 85))
            )
        )
    }

    // Filtered items list
    val filteredItems = marketItems.filter { item ->
        val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) ||
                            item.category.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "সব" || item.category == selectedCategory
        val matchesDistrict = item.district == selectedDistrict
        
        matchesSearch && matchesCategory && matchesDistrict
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("লাইভ বাজার দর ড্যাশবোর্ড", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("বরিশাল পাইকারি ও খুচরা বাজার দর", color = TextCyan, fontSize = 10.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("market_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "বাজার দর সফলভাবে হালনাগাদ করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = "Sync", tint = NeonCyan)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dropdown selectors row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. District selector
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { showDistrictMenu = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkNavySurfaceCard),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("জেলা: $selectedDistrict", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(
                            expanded = showDistrictMenu,
                            onDismissRequest = { showDistrictMenu = false },
                            modifier = Modifier.background(DarkNavySurface)
                        ) {
                            districts.forEach { dist ->
                                DropdownMenuItem(
                                    text = { Text(dist, color = Color.White) },
                                    onClick = {
                                        selectedDistrict = dist
                                        selectedMarket = marketsMap[dist]?.first() ?: "Sadar Bazar"
                                        showDistrictMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // 2. Market selector
                    Box(modifier = Modifier.weight(1.2f)) {
                        Button(
                            onClick = { showMarketMenu = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkNavySurfaceCard),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("বাজার: $selectedMarket", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(
                            expanded = showMarketMenu,
                            onDismissRequest = { showMarketMenu = false },
                            modifier = Modifier.background(DarkNavySurface)
                        ) {
                            markets.forEach { market ->
                                DropdownMenuItem(
                                    text = { Text(market, color = Color.White) },
                                    onClick = {
                                        selectedMarket = market
                                        showMarketMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // 3. Category selector
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { showCategoryMenu = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkNavySurfaceCard),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedCategory, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            }
                        }
                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false },
                            modifier = Modifier.background(DarkNavySurface)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = Color.White) },
                                    onClick = {
                                        selectedCategory = cat
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Search Box
                GlassTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "পণ্য খুঁজুন (যেমন: আলু, পেঁয়াজ, চাল)...",
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextGray) },
                    testTag = "market_search_input"
                )

                // Admin view banner indicator
                if (isAdminLoggedIn) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NeonCyan.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = NeonCyan)
                            Text(
                                "অ্যাডমিন ওভাররাইড সচল! যেকোনো পণ্যের ওপর ট্যাপ করে সরাসরি মূল্য সংশোধন করতে পারবেন।",
                                color = Color.White,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // List header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("পণ্যের তালিকা • ${filteredItems.size} টি পাওয়া গেছে", color = TextGray, fontSize = 11.sp)
                    Text("১ ঘণ্টা পূর্বে সিঙ্কড", color = TextCyan, fontSize = 9.sp)
                }

                // Items list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredItems) { item ->
                        val diff = item.currentPrice - item.yesterdayPrice
                        val isPriceUp = diff > 0
                        val isPriceStable = diff == 0
                        
                        GlassCard(
                            onClick = {
                                if (isAdminLoggedIn) {
                                    editingItem = item
                                    editPriceText = item.currentPrice.toString()
                                } else {
                                    Toast.makeText(context, "${item.name}: বর্তমান দাম ${item.currentPrice} টাকা", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("market_card_${item.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Category Icon box
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            if (isPriceStable) TextGray.copy(alpha = 0.1f)
                                            else if (isPriceUp) RedEmergency.copy(alpha = 0.1f)
                                            else NeonTeal.copy(alpha = 0.1f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPriceStable) Icons.Default.Remove
                                                      else if (isPriceUp) Icons.Default.TrendingUp
                                                      else Icons.Default.TrendingDown,
                                        contentDescription = "Trend",
                                        tint = if (isPriceStable) TextGray
                                               else if (isPriceUp) RedEmergency
                                               else NeonTeal,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // Product details
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("শ্রেণী: ${item.category} • ${item.marketName}", color = TextGray, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isPriceStable) "অপরিবর্তিত"
                                               else if (isPriceUp) "বৃদ্ধি: +$diff টাকা"
                                               else "হ্রাস: $diff টাকা",
                                        color = if (isPriceStable) TextGray
                                               else if (isPriceUp) RedEmergency
                                               else NeonTeal,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Interactive price mini trend sparkline (Canvas-drawn graph)
                                Box(
                                    modifier = Modifier
                                        .size(width = 60.dp, height = 30.dp)
                                        .padding(horizontal = 2.dp)
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val points = item.history
                                        if (points.isNotEmpty()) {
                                            val w = size.width
                                            val h = size.height
                                            val minPrice = points.minOrNull() ?: 0
                                            val maxPrice = points.maxOrNull() ?: 100
                                            val priceRange = (maxPrice - minPrice).coerceAtLeast(1)
                                            
                                            val path = Path()
                                            points.forEachIndexed { idx, price ->
                                                val x = (w / (points.size - 1)) * idx
                                                val normY = (price - minPrice).toFloat() / priceRange
                                                val y = h - (normY * (h - 6f)) - 3f
                                                if (idx == 0) {
                                                    path.moveTo(x, y)
                                                } else {
                                                    path.lineTo(x, y)
                                                }
                                            }
                                            drawPath(
                                                path = path,
                                                color = if (isPriceUp) RedEmergency.copy(alpha = 0.8f) else NeonTeal.copy(alpha = 0.8f),
                                                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                                            )
                                        }
                                    }
                                }

                                // Current price tag display
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.weight(0.8f)
                                ) {
                                    Text(
                                        text = "৳ ${item.currentPrice}",
                                        color = if (isPriceStable) Color.White else if (isPriceUp) RedEmergency else NeonTeal,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text("পূর্বের: ৳ ${item.yesterdayPrice}", color = TextGray, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }

            // --- ADMIN OVERRIDE EDIT DIALOG ---
            if (editingItem != null) {
                val item = editingItem!!
                AlertDialog(
                    onDismissRequest = { editingItem = null },
                    containerColor = DarkNavySurface,
                    title = { Text("দাম পরিবর্তন করুন", color = Color.White, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(item.name, color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("বাজার: ${item.marketName}", color = TextGray, fontSize = 11.sp)
                            
                            OutlinedTextField(
                                value = editPriceText,
                                onValueChange = { editPriceText = it },
                                label = { Text("নতুন বর্তমান মূল্য (৳)", color = TextCyan) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("admin_price_override_field")
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val newP = editPriceText.toIntOrNull()
                                if (newP != null) {
                                    marketItems = marketItems.map { mi ->
                                        if (mi.id == item.id) {
                                            mi.copy(
                                                yesterdayPrice = mi.currentPrice,
                                                currentPrice = newP,
                                                history = mi.history.takeLast(6) + newP
                                            )
                                        } else mi
                                    }
                                    Toast.makeText(context, "মূল্য সফলভাবে আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                                    editingItem = null
                                } else {
                                    Toast.makeText(context, "সঠিক সংখ্যা দিন!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("admin_price_save_confirm")
                        ) {
                            Text("সংরক্ষণ", color = NeonCyan, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { editingItem = null }) {
                            Text("বাতিল", color = TextGray)
                        }
                    }
                )
            }
        }
    }
}

data class MarketItem(
    val id: String,
    val name: String,
    val category: String,
    val currentPrice: Int,
    val yesterdayPrice: Int,
    val marketName: String,
    val district: String,
    val history: List<Int>
)
