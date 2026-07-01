package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.widget.VideoView
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IptvScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val channels by viewModel.iptvChannels.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var activeChannelUrl by remember { mutableStateOf<String?>(null) }
    var activeChannelName by remember { mutableStateOf<String?>(null) }

    // Categories list based on channels
    val categories = remember(channels) {
        val list = mutableListOf("All")
        val unique = channels.map { it.category }.distinct().filter { it.isNotEmpty() }
        list.addAll(unique)
        list
    }

    // Filter channels based on search & category
    val filteredChannels = remember(channels, searchQuery, selectedCategory) {
        channels.filter { ch ->
            val matchesQuery = ch.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || ch.category == selectedCategory
            matchesQuery && matchesCategory
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavyBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NeonCyan
                    )
                }
                Column {
                    Text(
                        "লাইভ টিভি ও স্পোর্টস IPTV",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "সহজে উপভোগ করুন আপনার প্রিয় খেলা ও লাইভ চ্যানেল",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
            }

            // Embedded Video Player Section (Shows at top if active)
            AnimatedVisibility(
                visible = activeChannelUrl != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                activeChannelUrl?.let { streamUrl ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(Color.Black)
                            ) {
                                AndroidView(
                                    factory = { ctx ->
                                        VideoView(ctx).apply {
                                            layoutParams = ViewGroup.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.MATCH_PARENT
                                            )
                                            setOnPreparedListener { mp ->
                                                mp.isLooping = true
                                                start()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    update = { videoView ->
                                        try {
                                            videoView.stopPlayback()
                                            videoView.setVideoURI(Uri.parse(streamUrl))
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                )

                                // Overlay indicators
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .background(Color.Red, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(Color.White, CircleShape)
                                        )
                                        Text("LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        activeChannelUrl = null
                                        activeChannelName = null
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close player", tint = Color.White)
                                }
                            }

                            // Active Stream Name
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.LiveTv, contentDescription = "Playing", tint = NeonCyan)
                                    Text(
                                        activeChannelName ?: "IPTV লাইভ স্ট্রিম",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(Uri.parse(streamUrl), "video/*")
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback
                                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(streamUrl))
                                            context.startActivity(browserIntent)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.1f)),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("External Player", color = NeonCyan, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar & Filter options
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("iptv_channel_search"),
                    placeholder = { Text("চ্যানেল খুঁজুন...", color = TextGray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = NeonCyan) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextGray)
                            }
                        }
                    },
                    singleLine = true
                )
            }

            // Category Chips row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(32.dp))
                            .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f))
                            .border(
                                BorderStroke(1.dp, if (isSelected) NeonCyan else GlassBorder),
                                RoundedCornerShape(32.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) NeonCyan else TextGray,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // Channels List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                if (filteredChannels.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LiveTv,
                            contentDescription = "No channels",
                            tint = TextGray,
                            modifier = Modifier.size(52.dp)
                        )
                        Text(
                            "কোনো চ্যানেল খুঁজে পাওয়া যায়নি!",
                            color = TextGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "অ্যাডমিন প্যানেল থেকে m3u ফাইল আপলোড করুন",
                            color = TextGray.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredChannels) { channel ->
                            val isPlayingThis = activeChannelUrl == channel.url
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("iptv_channel_card_${channel.id}"),
                                borderWidth = if (isPlayingThis) 2.dp else 1.dp,
                                glowColor = if (isPlayingThis) NeonCyan else Color.Transparent,
                                onClick = {
                                    activeChannelUrl = channel.url
                                    activeChannelName = channel.name
                                }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Custom Logo placeholder or real logo
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(if (isPlayingThis) NeonCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (channel.logo.isNotEmpty() && channel.logo.startsWith("http")) {
                                            // Mock/custom image loading fallback as vector icon if coil not available or loads
                                            Icon(
                                                imageVector = Icons.Default.Tv,
                                                contentDescription = channel.name,
                                                tint = if (isPlayingThis) NeonCyan else Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Tv,
                                                contentDescription = channel.name,
                                                tint = if (isPlayingThis) NeonCyan else Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        channel.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isPlayingThis) NeonCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            channel.category.ifEmpty { "Sports" },
                                            color = if (isPlayingThis) NeonCyan else TextGray,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
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
