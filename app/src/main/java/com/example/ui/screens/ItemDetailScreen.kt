package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.DirectoryItem
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: String,
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allItems by viewModel.allItems.collectAsState()
    val item = remember(allItems, itemId) {
        allItems.find { it.id == itemId }
    }

    if (item == null) {
        Scaffold(containerColor = DarkNavyBackground) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(text = "তথ্য খুঁজে পাওয়া যায়নি", color = TextWhite)
            }
        }
        return
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "সেবার বিবরণী", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.toggleFavorite(item.id, item.isFavorite)
                            Toast.makeText(
                                context,
                                if (item.isFavorite) "প্রিয় তালিকা থেকে সরানো হয়েছে" else "প্রিয় তালিকায় যুক্ত হয়েছে",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.testTag("detail_favorite_toggle")
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) RedEmergency else Color.White
                        )
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
        ) {
            // 1. Hero Image / Colored Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(DarkNavySurface, DarkNavyBackground)
                        )
                    )
            ) {
                if (item.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(item.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient dark overlay over image
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, DarkNavyBackground.copy(alpha = 0.9f))
                                )
                            )
                    )
                } else {
                    // Geometric colored glow banner for text fallback
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(DarkNavySurface, ElectricBlue.copy(alpha = 0.2f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = NeonCyan.copy(alpha = 0.4f),
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
                
                // Floating category badge
                Surface(
                    color = NeonCyan,
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = getCategoryBanglaName(item.category).uppercase(),
                        color = DarkNavyBackground,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // 2. Content Details Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Titles
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 28.sp
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Text(
                            text = item.subtitle,
                            color = TextCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Specs Quick Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Rating card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                        border = BorderStroke(1.dp, GlassBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = "Rating", tint = NeonCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "%.1f / 5.0".format(item.rating), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "রেটিং", color = TextGray, fontSize = 10.sp)
                        }
                    }

                    // Special tag card (price or condition)
                    if (item.priceOrFee.isNotEmpty() || item.statusOrSchedule.isNotEmpty()) {
                        Card(
                            modifier = Modifier.weight(1.5f),
                            colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val specIcon = if (item.category == "road") Icons.Default.Traffic else Icons.Default.Payments
                                Icon(imageVector = specIcon, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (item.category == "road") item.statusOrSchedule else item.priceOrFee,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (item.category == "road") "ট্রাফিক অবস্থা" else "ফি / মূল্য",
                                    color = TextGray,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                // Description Block
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "সেবার বিবরণী (Details)",
                            color = NeonCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.description,
                            color = TextWhite.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Location Details card
                if (item.location.isNotEmpty()) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Map Location",
                                tint = ElectricBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(text = "ঠিকানা (Address)", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = item.location, color = TextWhite, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Contact Details card
                if (item.contactPhone.isNotEmpty()) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneEnabled,
                                    contentDescription = "Phone",
                                    tint = NeonTeal,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(text = "যোগাযোগ নম্বর (Contact)", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(text = item.contactPhone, color = TextWhite, fontSize = 13.sp)
                                }
                            }
                            
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${item.contactPhone}")
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .background(NeonTeal.copy(alpha = 0.2f), shape = CircleShape)
                                    .size(40.dp)
                                    .testTag("dial_button")
                            ) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = NeonTeal)
                            }
                        }
                    }
                }

                // Main bottom working action CTA
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (item.contactPhone.isNotEmpty()) {
                        GlowButton(
                            text = "সরাসরি কল করুন (Dial)",
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${item.contactPhone}")
                                }
                                context.startActivity(intent)
                            },
                            containerColor = NeonTeal,
                            textColor = DarkNavyBackground,
                            modifier = Modifier.weight(1f),
                            testTag = "detail_action_dial"
                        )
                    }

                    GlowButton(
                        text = "মানচিত্রে দেখুন (Map)",
                        onClick = {
                            val mapUri = Uri.parse("geo:22.7010,90.3535?q=${Uri.encode(item.title + " " + item.subtitle)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                                `package` = "com.google.android.apps.maps"
                            }
                            try {
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "গুগল ম্যাপ পাওয়া যায়নি, ঠিকানা কপি করা হল।", Toast.LENGTH_SHORT).show()
                            }
                        },
                        containerColor = NeonCyan,
                        textColor = DarkNavyBackground,
                        modifier = Modifier.weight(1.2f),
                        testTag = "detail_action_map"
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
