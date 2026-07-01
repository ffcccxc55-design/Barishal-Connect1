package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DirectoryItem
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel

@Composable
fun SearchTab(
    viewModel: BarishalViewModel,
    onItemClick: (DirectoryItem) -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val selectedCat by viewModel.selectedCategory.collectAsState()
    val results by viewModel.filteredItems.collectAsState()

    val categories = listOf(
        CategoryChipData(null, "সব"),
        CategoryChipData("road", "রোড"),
        CategoryChipData("worker", "কর্মী"),
        CategoryChipData("job", "চাকরি"),
        CategoryChipData("hospital", "হাসপাতাল"),
        CategoryChipData("doctor", "ডাক্তার"),
        CategoryChipData("school", "শিক্ষা"),
        CategoryChipData("tourist", "পর্যটন"),
        CategoryChipData("bus", "বাস"),
        CategoryChipData("launch", "লঞ্চ"),
        CategoryChipData("market", "বাজার দর"),
        CategoryChipData("gov", "সরকারি অফিস"),
        CategoryChipData("emergency", "জরুরি"),
        CategoryChipData("business", "ব্যবসা"),
        CategoryChipData("blood", "রক্তদান")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Global Search Bar
        GlassTextField(
            value = query,
            onValueChange = { viewModel.setQuery(it) },
            label = "অনুসন্ধান করুন",
            placeholder = "হাসপাতাল, লঞ্চ, ডিরেক্টরি...",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = NeonCyan
                )
            },
            testTag = "global_search_input"
        )

        // Horizontal Category Filter List
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(categories) { catData ->
                FilterChip(
                    selected = selectedCat == catData.id,
                    onClick = { viewModel.selectCategory(catData.id) },
                    label = { Text(catData.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan,
                        selectedLabelColor = DarkNavyBackground,
                        containerColor = DarkNavySurfaceCard,
                        labelColor = TextWhite
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedCat == catData.id,
                        selectedBorderColor = NeonCyan,
                        borderColor = GlassBorder
                    ),
                    modifier = Modifier.testTag("search_filter_chip_${catData.id ?: "all"}")
                )
            }
        }

        // Search Results List
        if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "No results",
                        tint = TextGray.copy(alpha = 0.3f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "কোন তথ্য পাওয়া যায়নি",
                        color = TextGray,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "অন্য কোনো কিওয়ার্ড দিয়ে পুনরায় চেষ্টা করুন",
                        color = TextGray.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("search_results_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(results) { item ->
                    SearchResultRow(item = item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}

@Composable
fun SearchResultRow(
    item: DirectoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("search_result_item_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Badge
                    Surface(
                        color = NeonCyan.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Text(
                            text = getCategoryBanglaName(item.category).uppercase(),
                            color = NeonCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (item.statusOrSchedule.isNotEmpty() && item.category == "road") {
                        Surface(
                            color = if (item.statusOrSchedule.contains("Smooth") || item.statusOrSchedule.contains("স্বাভাবিক")) NeonTeal.copy(alpha = 0.15f) else RedEmergency.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = item.statusOrSchedule,
                                color = if (item.statusOrSchedule.contains("Smooth") || item.statusOrSchedule.contains("স্বাভাবিক")) NeonTeal else RedEmergency,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.subtitle,
                    color = TextCyan,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.description,
                    color = TextGray,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Ratings or special details
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = NeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "%.1f".format(item.rating),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (item.priceOrFee.isNotEmpty()) {
                    Text(
                        text = item.priceOrFee,
                        color = NeonTeal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun getCategoryBanglaName(category: String): String {
    return when (category) {
        "road" -> "সড়ক"
        "worker" -> "কর্মী"
        "job" -> "চাকরি"
        "hospital" -> "হাসপাতাল"
        "doctor" -> "ডাক্তার"
        "school" -> "শিক্ষা"
        "tourist" -> "পর্যটন"
        "bus" -> "বাস"
        "launch" -> "লঞ্চ"
        "market" -> "বাজার"
        "gov" -> "সরকারি"
        "emergency" -> "জরুরি"
        "business" -> "ব্যবসা"
        "blood" -> "রক্তদাতা"
        else -> category
    }
}

data class CategoryChipData(
    val id: String?,
    val displayName: String
)
