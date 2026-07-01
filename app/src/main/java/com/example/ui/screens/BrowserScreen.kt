package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import android.widget.VideoView
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val searchEngineUrl by viewModel.browserSearchEngine.collectAsState()
    val homepageUrlSetting by viewModel.browserHomepageUrl.collectAsState()
    val bookmarks by viewModel.browserBookmarks.collectAsState()
    val history by viewModel.browserHistory.collectAsState()
    val downloads by viewModel.downloadTasks.collectAsState()

    var urlInput by remember { mutableStateOf("") }
    var currentUrl by remember { mutableStateOf("") }
    var currentTitle by remember { mutableStateOf("") }
    var isLoadingWeb by remember { mutableStateOf(false) }
    var isBrowsingActive by remember { mutableStateOf(false) }

    // Navigation and Browser states
    var isNightMode by remember { mutableStateOf(false) }
    var isIncognito by remember { mutableStateOf(false) }
    var isDesktopSite by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Dialog & UI Visibility states
    var showBottomMenu by remember { mutableStateOf(false) }
    var showDownloadOptionsSheet by remember { mutableStateOf(false) }
    var showBookmarksDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showDownloadManager by remember { mutableStateOf(false) }
    var activeVideoUrlForPlayer by remember { mutableStateOf<String?>(null) }

    val externalUrlState by viewModel.browserTargetUrl.collectAsState()
    LaunchedEffect(externalUrlState) {
        externalUrlState?.let { url ->
            if (url.isNotEmpty()) {
                currentUrl = url
                urlInput = url
                isBrowsingActive = true
                viewModel.browserTargetUrl.value = null // reset
                webViewInstance?.loadUrl(url)
            }
        }
    }

    // Webview reference settings update helper
    LaunchedEffect(isDesktopSite) {
        webViewInstance?.let { webView ->
            val settings = webView.settings
            if (isDesktopSite) {
                settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            } else {
                settings.userAgentString = null
            }
            webView.reload()
        }
    }

    // Main layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isIncognito) Color(0xFF121214) else DarkNavyBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Address / Search Bar Top
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = if (isIncognito) Color(0xFF1E1E22) else DarkNavySurfaceCard),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(onClick = {
                        if (isBrowsingActive) {
                            if (webViewInstance?.canGoBack() == true) {
                                webViewInstance?.goBack()
                            } else {
                                isBrowsingActive = false
                                currentUrl = ""
                                urlInput = ""
                            }
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NeonCyan
                        )
                    }

                    TextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("browser_address_input"),
                        placeholder = {
                            Text(
                                "সার্চ করুন অথবা URL লিখুন...",
                                color = TextGray,
                                fontSize = 13.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = if (isIncognito) Icons.Default.PrivacyTip else Icons.Default.Search,
                                contentDescription = "Search icon",
                                tint = if (isIncognito) Color.Red else NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (urlInput.isNotEmpty()) {
                                IconButton(onClick = { urlInput = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = TextGray
                                    )
                                }
                            }
                        }
                    )

                    // Refresh / Load Go Button
                    IconButton(
                        onClick = {
                            if (urlInput.trim().isNotEmpty()) {
                                val destination = urlInput.trim()
                                val targetUrl = if (destination.startsWith("http://") || destination.startsWith("https://")) {
                                    destination
                                } else if (destination.contains(".") && !destination.contains(" ")) {
                                    "https://$destination"
                                } else {
                                    "$searchEngineUrl${Uri.encode(destination)}"
                                }
                                currentUrl = targetUrl
                                urlInput = targetUrl
                                isBrowsingActive = true
                                webViewInstance?.loadUrl(targetUrl)
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (isIncognito) Color.Red.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.1f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Go",
                            tint = if (isIncognito) Color.Red else NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Direct Download Detection Trigger Button
                    if (isBrowsingActive) {
                        IconButton(
                            onClick = { showDownloadOptionsSheet = true },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.Red.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Download Video",
                                tint = Color.Red,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // WebView or Browser Homepage Grid
            if (isBrowsingActive) {
                Box(modifier = Modifier.weight(1f)) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                        isLoadingWeb = true
                                        url?.let {
                                            currentUrl = it
                                            urlInput = it
                                        }
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isLoadingWeb = false
                                        url?.let {
                                            currentUrl = it
                                            urlInput = it
                                            if (!isIncognito) {
                                                viewModel.addHistory(view?.title ?: "ওয়েব পেজ", it)
                                            }
                                        }
                                        currentTitle = view?.title ?: ""
                                    }

                                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                        url?.let { view?.loadUrl(it) }
                                        return true
                                    }
                                }
                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        if (newProgress >= 100) isLoadingWeb = false
                                    }
                                }

                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    loadsImagesAutomatically = true
                                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                }
                                webViewInstance = this
                                loadUrl(currentUrl)
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { webView ->
                            webViewInstance = webView
                        }
                    )

                    if (isLoadingWeb) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter),
                            color = NeonCyan,
                            trackColor = Color.Transparent
                        )
                    }
                }
            } else {
                // Browser Homepage layout matching Screenshot 1 & 2
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Homepage",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = {
                                Toast.makeText(context, "QR Scanner Activated", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Scanner",
                                    tint = NeonCyan
                                )
                            }
                        }
                    }

                    item {
                        // Hero Logo
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(NeonCyan.copy(alpha = 0.2f), Color.Transparent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Browser Logo",
                                tint = NeonCyan,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                        Text(
                            "Lightweight Smart Browser",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            "High-speed ad-free browsing & premium video downloading",
                            color = TextGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )
                    }

                    item {
                        // Quick Access Shortcuts (Google, DorkGPT, Free, YouTube)
                        Text(
                            "জনপ্রিয় সাইট ও শর্টকাট",
                            color = NeonCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            textAlign = TextAlign.Start
                        )

                        val quickShortcuts = listOf(
                            Triple("Google", "https://www.google.com", Color(0xFF4CAF50)),
                            Triple("YouTube", "https://www.youtube.com", Color(0xFFEF4444)),
                            Triple("টেস্ট ভিডিও 🎥", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4", Color(0xFFE91E63)),
                            Triple("টেস্ট অডিও 🎵", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", Color(0xFF9C27B0)),
                            Triple("Facebook", "https://www.facebook.com", Color(0xFF1877F2)),
                            Triple("Twitter / X", "https://x.com", Color(0xFF000000)),
                            Triple("DorkGPT - AI", "https://chat.openai.com", Color(0xFF3B82F6)),
                            Triple("Wikipedia", "https://www.wikipedia.org", Color(0xFF607D8B))
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .height(170.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(quickShortcuts) { (name, link, color) ->
                                Column(
                                    modifier = Modifier
                                        .clickable {
                                            currentUrl = link
                                            urlInput = link
                                            isBrowsingActive = true
                                        }
                                        .padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(color),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            name.take(1),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        name,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Dynamic Buttons Created by Admin inside Web Browser
                    item {
                        val dynamicList by viewModel.dynamicButtons.collectAsState()
                        if (dynamicList.isNotEmpty()) {
                            Text(
                                "অ্যাডমিন ডায়নামিক বাটনসমূহ",
                                color = NeonCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                textAlign = TextAlign.Start
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                dynamicList.filter { it.type == "url" }.forEach { btn ->
                                    GlassCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                currentUrl = btn.target
                                                urlInput = btn.target
                                                isBrowsingActive = true
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Launch,
                                                contentDescription = "Launch",
                                                tint = NeonCyan
                                            )
                                            Column {
                                                Text(btn.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(btn.target, color = TextGray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation menu/options strip
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isIncognito) Color(0xFF1E1E22) else DarkNavySurfaceCard),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        webViewInstance?.let {
                            if (it.canGoBack()) it.goBack()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Web Back", tint = Color.White)
                    }

                    IconButton(onClick = {
                        webViewInstance?.let {
                            if (it.canGoForward()) it.goForward()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Web Forward", tint = Color.White)
                    }

                    IconButton(onClick = {
                        isBrowsingActive = false
                        currentUrl = ""
                        urlInput = ""
                    }) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Home", tint = NeonCyan)
                    }

                    IconButton(onClick = { showDownloadManager = true }) {
                        Box {
                            val activeDls = downloads.count { it.status == "DOWNLOADING" }
                            Icon(imageVector = Icons.Default.Download, contentDescription = "Downloads list", tint = Color.White)
                            if (activeDls > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color.Red, CircleShape)
                                        .align(Alignment.TopEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(activeDls.toString(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    IconButton(onClick = { showBottomMenu = true }) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu Options", tint = Color.White)
                    }
                }
            }
        }

        // Night Mode overlay
        if (isNightMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(enabled = false) {}
            )
        }

        // Bottom Menu Sheet Dialog (Matching Screenshot 2 layout)
        if (showBottomMenu) {
            AlertDialog(
                onDismissRequest = { showBottomMenu = false },
                title = { Text("ব্রাউজার অপশনসমূহ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        val menuItems = listOf(
                            Triple("Night Mode", Icons.Default.NightsStay, if (isNightMode) NeonCyan else Color.White),
                            Triple("Bookmarks", Icons.Default.BookmarkBorder, Color.White),
                            Triple("History", Icons.Default.History, Color.White),
                            Triple("Downloads", Icons.Default.FileDownload, Color.White),
                            Triple("Incognito Mode", Icons.Default.VisibilityOff, if (isIncognito) Color.Red else Color.White),
                            Triple("Share Link", Icons.Default.Share, Color.White),
                            Triple("Add Bookmark", Icons.Default.BookmarkAdd, Color.White),
                            Triple("Desktop Site", Icons.Default.DesktopMac, if (isDesktopSite) NeonCyan else Color.White)
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .height(180.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(menuItems) { (label, icon, color) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showBottomMenu = false
                                            when (label) {
                                                "Night Mode" -> isNightMode = !isNightMode
                                                "Bookmarks" -> showBookmarksDialog = true
                                                "History" -> showHistoryDialog = true
                                                "Downloads" -> showDownloadManager = true
                                                "Incognito Mode" -> isIncognito = !isIncognito
                                                "Add Bookmark" -> {
                                                    if (currentUrl.isNotEmpty()) {
                                                        viewModel.addBookmark(currentTitle.ifEmpty { "ওয়েব পেজ" }, currentUrl)
                                                        Toast.makeText(context, "বুকমার্ক সফলভাবে যুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "যুক্ত করার মতো কোনো সচল ইউআরএল নেই", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                "Desktop Site" -> isDesktopSite = !isDesktopSite
                                                "Share Link" -> {
                                                    if (currentUrl.isNotEmpty()) {
                                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                                            type = "text/plain"
                                                            putExtra(Intent.EXTRA_TEXT, currentUrl)
                                                        }
                                                        context.startActivity(Intent.createChooser(intent, "লিংক শেয়ার করুন"))
                                                    }
                                                }
                                            }
                                        },
                                    colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                    border = BorderStroke(1.dp, GlassBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(label, color = Color.White, fontSize = 9.sp, textAlign = TextAlign.Center, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBottomMenu = false }) {
                        Text("বন্ধ করুন", color = NeonCyan)
                    }
                },
                containerColor = DarkNavySurface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(16.dp))
            )
        }

        // Bookmarks list Dialog
        if (showBookmarksDialog) {
            AlertDialog(
                onDismissRequest = { showBookmarksDialog = false },
                title = { Text("সংরক্ষিত বুকমার্কসমূহ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Box(modifier = Modifier.height(280.dp).fillMaxWidth()) {
                        if (bookmarks.isEmpty()) {
                            Text("কোনো বুকমার্ক নেই", color = TextGray, modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(bookmarks) { b ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                currentUrl = b.url
                                                urlInput = b.url
                                                isBrowsingActive = true
                                                showBookmarksDialog = false
                                            }
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(b.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            Text(b.url, color = TextGray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        IconButton(onClick = { viewModel.deleteBookmark(b.id) }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBookmarksDialog = false }) {
                        Text("বন্ধ করুন", color = NeonCyan)
                    }
                },
                containerColor = DarkNavySurface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // History list Dialog
        if (showHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showHistoryDialog = false },
                title = { Text("ব্রাউজিং ইতিহাস (History)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = {
                    Box(modifier = Modifier.height(280.dp).fillMaxWidth()) {
                        if (history.isEmpty()) {
                            Text("কোনো ইতিহাস নেই", color = TextGray, modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(history) { h ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                currentUrl = h.url
                                                urlInput = h.url
                                                isBrowsingActive = true
                                                showHistoryDialog = false
                                            }
                                            .padding(8.dp)
                                    ) {
                                        Column {
                                            Text(h.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            Text(h.url, color = TextGray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Text("ক্লিয়ার হিস্ট্রি", color = Color.Red)
                        }
                        TextButton(onClick = { showHistoryDialog = false }) {
                            Text("বন্ধ করুন", color = NeonCyan)
                        }
                    }
                },
                containerColor = DarkNavySurface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // VIDEO DOWNLOAD SELECT OPTIONS SHEET (Matching Screenshot 3 Layout perfectly)
        if (showDownloadOptionsSheet) {
            var selectedFormat by remember { mutableStateOf("360P") }
            var selectedType by remember { mutableStateOf("Video") } // Video or Music
            var tempTitle by remember { mutableStateOf(currentTitle.ifEmpty { "social_media_video" }) }

            AlertDialog(
                onDismissRequest = { showDownloadOptionsSheet = false },
                title = {
                    Text(
                        text = "ডাউনলোড অপশনস নির্বাচন করুন",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Thumbnail mockup and Title editing
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp, 52.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(24.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                        .align(Alignment.BottomEnd)
                                ) {
                                    Text("02:05:06", color = Color.White, fontSize = 8.sp)
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                TextField(
                                    value = tempTitle,
                                    onValueChange = { tempTitle = it },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = NeonCyan,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Text("Path: /storage/emulated/0/Download/", color = TextGray, fontSize = 9.sp)
                            }
                        }

                        // Music formats list
                        Text("🎵 Music Formats (Audio)", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        val musicFormats = listOf(
                            "48K (M4A)" to "45.77 MB",
                            "48K (MP3) SLOW" to "45.77 MB",
                            "128K (M4A)" to "121.48 MB",
                            "128K (MP3) SLOW" to "121.48 MB",
                            "256K (MP3) SLOW" to "138.12 MB"
                        )
                        musicFormats.forEach { (format, size) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedFormat = format
                                        selectedType = "Music"
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RadioButton(
                                    selected = (selectedFormat == format && selectedType == "Music"),
                                    onClick = {
                                        selectedFormat = format
                                        selectedType = "Music"
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color.Red, unselectedColor = TextGray)
                                )
                                Text(format, color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text(size, color = TextGray, fontSize = 10.sp)
                            }
                        }

                        // Video formats list
                        Text("📹 Video Formats", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        val videoFormats = listOf(
                            "144P (MP4)" to "159.83 MB",
                            "240P (MP4)" to "217.29 MB",
                            "360P (MP4)" to "560.81 MB",
                            "480P (MP4)" to "454.84 MB",
                            "720P HD (MP4)" to "1.2 GB",
                            "1080P HD (MP4)" to "2.4 GB"
                        )
                        videoFormats.forEach { (format, size) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedFormat = format
                                        selectedType = "Video"
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RadioButton(
                                    selected = (selectedFormat == format && selectedType == "Video"),
                                    onClick = {
                                        selectedFormat = format
                                        selectedType = "Video"
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color.Red, unselectedColor = TextGray)
                                )
                                Text(format, color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text(size, color = TextGray, fontSize = 10.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDownloadOptionsSheet = false
                            val parsedBytes = if (selectedFormat.contains("K")) 45 * 1024 * 1024L else 560 * 1024 * 1024L
                            viewModel.startDownload(
                                context = context,
                                title = tempTitle,
                                url = currentUrl,
                                fileType = selectedType,
                                resolution = selectedFormat,
                                sizeBytes = parsedBytes
                            )
                            Toast.makeText(context, "ডাউনলোড ম্যানেজার এ যুক্ত করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                            Text("DOWNLOAD", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                containerColor = DarkNavySurface,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(20.dp))
            )
        }

        // DOWNLOAD MANAGER PANEL / SCREEN (Matching Screenshot 4)
        if (showDownloadManager) {
            AlertDialog(
                onDismissRequest = { showDownloadManager = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ডাউনলোড ম্যানেজার", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(onClick = { showDownloadManager = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                },
                text = {
                    Box(modifier = Modifier.height(380.dp).fillMaxWidth()) {
                        if (downloads.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.FolderOpen, contentDescription = "Empty", tint = TextGray, modifier = Modifier.size(44.dp))
                                Text("কোনো সচল ডাউনলোড টাস্ক নেই", color = TextGray, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(downloads) { d ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                        border = BorderStroke(1.dp, if (d.status == "COMPLETED") NeonCyan.copy(alpha = 0.3f) else GlassBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = if (d.fileType == "Music") Icons.Default.MusicNote else Icons.Default.VideoLibrary,
                                                        contentDescription = d.fileType,
                                                        tint = if (d.status == "COMPLETED") NeonCyan else Color.Red,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text(
                                                        d.title,
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.width(140.dp)
                                                    )
                                                }
                                                Text(d.status, color = if (d.status == "COMPLETED") NeonCyan else Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }

                                            if (d.status == "DOWNLOADING") {
                                                LinearProgressIndicator(
                                                    progress = d.progress,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    color = Color.Red,
                                                    trackColor = Color.White.copy(alpha = 0.1f)
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val progressPct = (d.progress * 100).toInt()
                                                    Text("$progressPct% ($progressPct KB/s)", color = TextGray, fontSize = 9.sp)
                                                    Text(d.resolutionOrQuality, color = TextGray, fontSize = 9.sp)
                                                }
                                            } else {
                                                // Completed Task options
                                                Text(d.filePath, color = TextGray, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            activeVideoUrlForPlayer = "https://www.w3schools.com/html/mov_bbb.mp4" // mock stream video inside app player
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f)),
                                                        modifier = Modifier.height(28.dp)
                                                    ) {
                                                        Text("প্লে করুন (Internal Player)", color = NeonCyan, fontSize = 10.sp)
                                                    }

                                                    IconButton(onClick = { viewModel.deleteDownload(d.id) }) {
                                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDownloadManager = false }) {
                        Text("বন্ধ করুন", color = NeonCyan)
                    }
                },
                containerColor = DarkNavySurface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(16.dp))
            )
        }

        // INTERNAL BUILT-IN VIDEO PLAYER (Plays streams or videos inside app)
        if (activeVideoUrlForPlayer != null) {
            AlertDialog(
                onDismissRequest = { activeVideoUrlForPlayer = null },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ইন-অ্যাপ প্লেয়ার", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        IconButton(onClick = { activeVideoUrlForPlayer = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        320
                                    )
                                    val uri = Uri.parse(activeVideoUrlForPlayer)
                                    setVideoURI(uri)
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        start()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                        )
                        Text(
                            text = "লাইভ স্ট্রিম এবং ডেকোর্ডেড ফাইল সরাসরি অ্যাপে প্লে হচ্ছে",
                            color = TextGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { activeVideoUrlForPlayer = null }) {
                        Text("প্লেয়ার বন্ধ করুন", color = Color.Red)
                    }
                },
                containerColor = DarkNavySurface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(16.dp))
            )
        }
    }
}
