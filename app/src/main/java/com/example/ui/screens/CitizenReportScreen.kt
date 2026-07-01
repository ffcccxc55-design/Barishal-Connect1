package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CitizenReport
import com.example.data.model.getComments
import com.example.data.model.getReactions
import com.example.data.model.withAddedComment
import com.example.data.model.withToggledReaction
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.GlowButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CitizenReportScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val reports by viewModel.citizenReports.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // User credentials & preferences
    val currentUsername by viewModel.userUsername.collectAsState()
    val currentRealName by viewModel.userName.collectAsState()
    val effectiveUsername = currentUsername.ifEmpty { currentRealName.ifEmpty { "citizen" } }
    val userPhotoUrl by viewModel.userPhotoUrl.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    
    // Followers & Saves Flow
    val followingUsers by viewModel.followingUsers.collectAsState()
    val savedPosts by viewModel.savedPosts.collectAsState()
    
    // Dynamic Settings Flow
    val commName by viewModel.communityName.collectAsState()
    val commLogo by viewModel.communityLogo.collectAsState()
    val postingPerm by viewModel.postingPermission.collectAsState()
    val videoLimit by viewModel.videoUploadSize.collectAsState()
    val imageLimit by viewModel.imageUploadSize.collectAsState()
    val storyDurationValue by viewModel.storyDuration.collectAsState()
    val maxLen by viewModel.maxPostLength.collectAsState()
    val isStoriesEnabled by viewModel.enableStories.collectAsState()
    val isShortVideosEnabled by viewModel.enableShortVideos.collectAsState()
    val isPollsEnabled by viewModel.enablePolls.collectAsState()
    val isVoicePostsEnabled by viewModel.enableVoicePosts.collectAsState()
    val isCreatorVerificationEnabled by viewModel.enableCreatorVerification.collectAsState()
    val allowedEmojisStr by viewModel.allowedEmojis.collectAsState()
    
    val emojiList = remember(allowedEmojisStr) {
        allowedEmojisStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    // Navigation and Feed states
    var activeTab by remember { mutableStateOf("For You") } // "For You", "Following", "Trending", "Videos", "Photos", "Discussions", "Chat", "Challenges"
    val homeTabs = listOf("For You", "Following", "Trending", "Videos", "Photos", "Discussions", "Chat", "Challenges")
    
    // Modals & Sheets
    var showCreatePostForm by remember { mutableStateOf(false) }
    var showCreateStoryForm by remember { mutableStateOf(false) }
    var storiesList by remember {
        mutableStateOf(
            listOf(
                MockStory("sakib_dev", "https://api.dicebear.com/7.x/bottts/svg?seed=sakib", "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=500", false, "আজকের নতুন প্রজেক্ট কোডিং সম্পন্ন! 💻"),
                MockStory("anika_photo", "https://api.dicebear.com/7.x/avataaars/svg?seed=anika", "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=500", false, "কীর্তনখোলা নদীর মনোরম ভোর 🌅"),
                MockStory("tasnim_barishal", "https://api.dicebear.com/7.x/avataaars/svg?seed=tasnim", "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=500", false, "বরিশাল ক্যাডেট কলেজ ক্যাম্পাস ভ্রমণ 🏫"),
                MockStory("chef_kamrul", "https://api.dicebear.com/7.x/pixel-art/svg?seed=chef", "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=500", false, "স্পেশাল বরিশাইল্লা গরুর মাংস ভুনা! 🥩")
            )
        )
    }
    var selectedPostForAnalytics by remember { mutableStateOf<CitizenReport?>(null) }
    var selectedPostForComments by remember { mutableStateOf<CitizenReport?>(null) }
    var zoomPhotoUrl by remember { mutableStateOf<String?>(null) }
    var activeStoryView by remember { mutableStateOf<MockStory?>(null) }
    var activeCreatorProfile by remember { mutableStateOf<MockCreator?>(null) }

    // Filter, Search, Rank Feed
    val searchQuery = remember { mutableStateOf("") }
    val filteredReports = remember(reports, activeTab, followingUsers, savedPosts, searchQuery.value) {
        var base = reports.filter { it.status == "APPROVED" || it.reporterName == effectiveUsername }
        
        // Search filter
        if (searchQuery.value.isNotEmpty()) {
            base = base.filter { 
                it.title.contains(searchQuery.value, ignoreCase = true) || 
                it.description.contains(searchQuery.value, ignoreCase = true) ||
                it.hashtags.contains(searchQuery.value, ignoreCase = true)
            }
        }

        // Tab filters
        when (activeTab) {
            "Following" -> base.filter { followingUsers.contains(it.reporterName) }
            "Trending" -> base.filter { it.isTrending || it.votesCount > 5 || it.viewsCount > 10 }
            "Videos" -> base.filter { it.postType == "Video" }
            "Photos" -> base.filter { it.postType == "Photo" }
            "Discussions" -> base.filter { it.postType == "Question" || it.postType == "Text" }
            else -> base
        }
    }

    // Sort the feed using Engagement Score (freshness + boosts + engagement count)
    val sortedReports = remember(filteredReports) {
        filteredReports.sortedWith(compareByDescending<CitizenReport> {
            var score = it.votesCount * 2 + it.commentsJson.split("|||").filter { c -> c.isNotEmpty() }.size * 3 + it.sharesCount * 4 + it.savesCount * 3
            if (it.isBoosted) score += 100
            if (it.isFeatured) score += 75
            if (it.isPinned) score += 200
            score += (it.reachMultiplier * 10).toInt()
            score
        })
    }

    // Pre-populate some dummy social reports if database is empty on first boot
    LaunchedEffect(reports.size) {
        if (reports.isEmpty()) {
            viewModel.addCommunityPost(
                title = "বরিশাল কীর্তনখোলা নদীর অপূর্ব সূর্যাস্ত",
                description = "আজ বিকেলে কীর্তনখোলা নদীর পাড় থেকে তোলা একটি মনোরম দৃশ্য। বরিশাল সত্যিই প্রাকৃতিক সৌন্দর্যের লীলাভূমি।",
                location = "কীর্তনখোলা ঘাট, বরিশাল",
                category = "Photography",
                postType = "Photo",
                imageUrl = "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=800",
                hashtags = "#Barishal #Kirtankhola #Sunset #Nature",
                district = "বরিশাল",
                upazila = "সদর",
                unionName = "চরকাউয়া",
                status = "APPROVED",
                customReporter = "anika_photo"
            )
            viewModel.addCommunityPost(
                title = "কীর্তনখোলা নৌ ভ্রমণ রিলস",
                description = "স্পীডবোট নিয়ে কীর্তনখোলার বুক চিরে ভ্রমণ। বরিশাল নদীমাতৃক সৌন্দর্যের অনন্য উদাহরণ।",
                location = "বরিশাল নদী বন্দর",
                category = "Travel",
                postType = "Video",
                videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-small-flowering-plants-on-the-riverside-44391-large.mp4",
                hashtags = "#Kirtankhola #TravelBarishal #Vibe",
                district = "বরিশাল",
                upazila = "সদর",
                status = "APPROVED",
                customReporter = "tasnim_barishal"
            )
            viewModel.addCommunityPost(
                title = "কমিউনিটি পোল: বরিশাল লঞ্চের কোন ডেকের কেবিন বেশি আরামদায়ক?",
                description = "আপনারা বরিশাল-ঢাকা রুটে ভ্রমণের জন্য কোন অংশটি পছন্দ করেন? ভোট দিন!",
                location = "বরিশাল পোর্ট",
                category = "Discussion",
                postType = "Poll",
                pollOptions = "১ম তলা সিঙ্গেল কেবিন|||২য় তলা ফ্যামিলি ডেক|||৩য় তলা ভিআইপি কেবিন",
                hashtags = "#LaunchService #Discussion #Poll",
                district = "বরিশাল",
                status = "APPROVED",
                customReporter = "sakib_dev"
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = commLogo, fontSize = 24.sp)
                        Column {
                            Text(text = commName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "কানেক্ট, শেয়ার ও ডিসকভার", color = TextCyan, fontSize = 10.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("reports_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCreatePostForm = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f))
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = "Create Post", tint = NeonCyan)
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Search bar and Settings summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassTextField(
                        value = searchQuery.value,
                        onValueChange = { searchQuery.value = it },
                        label = "",
                        placeholder = "সার্চ করুন... (যেমন: #Sunset, ভ্রমণ, Meme)",
                        modifier = Modifier.weight(1f),
                        testTag = "community_search_input"
                    )
                }

                // 24 Hour Story Row (If enabled)
                if (isStoriesEnabled) {
                    StoryRowSection(
                        stories = storiesList,
                        onStoryClick = { activeStoryView = it },
                        onAddStory = {
                            showCreateStoryForm = true
                        }
                    )
                }

                // Categories / Navigation Tabs Row
                ScrollableTabRow(
                    selectedTabIndex = homeTabs.indexOf(activeTab).coerceAtLeast(0),
                    containerColor = Color.Transparent,
                    contentColor = NeonCyan,
                    edgePadding = 12.dp,
                    divider = {}
                ) {
                    homeTabs.forEach { tab ->
                        Tab(
                            selected = activeTab == tab,
                            onClick = { activeTab = tab },
                            text = { Text(text = tab, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Primary Feed or Alternative Views
                when (activeTab) {
                    "Chat" -> {
                        CommunityChatSection(currentUser = effectiveUsername)
                    }
                    "Challenges" -> {
                        CommunityChallengesSection()
                    }
                    else -> {
                        // Standard Feed List
                        if (sortedReports.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "📭", fontSize = 48.sp)
                                    Text(text = "কোনো পোস্ট খুঁজে পাওয়া যায়নি!", color = TextGray, fontSize = 14.sp)
                                    Text(text = "প্রথম ক্রিয়েটর হয়ে নতুন কিছু পোস্ট করুন!", color = TextCyan, fontSize = 11.sp)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("community_feed_list"),
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(sortedReports) { post ->
                                    FeedCardItem(
                                        post = post,
                                        currentUser = effectiveUsername,
                                        isAdmin = isAdminLoggedIn,
                                        emojiList = emojiList,
                                        isPollEnabled = isPollsEnabled,
                                        isVoiceEnabled = isVoicePostsEnabled,
                                        followingUsers = followingUsers,
                                        savedPosts = savedPosts,
                                        onReact = { emoji -> viewModel.toggleReportReaction(post, emoji) },
                                        onFollowClick = { viewModel.toggleFollowUser(post.reporterName) },
                                        onSaveClick = { viewModel.toggleSavePost(post.id.toString()) },
                                        onCommentClick = { selectedPostForComments = post },
                                        onAnalyticsClick = { selectedPostForAnalytics = post },
                                        onImageClick = { url -> zoomPhotoUrl = url },
                                        onAdminAction = { updated -> viewModel.updateCitizenReport(updated) },
                                        onDeletePost = { viewModel.deleteReport(post.id) },
                                        onCreatorClick = { activeCreatorProfile = getMockCreator(post.reporterName) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- ALL DIALOGS & OVERLAYS ---

            // 1. Create Post Overlay
            if (showCreatePostForm) {
                CreatePostDialog(
                    postingPermission = postingPerm,
                    videoLimit = videoLimit,
                    imageLimit = imageLimit,
                    maxLen = maxLen,
                    onDismiss = { showCreatePostForm = false },
                    onSubmit = { title, desc, cat, pType, img, vid, vUrl, tags, dist, upa, uni, priv, opts ->
                        viewModel.addCommunityPost(
                            title = title,
                            description = desc,
                            location = "$dist $upa",
                            category = cat,
                            postType = pType,
                            imageUrl = img,
                            videoUrl = vid,
                            voiceUrl = vUrl,
                            hashtags = tags,
                            pollOptions = opts,
                            district = dist,
                            upazila = upa,
                            unionName = uni,
                            privacy = priv,
                            status = if (isAdminLoggedIn) "APPROVED" else "PENDING"
                        )
                        showCreatePostForm = false
                        if (isAdminLoggedIn) {
                            Toast.makeText(context, "পোস্ট সরাসরি পাবলিশ করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "ধন্যবাদ! আপনার পোস্টটি অ্যাডমিন অনুমোদনের পর প্রদর্শিত হবে।", Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }

            // 1b. Create Story Overlay
            if (showCreateStoryForm) {
                CreateStoryDialog(
                    onDismiss = { showCreateStoryForm = false },
                    onSubmit = { mediaUrl, isVid, caption ->
                        val newStory = MockStory(
                            username = effectiveUsername,
                            avatarUrl = userPhotoUrl.ifEmpty { "https://api.dicebear.com/7.x/avataaars/svg?seed=$effectiveUsername" },
                            contentUrl = mediaUrl,
                            isVideo = isVid,
                            caption = caption
                        )
                        storiesList = storiesList + newStory
                        showCreateStoryForm = false
                        Toast.makeText(context, "স্টোরি সফলভাবে যুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // 2. Comments Drawer Sheet
            selectedPostForComments?.let { post ->
                CommentsDrawerSheet(
                    post = post,
                    currentUser = effectiveUsername,
                    onDismiss = { selectedPostForComments = null },
                    onAddComment = { text ->
                        viewModel.addReportComment(post, text)
                        // Trigger local update for comments view state
                        selectedPostForComments = reports.find { it.id == post.id }
                    }
                )
            }

            // 3. Post Analytics Sheet
            selectedPostForAnalytics?.let { post ->
                PostAnalyticsSheet(
                    post = post,
                    onDismiss = { selectedPostForAnalytics = null }
                )
            }

            // 4. Image Zoom Overlay
            zoomPhotoUrl?.let { url ->
                FullscreenZoomDialog(url = url, onDismiss = { zoomPhotoUrl = null })
            }

            // 5. Story Viewer Overlay
            activeStoryView?.let { story ->
                StoryViewerDialog(story = story, onDismiss = { activeStoryView = null })
            }

            // 6. Creator Profile Overlay
            activeCreatorProfile?.let { creator ->
                CreatorProfileDialog(
                    creator = creator,
                    isFollowing = followingUsers.contains(creator.username),
                    onFollowToggle = { viewModel.toggleFollowUser(creator.username) },
                    onDismiss = { activeCreatorProfile = null }
                )
            }
        }
    }
}

// ======================== STORY COMPONENTS ========================

data class MockStory(
    val username: String,
    val avatarUrl: String,
    val contentUrl: String,
    val isVideo: Boolean = false,
    val caption: String = ""
)

@Composable
fun StoryRowSection(
    stories: List<MockStory>,
    onStoryClick: (MockStory) -> Unit,
    onAddStory: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onAddStory() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.2f))
                            .border(2.dp, NeonCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Story", tint = NeonCyan, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "আপনার স্টোরি", color = Color.White, fontSize = 10.sp)
                }
            }

            items(stories) { story ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onStoryClick(story) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(DarkNavySurfaceCard)
                            .border(
                                BorderStroke(2.5.dp, Brush.linearGradient(listOf(NeonCyan, NeonTeal, Color.Magenta))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = story.avatarUrl,
                            contentDescription = story.username,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "@${story.username}", color = TextGray, fontSize = 10.sp)
                }
            }
        }
    }
}

// ======================== FEED CARD ITEM ========================

@Composable
fun FeedCardItem(
    post: CitizenReport,
    currentUser: String,
    isAdmin: Boolean,
    emojiList: List<String>,
    isPollEnabled: Boolean,
    isVoiceEnabled: Boolean,
    followingUsers: Set<String>,
    savedPosts: Set<String>,
    onReact: (String) -> Unit,
    onFollowClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCommentClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onImageClick: (String) -> Unit,
    onAdminAction: (CitizenReport) -> Unit,
    onDeletePost: () -> Unit,
    onCreatorClick: () -> Unit
) {
    val comments = post.getComments()
    val reactions = post.getReactions()
    val context = LocalContext.current
    val hasLiked = reactions.any { it.username == currentUser }

    var localSelectedVoteOption by remember { mutableStateOf<Int?>(null) }

    // Incremented views simulation
    LaunchedEffect(post.id) {
        if (post.viewsCount == 0) {
            onAdminAction(post.copy(viewsCount = (15..250).random(), reachCount = (30..400).random()))
        }
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderWidth = if (post.isPinned) 2.dp else 1.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header: User Info, Follow and Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = "https://api.dicebear.com/7.x/bottts/svg?seed=${post.reporterName}",
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.1f))
                        .clickable { onCreatorClick() }
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "@${post.reporterName}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable { onCreatorClick() }
                        )
                        // Verified badge mockup
                        if (post.reporterName == "sakib_dev" || post.reporterName == "anika_photo") {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Verified", tint = NeonCyan, modifier = Modifier.size(13.dp))
                        }
                        
                        // Category Chip
                        Text(
                            text = post.category,
                            color = NeonTeal,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonTeal.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(post.timestamp)),
                        color = TextGray,
                        fontSize = 10.sp
                    )
                }

                // Follow button
                if (post.reporterName != currentUser) {
                    TextButton(
                        onClick = onFollowClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = if (followingUsers.contains(post.reporterName)) TextGray else NeonCyan)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(
                                imageVector = if (followingUsers.contains(post.reporterName)) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                                contentDescription = "Follow",
                                modifier = Modifier.size(14.dp)
                            )
                            Text(text = if (followingUsers.contains(post.reporterName)) "ফলোয়িং" else "ফলো", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Bookmark/Save
                IconButton(onClick = onSaveClick) {
                    Icon(
                        imageVector = if (savedPosts.contains(post.id.toString())) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (savedPosts.contains(post.id.toString())) NeonCyan else TextGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Post Pins, Boosted signs
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (post.isPinned) {
                    Text("📌 পিন করা পোস্ট", color = NeonTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                if (post.isBoosted) {
                    Text("🚀 বুস্টেড", color = Color.Magenta, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                if (post.isFeatured) {
                    Text("⭐ ফিচারড", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                if (post.isTrending) {
                    Text("🔥 ট্রেন্ডিং", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Content Title
            Text(text = post.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

            // Content Description/Caption
            Text(text = post.description, color = TextWhite, fontSize = 12.sp, lineHeight = 18.sp)

            // Hashtags view
            if (post.hashtags.isNotEmpty()) {
                Text(text = post.hashtags, color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            // Custom Layouts depending on PostType
            when (post.postType) {
                "Photo" -> {
                    if (post.imageUrl.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onImageClick(post.imageUrl) }
                        ) {
                            AsyncImage(
                                model = post.imageUrl,
                                contentDescription = "Post Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom", tint = Color.White, modifier = Modifier.size(10.dp))
                                    Text("জুম করুন", color = Color.White, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
                "Video" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.PlayCircleFilled, contentDescription = "Play Video", tint = NeonCyan, modifier = Modifier.size(54.dp))
                        Text(
                            text = "ভিডিও রিল প্লে করতে ট্যাপ করুন", 
                            color = Color.White.copy(alpha = 0.7f), 
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 80.dp)
                        )
                    }
                }
                "Voice" -> {
                    if (isVoiceEnabled) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkNavySurface)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = { Toast.makeText(context, "অডিও পডকাস্ট বাজানো হচ্ছে...", Toast.LENGTH_SHORT).show() },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(NeonCyan)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play Voice", tint = DarkNavyBackground)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "ভয়েস পডকাস্ট বার্তা", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                LinearProgressIndicator(progress = 0.35f, color = NeonCyan, trackColor = GlassBorder, modifier = Modifier.fillMaxWidth())
                            }
                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Volume", tint = NeonCyan, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                "Poll" -> {
                    if (isPollEnabled && post.pollOptions.isNotEmpty()) {
                        val options = post.pollOptions.split("|||")
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            options.forEachIndexed { idx, opt ->
                                val isVoted = localSelectedVoteOption == idx
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isVoted) NeonCyan.copy(alpha = 0.2f) else DarkNavySurface)
                                        .border(1.dp, if (isVoted) NeonCyan else GlassBorder, RoundedCornerShape(6.dp))
                                        .clickable {
                                            localSelectedVoteOption = idx
                                            Toast.makeText(context, "ভোট সফলভাবে গণনা করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(10.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(text = opt, color = if (isVoted) NeonCyan else Color.White, fontSize = 11.sp)
                                        Text(text = if (isVoted) "৭২%" else "${(15..45).random()}%", color = TextGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Location details
            if (post.location.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Location", tint = NeonTeal, modifier = Modifier.size(12.dp))
                    Text(text = post.location, color = TextGray, fontSize = 10.sp)
                }
            }

            Divider(color = GlassBorder, thickness = 0.8.dp)

            // React/Engagement buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Love/Like Reaction Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onReact("❤️") }) {
                        Icon(
                            imageVector = if (hasLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "React",
                            tint = if (hasLiked) Color.Red else TextGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "${post.votesCount + reactions.size}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Small emojis overlay shortcut
                    Row(
                        modifier = Modifier.padding(start = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        emojiList.take(3).forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(DarkNavySurface)
                                    .clickable { onReact(emoji) }
                                    .padding(2.dp)
                            )
                        }
                    }
                }

                // Comment trigger
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCommentClick() }
                ) {
                    Icon(imageVector = Icons.Default.Comment, contentDescription = "Comment", tint = TextGray, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${comments.size} কমেন্ট", color = TextGray, fontSize = 11.sp)
                }

                // Analytics View Icon
                IconButton(onClick = onAnalyticsClick) {
                    Icon(imageVector = Icons.Default.Equalizer, contentDescription = "Analytics", tint = NeonCyan, modifier = Modifier.size(18.dp))
                }

                // Share button
                IconButton(onClick = {
                    if (post.isSharingDisabled) {
                        Toast.makeText(context, "এই পোস্টের জন্য শেয়ারিং বন্ধ রয়েছে!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "শেয়ার লিংক ক্লিপবোর্ডে কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        onAdminAction(post.copy(sharesCount = post.sharesCount + 1))
                    }
                }) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = TextGray, modifier = Modifier.size(18.dp))
                }
            }

            // --- ADMIN CONTROLS IN FEED ---
            if (isAdmin) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🛡️ অ্যাডমিন ফিড কন্ট্রোল (Admin Control)", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Boost Post Toggle
                        TextButton(
                            onClick = { onAdminAction(post.copy(isBoosted = !post.isBoosted)) },
                            colors = ButtonDefaults.textButtonColors(contentColor = if (post.isBoosted) Color.Magenta else Color.White)
                        ) {
                            Text(text = if (post.isBoosted) "🚀 বুস্ট অফ" else "🚀 বুস্ট দিন", fontSize = 10.sp)
                        }

                        // Feature Post Toggle
                        TextButton(
                            onClick = { onAdminAction(post.copy(isFeatured = !post.isFeatured)) },
                            colors = ButtonDefaults.textButtonColors(contentColor = if (post.isFeatured) NeonCyan else Color.White)
                        ) {
                            Text(text = if (post.isFeatured) "⭐ আন-ফিচার" else "⭐ ফিচার করুন", fontSize = 10.sp)
                        }

                        // Pin Top Toggle
                        TextButton(
                            onClick = { onAdminAction(post.copy(isPinned = !post.isPinned)) },
                            colors = ButtonDefaults.textButtonColors(contentColor = if (post.isPinned) NeonTeal else Color.White)
                        ) {
                            Text(text = if (post.isPinned) "📌 আন-পিন" else "📌 পিন টপ", fontSize = 10.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Increase Reach
                        Button(
                            onClick = { onAdminAction(post.copy(reachMultiplier = post.reachMultiplier + 1.5f, isTrending = true)) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal.copy(alpha = 0.2f), contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("⬆ রিচ বাড়ান", fontSize = 9.sp)
                        }

                        // Lock Discussion
                        Button(
                            onClick = { onAdminAction(post.copy(isDiscussionLocked = !post.isDiscussionLocked)) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f), contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (post.isDiscussionLocked) "🔓 আনলক চ্যাট" else "🔒 আলোচনা লক", fontSize = 9.sp)
                        }

                        // Delete Post directly
                        Button(
                            onClick = onDeletePost,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🗑 ডিলিট", fontSize = 9.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ======================== DIALOGS & DRAWERS ========================

data class GalleryMediaItem(
    val title: String,
    val url: String,
    val isVideo: Boolean
)

val mockGalleryPhotos = listOf(
    GalleryMediaItem("কীর্তনখোলা নদীর ভোর 🌅", "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=500", false),
    GalleryMediaItem("কুয়াকাটা সী-বিচ সূর্যাস্ত 🌊", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500", false),
    GalleryMediaItem("বরিশাল সবুজ ধানক্ষেত 🌾", "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=500", false),
    GalleryMediaItem("নদীর চরে ভাসমান নৌকা ⛵", "https://images.unsplash.com/photo-1516690561799-46d8f74f9abf?w=500", false),
    GalleryMediaItem("বরিশালের স্পেশাল আমড়া ও ডাব 🥥", "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=500", false),
    GalleryMediaItem("ঐতিহাসিক গুটিয়া মসজিদ 🕌", "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=500", false)
)

val mockGalleryVideos = listOf(
    GalleryMediaItem("কীর্তনখোলা ড্রোন শট 🚁", "https://assets.mixkit.co/videos/preview/mixkit-beautiful-aerial-view-of-a-winding-river-in-the-forest-41618-large.mp4", true),
    GalleryMediaItem("কুয়াকাটা সমুদ্রের ঢেউ 🌊", "https://assets.mixkit.co/videos/preview/mixkit-aerial-view-of-ocean-waves-crashing-on-sandy-beach-42930-large.mp4", true),
    GalleryMediaItem("সবুজ পাতায় বৃষ্টির জল 🌧️", "https://assets.mixkit.co/videos/preview/mixkit-rain-drops-falling-on-puddle-on-the-pavement-41584-large.mp4", true),
    GalleryMediaItem("শান্ত গ্রামের বাঁশঝাড় 🎋", "https://assets.mixkit.co/videos/preview/mixkit-wind-blowing-through-green-leaves-of-a-tree-41624-large.mp4", true)
)

@Composable
fun GalleryMediaPickerDialog(
    isVideo: Boolean,
    onDismiss: () -> Unit,
    onMediaSelected: (GalleryMediaItem) -> Unit
) {
    val items = if (isVideo) mockGalleryVideos else mockGalleryPhotos
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isVideo) "🎥 গ্যালারি থেকে ভিডিও নির্বাচন করুন" else "📸 গ্যালারি থেকে ফটো নির্বাচন করুন",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "আপনার ফোনের গ্যালারিতে থাকা ফাইলগুলো নিচে প্রদর্শিত হচ্ছে:", color = TextGray, fontSize = 11.sp)
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(260.dp)
                ) {
                    items(items) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clickable {
                                    onMediaSelected(item)
                                    onDismiss()
                                },
                            colors = CardDefaults.cardColors(containerColor = DarkNavyBackground),
                            border = BorderStroke(1.dp, GlassBorder)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = if (item.isVideo) "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=200" else item.url,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = item.title,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                
                                if (item.isVideo) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.Center)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("ফিরে যান", color = Color.White) }
        },
        containerColor = DarkNavySurfaceCard
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostDialog(
    postingPermission: String,
    videoLimit: String,
    imageLimit: String,
    maxLen: String,
    onDismiss: () -> Unit,
    onSubmit: (title: String, desc: String, category: String, postType: String, img: String, vid: String, voice: String, tags: String, dist: String, upa: String, uni: String, privacy: String, pollOpts: String) -> Unit
) {
    var pDesc by remember { mutableStateOf("") }
    var pCat by remember { mutableStateOf("Photography") }
    var selectedPhotoItem by remember { mutableStateOf<GalleryMediaItem?>(null) }
    var selectedVideoItem by remember { mutableStateOf<GalleryMediaItem?>(null) }
    
    var showPhotoPicker by remember { mutableStateOf(false) }
    var showVideoPicker by remember { mutableStateOf(false) }
    var showAdvancedSettings by remember { mutableStateOf(false) }

    // Advanced Option States
    var pTitle by remember { mutableStateOf("") }
    var pTags by remember { mutableStateOf("") }
    var pPollOptions by remember { mutableStateOf("") }
    var pDist by remember { mutableStateOf("বরিশাল") }
    var pUpa by remember { mutableStateOf("সদর") }
    var pPrivacy by remember { mutableStateOf("Public") }

    val categories = listOf("Photography", "Entertainment", "Meme", "Travel", "Sports", "Education", "Health", "Business")

    if (showPhotoPicker) {
        GalleryMediaPickerDialog(
            isVideo = false,
            onDismiss = { showPhotoPicker = false },
            onMediaSelected = { selectedPhotoItem = it }
        )
    }

    if (showVideoPicker) {
        GalleryMediaPickerDialog(
            isVideo = true,
            onDismiss = { showVideoPicker = false },
            onMediaSelected = { selectedVideoItem = it }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "✍️ নতুন সামাজিক পোস্ট লিখুন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Post Category select (simple chip group)
                Text(text = "ক্যাটাগরি নির্বাচন করুন:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                ScrollableTabRow(
                    selectedTabIndex = categories.indexOf(pCat).coerceAtLeast(0),
                    containerColor = Color.Transparent,
                    contentColor = NeonTeal,
                    divider = {}
                ) {
                    categories.forEach { c ->
                        Tab(selected = pCat == c, onClick = { pCat = c }) {
                            Text(text = c, color = if (pCat == c) NeonTeal else Color.White, fontSize = 11.sp, modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp))
                        }
                    }
                }

                // Simplified Caption Input
                GlassTextField(
                    value = pDesc,
                    onValueChange = { if (it.length <= (maxLen.toIntOrNull() ?: 1000)) pDesc = it },
                    label = "ক্যাপশন বা আপনার মনের ভাব (Caption)",
                    placeholder = "আপনার মনের ভাব প্রকাশ করুন...",
                    testTag = "create_post_desc"
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Beautiful direct buttons for Gallery Selector
                Text(text = "গ্যালারি থেকে ফটো/ভিডিও যুক্ত করুন:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    // Add Photo button
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showPhotoPicker = true },
                        colors = CardDefaults.cardColors(containerColor = if (selectedPhotoItem != null) NeonCyan.copy(alpha = 0.15f) else DarkNavyBackground),
                        border = BorderStroke(1.dp, if (selectedPhotoItem != null) NeonCyan else GlassBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Add Photo", tint = if (selectedPhotoItem != null) NeonCyan else Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (selectedPhotoItem != null) "📸 ফটো যুক্ত হয়েছে" else "📸 ফটো নির্বাচন",
                                color = if (selectedPhotoItem != null) NeonCyan else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Add Video button
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showVideoPicker = true },
                        colors = CardDefaults.cardColors(containerColor = if (selectedVideoItem != null) NeonTeal.copy(alpha = 0.15f) else DarkNavyBackground),
                        border = BorderStroke(1.dp, if (selectedVideoItem != null) NeonTeal else GlassBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.VideoCameraBack, contentDescription = "Add Video", tint = if (selectedVideoItem != null) NeonTeal else Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (selectedVideoItem != null) "🎥 ভিডিও যুক্ত হয়েছে" else "🎥 ভিডিও নির্বাচন",
                                color = if (selectedVideoItem != null) NeonTeal else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Selected Media Preview (if any)
                if (selectedPhotoItem != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkNavyBackground),
                        border = BorderStroke(1.dp, NeonCyan)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = selectedPhotoItem!!.url,
                                contentDescription = "Selected Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = "ফটো: ${selectedPhotoItem!!.title}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            IconButton(
                                onClick = { selectedPhotoItem = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "remove", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                if (selectedVideoItem != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkNavyBackground),
                        border = BorderStroke(1.dp, NeonTeal)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=200",
                                contentDescription = "Selected Video",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(36.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = NeonTeal, modifier = Modifier.size(20.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = "ভিডিও: ${selectedVideoItem!!.title}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            IconButton(
                                onClick = { selectedVideoItem = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "remove", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                // Advanced Options Expandable
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkNavyBackground.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAdvancedSettings = !showAdvancedSettings }
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⚙️ অতিরিক্ত অপশন (Advanced Options)", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Icon(
                                imageVector = if (showAdvancedSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle",
                                tint = TextGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (showAdvancedSettings) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                                GlassTextField(value = pTitle, onValueChange = { pTitle = it }, label = "পোস্ট শিরোনাম (Title) - ঐচ্ছিক", testTag = "create_post_title")
                                GlassTextField(value = pTags, onValueChange = { pTags = it }, label = "হ্যাশট্যাগসমূহ (Hashtags)", placeholder = "#Barishal #Vibe", testTag = "create_post_tags")
                                GlassTextField(value = pPollOptions, onValueChange = { pPollOptions = it }, label = "পোল অপশন (কমা দিয়ে আলাদা করুন)", placeholder = "অপশন ১, অপশন ২", testTag = "create_post_poll")
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        GlassTextField(value = pDist, onValueChange = { pDist = it }, label = "জেলা (District)")
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        GlassTextField(value = pUpa, onValueChange = { pUpa = it }, label = "উপজেলা (Upazila)")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pDesc.isEmpty()) {
                        return@Button
                    }
                    val finalTitle = pTitle.ifEmpty {
                        if (pDesc.length > 30) pDesc.take(30) + "..." else pDesc
                    }
                    val finalPostType = when {
                        selectedVideoItem != null -> "Video"
                        selectedPhotoItem != null -> "Photo"
                        pPollOptions.isNotEmpty() -> "Poll"
                        else -> "Text"
                    }
                    onSubmit(
                        finalTitle,
                        pDesc,
                        pCat,
                        finalPostType,
                        selectedPhotoItem?.url ?: "",
                        selectedVideoItem?.url ?: "",
                        "", // VoiceUrl
                        pTags,
                        pDist,
                        pUpa,
                        "", // Union
                        pPrivacy,
                        pPollOptions
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("পাবলিশ করুন", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল", color = Color.White) }
        },
        containerColor = DarkNavySurfaceCard
    )
}

@Composable
fun CreateStoryDialog(
    onDismiss: () -> Unit,
    onSubmit: (mediaUrl: String, isVideo: Boolean, caption: String) -> Unit
) {
    var captionText by remember { mutableStateOf("") }
    var selectedMedia by remember { mutableStateOf<GalleryMediaItem?>(null) }
    var showPhotoPicker by remember { mutableStateOf(false) }

    if (showPhotoPicker) {
        GalleryMediaPickerDialog(
            isVideo = false,
            onDismiss = { showPhotoPicker = false },
            onMediaSelected = { selectedMedia = it }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "📸 নতুন ২৪-ঘণ্টার স্টোরি দিন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Text(text = "সোশ্যাল মিডিয়া ফিডে আপনার বন্ধুদের সাথে স্টোরি শেয়ার করুন:", color = TextGray, fontSize = 11.sp)

                // Select photo button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPhotoPicker = true },
                    colors = CardDefaults.cardColors(containerColor = if (selectedMedia != null) NeonCyan.copy(alpha = 0.15f) else DarkNavyBackground),
                    border = BorderStroke(1.dp, if (selectedMedia != null) NeonCyan else GlassBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = "Add Photo", tint = if (selectedMedia != null) NeonCyan else Color.White, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedMedia != null) "📸 ফটো সিলেক্ট করা হয়েছে" else "📸 গ্যালারি থেকে ফটো নির্বাচন করুন",
                            color = if (selectedMedia != null) NeonCyan else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (selectedMedia != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        border = BorderStroke(1.dp, NeonCyan)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = selectedMedia!!.url,
                                contentDescription = "Selected Story Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                GlassTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    label = "স্টোরি ক্যাপশন (ঐচ্ছিক)",
                    placeholder = "দারুণ সকাল বা অনুভূতি..."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedMedia == null) return@Button
                    onSubmit(selectedMedia!!.url, selectedMedia!!.isVideo, captionText)
                },
                enabled = selectedMedia != null,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("শেয়ার করুন", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বাতিল", color = Color.White) }
        },
        containerColor = DarkNavySurfaceCard
    )
}

@Composable
fun CommentsDrawerSheet(
    post: CitizenReport,
    currentUser: String,
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    val comments = post.getComments()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .clickable(enabled = false, onClick = {}),
            colors = CardDefaults.cardColors(containerColor = DarkNavyBackground),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "মন্তব্যসমূহ (${comments.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                if (post.isCommentsDisabled) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("🔒 এই পোস্টের জন্য অ্যাডমিন মন্তব্য করা বন্ধ রেখেছেন!", color = Color.Red, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                } else {
                    // Comments list
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (comments.isEmpty()) {
                            item {
                                Text(text = "কোনো মন্তব্য নেই। প্রথম মন্তব্যটি আপনি করুন!", color = TextGray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(20.dp))
                            }
                        } else {
                            items(comments) { comment ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    AsyncImage(
                                        model = comment.avatarUrl.ifEmpty { "https://api.dicebear.com/7.x/bottts/svg?seed=${comment.authorName}" },
                                        contentDescription = "Avatar",
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(GlassBorder)
                                    )
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DarkNavySurfaceCard)
                                            .padding(8.dp)
                                    ) {
                                        Text(text = "@${comment.authorName}", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text(text = comment.text, color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Add comment row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            label = "",
                            placeholder = "একটি মন্তব্য লিখুন...",
                            modifier = Modifier.weight(1f),
                            testTag = "add_comment_input"
                        )
                        IconButton(
                            onClick = {
                                if (commentText.trim().isNotEmpty()) {
                                    onAddComment(commentText.trim())
                                    commentText = ""
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(NeonTeal)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = DarkNavyBackground)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostAnalyticsSheet(
    post: CitizenReport,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.Equalizer, contentDescription = "Analytics", tint = NeonCyan)
                Text(text = "পোস্ট এনালিটিক্স ও রিচ ডাটা", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Text(text = "পোস্ট আইডি: BC-CH-${post.id}", color = TextGray, fontSize = 10.sp)
                
                DetailValueRow(label = "👁️ সর্বমোট ভিউ (Views):", value = "${post.viewsCount} বার")
                DetailValueRow(label = "📈 মোট রিচ (Reach):", value = "${post.reachCount} জন")
                DetailValueRow(label = "❤️ রিঅ্যাকশন সংখ্যা:", value = "${post.votesCount}")
                DetailValueRow(label = "💬 মোট কমেন্ট সংখ্যা:", value = "${post.getComments().size}টি")
                DetailValueRow(label = "🔗 শেয়ার সংখ্যা (Shares):", value = "${post.sharesCount}টি")
                DetailValueRow(label = "💾 সংরক্ষিত হয়েছে (Saves):", value = "${post.savesCount} বার")
                DetailValueRow(label = "⏱️ এভারেজ ওয়াচ টাইম:", value = "${(15..95).random()} সেকেন্ড")
                DetailValueRow(label = "📊 এঙ্গেজমেন্ট রেট (Engagement):", value = "${String.format("%.2f", (post.votesCount + post.sharesCount + post.savesCount).toFloat() / post.viewsCount.coerceAtLeast(1) * 100)}%")
                
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "📍 বরিশাল লোকাল এরিয়া রিচ: বরিশাল জেলার ${post.location.ifEmpty { "সকল" }} এলাকা থেকে ${post.viewsCount / 2} জন মানুষ এই পোস্টটি স্ক্রোল করেছেন।", color = NeonTeal, fontSize = 11.sp, lineHeight = 16.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)) {
                Text(text = "ঠিক আছে", color = DarkNavyBackground)
            }
        },
        containerColor = DarkNavySurfaceCard
    )
}

@Composable
fun DetailValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextGray, fontSize = 12.sp)
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun FullscreenZoomDialog(
    url: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = url,
            contentDescription = "Zoomed Photo",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Fit
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
    }
}

@Composable
fun StoryViewerDialog(
    story: MockStory,
    onDismiss: () -> Unit
) {
    var progress by remember { mutableStateOf(0.0f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (progress < 1.0f) {
            delay(50)
            progress += 0.01f
        }
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = story.contentUrl,
            contentDescription = "Story Content",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Progress Bar
                LinearProgressIndicator(
                    progress = progress,
                    color = NeonCyan,
                    trackColor = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth().height(4.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AsyncImage(
                        model = story.avatarUrl,
                        contentDescription = story.username,
                        modifier = Modifier.size(36.dp).clip(CircleShape)
                    )
                    Text(text = "@${story.username}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }

            // Caption
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = story.caption, color = Color.White, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "👁️ ${(12..145).random()} Views", color = TextGray, fontSize = 10.sp)
                }
            }
        }
    }
}

// ======================== CREATOR PROFILE OVERLAY ========================

data class MockCreator(
    val username: String,
    val realName: String,
    val avatarUrl: String,
    val coverUrl: String,
    val bio: String,
    val followersCount: Int,
    val followingCount: Int,
    val achievements: List<String>,
    val photosCount: Int,
    val videosCount: Int
)

fun getMockCreator(username: String): MockCreator {
    return when (username) {
        "sakib_dev" -> MockCreator(
            username = "sakib_dev",
            realName = "সাকিব আহমেদ",
            avatarUrl = "https://api.dicebear.com/7.x/bottts/svg?seed=sakib",
            coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500",
            bio = "বরিশাল কানেক্ট এবং কমিউনিটি হাবের কো-ফাউন্ডার ও সফটওয়্যার ডেভেলপার। বরিশালকে স্মার্ট বানাতে অঙ্গীকারবদ্ধ।",
            followersCount = 1420,
            followingCount = 110,
            achievements = listOf("🏆 টপ ক্রিয়েটর", "🤝 টেক্স কনট্রিবিউটর", "⚡ ভেরিফাইড ব্যাজ"),
            photosCount = 12,
            videosCount = 3
        )
        "anika_photo" -> MockCreator(
            username = "anika_photo",
            realName = "আনিকা রহমান",
            avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=anika",
            coverUrl = "https://images.unsplash.com/photo-1454496522488-7a8e488e8606?w=500",
            bio = "প্রকৃতিপ্রেমী এবং ফ্রিল্যান্স ফটোগ্রাফার। বরিশাল নদী ও গ্রামীণ জীবন ক্যামেরায় বন্দি করতে ভালোবাসি।📸",
            followersCount = 2850,
            followingCount = 340,
            achievements = listOf("📷 সেরা ফটোগ্রাফার", "🍁 প্রকৃতি মিত্র", "⭐ গোল্ডেন ক্রিয়েটর"),
            photosCount = 54,
            videosCount = 8
        )
        else -> MockCreator(
            username = username,
            realName = "কমিউনিটি মেম্বার",
            avatarUrl = "https://api.dicebear.com/7.x/bottts/svg?seed=$username",
            coverUrl = "https://images.unsplash.com/photo-1557683316-973673baf926?w=500",
            bio = "আমি বরিশাল কানেক্ট সোশ্যাল ক্লাবের একজন গর্বিত সক্রিয় সদস্য। বরিশালকে জানুন ও জানান!",
            followersCount = 125,
            followingCount = 45,
            achievements = listOf("👍 উদীয়মান কন্ট্রিকিউটর"),
            photosCount = 3,
            videosCount = 1
        )
    }
}

@Composable
fun CreatorProfileDialog(
    creator: MockCreator,
    isFollowing: Boolean,
    onFollowToggle: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cover photo mockup
                AsyncImage(
                    model = creator.coverUrl,
                    contentDescription = "Cover",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AsyncImage(
                        model = creator.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(GlassBorder)
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = creator.realName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Verified Badge", tint = NeonCyan, modifier = Modifier.size(14.dp))
                        }
                        Text(text = "@${creator.username}", color = TextGray, fontSize = 11.sp)
                    }
                }

                Text(text = creator.bio, color = TextWhite, fontSize = 12.sp, lineHeight = 18.sp)

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column {
                        Text(text = "${creator.followersCount}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "ফলোয়ারস", color = TextGray, fontSize = 10.sp)
                    }
                    Column {
                        Text(text = "${creator.followingCount}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "ফলোয়িং", color = TextGray, fontSize = 10.sp)
                    }
                    Column {
                        Text(text = "${creator.photosCount}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "ছবিসমূহ", color = TextGray, fontSize = 10.sp)
                    }
                }

                Divider(color = GlassBorder)

                // Achievements / Badges Section
                Text(text = "সম্মাননা ও মেডেল (Achievements):", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    creator.achievements.forEach { achievement ->
                        Text(
                            text = achievement,
                            color = DarkNavyBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonCyan)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onFollowToggle, colors = ButtonDefaults.buttonColors(containerColor = if (isFollowing) TextGray else NeonCyan)) {
                Text(text = if (isFollowing) "আনফলো" else "ফলো করুন", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বন্ধ করুন", color = Color.White) }
        },
        containerColor = DarkNavySurfaceCard
    )
}

// ======================== COMMUNITY CHAT SECTION ========================

data class ChatMessage(
    val sender: String,
    val text: String,
    val timestamp: String,
    val isMe: Boolean
)

@Composable
fun CommunityChatSection(currentUser: String) {
    val messages = remember {
        mutableStateListOf(
            ChatMessage("sakib_dev", "হ্যালো বন্ধুরা! বরিশাল কানেক্ট কমিউনিটি চ্যাটরুমে আপনাদের স্বাগতম।", "১০:১৫ AM", false),
            ChatMessage("anika_photo", "সবাই কেমন আছেন? আগামী শুক্রবারে কীর্তনখোলা ঘাটে একটি ফটোওয়াক আয়োজন করলে কেমন হয়?", "১০:১৮ AM", false)
        )
    }

    var textInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = "💬 বরিশাল সোশ্যাল লাইভ চ্যাটরুম (Community Chat)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = "এখানে বরিশালের সক্রিয় ক্রিয়েটর এবং বন্ধুরা রিয়েল-টাইমে একে অপরের সাথে কথা বলে।", color = TextGray, fontSize = 11.sp)

        // Message board
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val alignment = if (msg.isMe) Alignment.End else Alignment.Start
                val cardColor = if (msg.isMe) NeonCyan.copy(alpha = 0.2f) else DarkNavySurfaceCard
                val borderStroke = if (msg.isMe) BorderStroke(1.dp, NeonCyan) else BorderStroke(1.dp, GlassBorder)
                
                Column(horizontalAlignment = alignment, modifier = Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier.widthIn(max = 260.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        border = borderStroke
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (!msg.isMe) {
                                Text(text = "@${msg.sender}", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            Text(text = msg.text, color = Color.White, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = msg.timestamp, 
                                color = TextGray, 
                                fontSize = 8.sp, 
                                textAlign = if (msg.isMe) TextAlign.End else TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Fast Quick Reply presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val presets = listOf("অবশ্যই যাব!", "দারুণ আইডিয়া! 👍", "আমি রাজি! 🤝")
            presets.forEach { preset ->
                Text(
                    text = preset,
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonCyan.copy(alpha = 0.12f))
                        .clickable {
                            messages.add(ChatMessage(currentUser, preset, "এখনই", true))
                            // Simple mock autoreply
                            coroutineScope.launch {
                                delay(1200)
                                messages.add(ChatMessage("bot_connect", "চ্যাট মেম্বার @$currentUser রেসপন্স করেছেন: $preset", "১০:১৯ AM", false))
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // Text Box Send Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = "",
                placeholder = "একটি চ্যাট মেসেজ লিখুন...",
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    if (textInput.trim().isNotEmpty()) {
                        messages.add(ChatMessage(currentUser, textInput.trim(), "এখনই", true))
                        val textSaved = textInput.trim()
                        textInput = ""
                        coroutineScope.launch {
                            delay(1200)
                            // Smart Auto responses
                            val reply = when {
                                textSaved.contains("হ্যালো", true) || textSaved.contains("হাই", true) -> "হ্যালো @$currentUser! কেমন আছেন?"
                                textSaved.contains("যাব", true) || textSaved.contains("শুক্র", true) -> "দারুণ! ফটোওয়াকে কীর্তনখোলার সুন্দর স্মৃতি ধারণ করব সবাই।"
                                else -> "খুবই চমৎকার ভাবনা! বরিশাল কানেক্ট এগিয়ে যাচ্ছে।"
                            }
                            messages.add(ChatMessage("anika_photo", reply, "এখনই", false))
                        }
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(NeonCyan)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = DarkNavyBackground)
            }
        }
    }
}

// ======================== COMMUNITY CHALLENGES SECTION ========================

@Composable
fun CommunityChallengesSection() {
    val challenges = listOf(
        "Weekly Photo Challenge" to "কীর্তনখোলার মনোরম নৌ-ভ্রমণের দৃশ্য শেয়ার করুন। সেরা ৩ জন পাবেন গোল্ডেন ফটোগ্রাফার মেডেল!",
        "Weekly Food Challenge" to "বরিশালের ঐতিহ্যবাহী রসাল আমড়া ও ডাবের সেরা রিভিউ করুন।",
        "Weekly Travel Challenge" to "বরিশাল বা ভোলা জেলার সুপ্রাচীন জমিদার বাড়ির ইতিহাস ও ভ্রমণকাহিনী লিখুন।"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = "🏆 ক্রিয়েটর চ্যালেঞ্জ ও লিডারবোর্ড (Challenges)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = "সাপ্তাহিক আকর্ষণীয় চ্যালেঞ্জগুলোতে অংশ নিন, মেডেল জিতুন এবং টপ ক্রিয়েটর লিডারবোর্ডের শীর্ষে অবস্থান করুন!", color = TextGray, fontSize = 11.sp)

        challenges.forEach { (title, desc) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Trophy", tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Text(text = title, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Text(text = desc, color = TextWhite, fontSize = 11.sp, lineHeight = 16.sp)
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = "অংশ নিন", color = DarkNavyBackground, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Leaderboard
        Text(text = "👑 সেরা কন্ট্রিবিউটর লিডারবোর্ড (Leaderboard)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        
        val leaderboard = listOf(
            Triple("anika_photo", "২৮৫০ পয়েন্ট", "🥇 সেরা ক্রিয়েটর"),
            Triple("sakib_dev", "২৪২০ পয়েন্ট", "🥈 কোড কন্ট্রিবিউটর"),
            Triple("tasnim_barishal", "১৮১০ পয়েন্ট", "🥉 ট্রাভেলার")
        )

        leaderboard.forEach { (user, pts, rank) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AsyncImage(
                        model = "https://api.dicebear.com/7.x/bottts/svg?seed=$user",
                        contentDescription = "Leaderboard Avatar",
                        modifier = Modifier.size(28.dp).clip(CircleShape)
                    )
                    Text(text = "@$user", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = rank, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text(text = pts, color = TextGray, fontSize = 10.sp)
                }
            }
        }
    }
}

// ======================== GOOGLE SHEETS SETUP INSTRUCTION ========================

@Composable
fun GoogleSheetsInstructionSection(
    apiUrl: String,
    onSaveUrl: (String) -> Unit,
    onBackup: () -> Unit
) {
    val context = LocalContext.current
    var editUrl by remember { mutableStateOf(apiUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = "🗄️ গুগল শিট ক্লাউড কানেকশন গাইড ও ব্যাকআপ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = "অ্যাপের সমস্ত মেটাডাটা, সেটিংস, স্টোরি এবং পোস্ট সরাসরি গুগল স্প্রেডশিটে ব্যাকআপ বা সিঙ্ক করার জন্য নিচের এপিআই কানেক্ট করুন।", color = TextGray, fontSize = 11.sp, lineHeight = 16.sp)

        // API URL Configuration Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "🔗 আপনার গুগল অ্যাপস স্ক্রিপ্ট এপিআই ইউআরএল (API WebApp URL)", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                
                GlassTextField(
                    value = editUrl,
                    onValueChange = { editUrl = it },
                    label = "",
                    placeholder = "https://script.google.com/macros/s/.../exec"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onSaveUrl(editUrl)
                            Toast.makeText(context, "এপিআই লিঙ্ক সফলভাবে সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text(text = "এপিআই লিঙ্ক সেভ", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onBackup,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "ব্যাকআপ শুরু", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Detailed Google Apps Script Setup Code
        Text(text = "🛠️ কিভাবে সেটআপ করবেন (Step-by-Step Instructions):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        
        Text(
            text = "১. প্রথমে আপনার গুগল ড্রাইভ থেকে একটি নতুন Google Sheet খুলুন।\n" +
                    "২. স্প্রেডশিটটিতে ৩টি ট্যাব তৈরি করুন:\n" +
                    "   * Tab 1: 'CommunityPosts' (Columns: id, title, description, reporter, postType, status, likes, comments)\n" +
                    "   * Tab 2: 'Stories' (Columns: username, contentUrl, caption, timestamp)\n" +
                    "   * Tab 3: 'Settings' (Columns: key, value)\n" +
                    "৩. গুগল শিটের 'Extensions' -> 'Apps Script' এ ক্লিক করুন এবং নিচের কোডটি পেস্ট করে Deploy Web App করুন:",
            color = TextWhite,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )

        // GAS Source Code display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black)
                .padding(10.dp)
        ) {
            Text(
                text = "function doPost(e) {\n" +
                        "  var data = JSON.parse(e.postData.contents);\n" +
                        "  var ss = SpreadsheetApp.getActiveSpreadsheet();\n" +
                        "  var sheet = ss.getSheetByName('CommunityPosts');\n" +
                        "  sheet.appendRow([\n" +
                        "    data.id, data.title, data.description,\n" +
                        "    data.reporterName, data.postType, data.status\n" +
                        "  ]);\n" +
                        "  return ContentService.createTextOutput(\n" +
                        "    JSON.stringify({status: 'success'})\n" +
                        "  ).setMimeType(ContentService.MimeType.JSON);\n" +
                        "}",
                color = NeonCyan,
                fontSize = 9.sp,
                lineHeight = 12.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}
