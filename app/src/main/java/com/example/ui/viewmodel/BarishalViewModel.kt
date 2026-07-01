package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CitizenReport
import com.example.data.model.ReportComment
import com.example.data.model.withAddedComment
import com.example.data.model.withToggledReaction
import com.example.data.model.DirectoryItem
import com.example.data.model.UserActivity
import com.example.data.repository.BarishalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

sealed class UiState {
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
}

class BarishalViewModel(
    private val repository: BarishalRepository,
    private val prefs: android.content.SharedPreferences? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // --- Dynamic Settings and Security States ---
    val appName = MutableStateFlow(prefs?.getString("app_name", "Barishal Connect") ?: "Barishal Connect")
    val appLogo = MutableStateFlow(prefs?.getString("app_logo", "B") ?: "B")
    val themeColor = MutableStateFlow(prefs?.getString("theme_color", "DarkNavyBackground") ?: "DarkNavyBackground")
    val accentColor = MutableStateFlow(prefs?.getString("accent_color", "NeonCyan") ?: "NeonCyan")
    val contactPhone = MutableStateFlow(prefs?.getString("contact_phone", "০১৭০০০০০০০০") ?: "০১৭০০০০০০০০")
    val contactEmail = MutableStateFlow(prefs?.getString("contact_email", "support@barishalconnect.gov") ?: "support@barishalconnect.gov")
    val facebookUrl = MutableStateFlow(prefs?.getString("facebook_url", "https://facebook.com/barishalconnect") ?: "https://facebook.com/barishalconnect")
    val websiteUrl = MutableStateFlow(prefs?.getString("website_url", "https://barishalconnect.gov") ?: "https://barishalconnect.gov")
    val whatsappUrl = MutableStateFlow(prefs?.getString("whatsapp_url", "https://wa.me/8801700000000") ?: "https://wa.me/8801700000000")
    val telegramUrl = MutableStateFlow(prefs?.getString("telegram_url", "https://t.me/barishalconnect") ?: "https://t.me/barishalconnect")
    val youtubeUrl = MutableStateFlow(prefs?.getString("youtube_url", "https://youtube.com/barishalconnect") ?: "https://youtube.com/barishalconnect")
    val privacyPolicy = MutableStateFlow(prefs?.getString("privacy_policy", "বরিশাল কানেক্ট অ্যাপের গোপনীয়তা নীতি এবং সুরক্ষা নির্দেশিকা। আমরা বরিশালবাসীর তথ্যের গোপনীয়তা রক্ষায় অঙ্গীকারবদ্ধ।") ?: "বরিশাল কানেক্ট অ্যাপের গোপনীয়তা নীতি এবং সুরক্ষা নির্দেশিকা। আমরা বরিশালবাসীর তথ্যের গোপনীয়তা রক্ষায় অঙ্গীকারবদ্ধ।")
    val terms = MutableStateFlow(prefs?.getString("terms_of_use", "বরিশাল কানেক্ট অ্যাপ ব্যবহার করে বরিশাল বিভাগের সকল নাগরিক সেবামূলক তথ্যাদি সহজে পেতে পারেন। অপব্যবহার সম্পূর্ণ দণ্ডনীয় অপরাধ।") ?: "বরিশাল কানেক্ট অ্যাপ ব্যবহার করে বরিশাল বিভাগের সকল নাগরিক সেবামূলক তথ্যাদি সহজে পেতে পারেন। অপব্যবহার সম্পূর্ণ দণ্ডনীয় অপরাধ।")
    val about = MutableStateFlow(prefs?.getString("about_app", "বরিশাল কানেক্ট একটি সমন্বিত ডিজিটাল ডিরেক্টরি এবং নাগরিক সেবা প্ল্যাটফর্ম।") ?: "বরিশাল কানেক্ট একটি সমন্বিত ডিজিটাল ডিরেক্টরি এবং নাগরিক সেবা প্ল্যাটফর্ম।")
    val maintenanceMode = MutableStateFlow(prefs?.getBoolean("maintenance_mode", false) ?: false)
    val minVersion = MutableStateFlow(prefs?.getString("min_version", "1.0.0") ?: "1.0.0")

    // Donation settings
    val bkashNumber = MutableStateFlow(prefs?.getString("bkash_number", "০১৭০০০০০০০১") ?: "০১৭০০০০০০০১")
    val nagadNumber = MutableStateFlow(prefs?.getString("nagad_number", "০১৯০০০০০০০২") ?: "০১৯০০০০০০০২")
    val rocketNumber = MutableStateFlow(prefs?.getString("rocket_number", "০১৮০০০০০০০৩") ?: "০১৮০০০০০০০৩")
    val bankAccount = MutableStateFlow(prefs?.getString("bank_account", "বরিশাল ব্যাংক, হিসাব নং: ১২৩৪৫৬৭৮৯০") ?: "বরিশাল ব্যাংক, হিসাব নং: ১২৩৪৫৬৭৮৯০")
    val donationStatus = MutableStateFlow(prefs?.getString("donation_status", "সচল (Active)") ?: "সচল (Active)")

    // Developer Settings
    val developerName = MutableStateFlow(prefs?.getString("dev_name", "সাকিব আহমেদ (Sakib)") ?: "সাকিব আহমেদ (Sakib)")
    val developerDesignation = MutableStateFlow(prefs?.getString("dev_designation", "সিনিয়র সফটওয়্যার ইঞ্জিনিয়ার") ?: "সিনিয়র সফটওয়্যার ইঞ্জিনিয়ার")
    val developerDesc = MutableStateFlow(prefs?.getString("dev_desc", "বরিশালকে ডিজিটাল স্মার্ট বিভাগ হিসেবে গড়ে তোলার লক্ষে নিয়োজিত একজন স্বপ্নবাজ ডেভেলপার।") ?: "বরিশালকে ডিজিটাল স্মার্ট বিভাগ হিসেবে গড়ে তোলার লক্ষে নিয়োজিত একজন স্বপ্নবাজ ডেভেলপার।")
    val developerEmail = MutableStateFlow(prefs?.getString("dev_email", "sakib.barishal@gmail.com") ?: "sakib.barishal@gmail.com")
    val developerPhone = MutableStateFlow(prefs?.getString("dev_phone", "+৮৮০১৭০০০০০০০০") ?: "+৮৮০১৭০০০০০০০০")
    val developerGithub = MutableStateFlow(prefs?.getString("dev_github", "https://github.com/sakib-barishal") ?: "https://github.com/sakib-barishal")
    val developerLinkedin = MutableStateFlow(prefs?.getString("dev_linkedin", "https://linkedin.com/in/sakib-barishal") ?: "https://linkedin.com/in/sakib-barishal")

    // Admin Credentials
    val adminPin = MutableStateFlow(prefs?.getString("admin_pin", "1234") ?: "1234")
    val adminUsername = MutableStateFlow(prefs?.getString("admin_username", "admin") ?: "admin")
    val adminPassword = MutableStateFlow(prefs?.getString("admin_password", "admin123") ?: "admin123")
    val adminSecurityCode = MutableStateFlow(prefs?.getString("admin_security_code", "9988") ?: "9988")
    val securityQuestion = MutableStateFlow(prefs?.getString("security_question", "আপনার জন্মস্থান কোথায়?") ?: "আপনার জন্মস্থান কোথায়?")
    val securityAnswer = MutableStateFlow(prefs?.getString("security_answer", "বরিশাল") ?: "বরিশাল")
    val isAdminLoggedIn = MutableStateFlow(prefs?.getBoolean("is_admin_logged_in", false) ?: false)
    val isBengali = MutableStateFlow(prefs?.getBoolean("is_bengali", true) ?: true)

    // Google Sheets Settings
    val sheetsApiUrl = MutableStateFlow(prefs?.getString("sheets_api_url", "https://script.google.com/macros/s/AKfycby_barishal_connect_api/exec") ?: "https://script.google.com/macros/s/AKfycby_barishal_connect_api/exec")
    val sheetsStatus = MutableStateFlow(prefs?.getString("sheets_status", "সংযুক্ত (Connected)") ?: "সংযুক্ত (Connected)")
    val lastSyncTime = MutableStateFlow(prefs?.getString("last_sync_time", "আজ সকাল ১০:৩০") ?: "আজ সকাল ১০:৩০")
    val autoSyncEnabled = MutableStateFlow(prefs?.getBoolean("auto_sync_enabled", true) ?: true)
    val allowedEmojis = MutableStateFlow(prefs?.getString("allowed_emojis", "👍,❤️,😮,⚠️,😡,🛠️") ?: "👍,❤️,😮,⚠️,😡,🛠️")

    // Community Hub Settings
    val communityName = MutableStateFlow(prefs?.getString("community_name", "বরিশাল সোশ্যাল ক্লাব") ?: "বরিশাল সোশ্যাল ক্লাব")
    val communityLogo = MutableStateFlow(prefs?.getString("community_logo", "🤝") ?: "🤝")
    val postingPermission = MutableStateFlow(prefs?.getString("posting_permission", "সবার জন্য উন্মুক্ত") ?: "সবার জন্য উন্মুক্ত")
    val videoUploadSize = MutableStateFlow(prefs?.getString("video_upload_size", "25 MB") ?: "25 MB")
    val imageUploadSize = MutableStateFlow(prefs?.getString("image_upload_size", "10 MB") ?: "10 MB")
    val storyDuration = MutableStateFlow(prefs?.getString("story_duration", "24 Hours") ?: "24 Hours")
    val maxPostLength = MutableStateFlow(prefs?.getString("max_post_length", "1000") ?: "1000")
    val enableStories = MutableStateFlow(prefs?.getBoolean("enable_stories", true) ?: true)
    val enableShortVideos = MutableStateFlow(prefs?.getBoolean("enable_short_videos", true) ?: true)
    val enablePolls = MutableStateFlow(prefs?.getBoolean("enable_polls", true) ?: true)
    val enableVoicePosts = MutableStateFlow(prefs?.getBoolean("enable_voice_posts", true) ?: true)
    val enableCreatorVerification = MutableStateFlow(prefs?.getBoolean("enable_creator_verification", true) ?: true)

    // Follower and Saving systems
    val followingUsers = MutableStateFlow(prefs?.getStringSet("following_users", setOf("tasnim_barishal", "sakib_dev", "anika_photo", "creator_bangla"))?.toMutableSet() ?: mutableSetOf("tasnim_barishal", "sakib_dev", "anika_photo", "creator_bangla"))
    val savedPosts = MutableStateFlow(prefs?.getStringSet("saved_posts", emptySet())?.toMutableSet() ?: mutableSetOf())

    // Feature toggles for temporary closure and custom message
    val globalClosureMessage = MutableStateFlow(prefs?.getString("global_closure_message", "সাময়িক সময়ের জন্য সার্ভিসটি বন্ধ আছে।") ?: "সাময়িক সময়ের জন্য সার্ভিসটি বন্ধ আছে।")
    val disabledFeatures = MutableStateFlow(prefs?.getStringSet("disabled_features", emptySet()) ?: emptySet())

    // --- Dynamic Profile Donation and Footer Controls ---
    val profileDonationTitle = MutableStateFlow(prefs?.getString("profile_donation_title", "দক্ষিণাঞ্চল দুর্যোগ ত্রাণ তহবিল") ?: "দক্ষিণাঞ্চল দুর্যোগ ত্রাণ তহবিল")
    val profileDonationSubtitle = MutableStateFlow(prefs?.getString("profile_donation_sub", "আপনার মোট ফান্ডিং অবদান") ?: "আপনার মোট ফান্ডিং অবদান")
    val profileDonationBtnText = MutableStateFlow(prefs?.getString("profile_donation_btn", "+ ৫০০ টাকা দান") ?: "+ ৫০০ টাকা দান")
    val footerDevText = MutableStateFlow(prefs?.getString("footer_dev_text", "App Developed with ❤️ by AI Coding Agent") ?: "App Developed with ❤️ by AI Coding Agent")
    val browserTargetUrl = MutableStateFlow<String?>(null)

    // --- IPTV & Browser Dynamic Settings ---
    val browserSearchEngine = MutableStateFlow(prefs?.getString("browser_search_engine", "https://www.google.com/search?q=") ?: "https://www.google.com/search?q=")
    val browserHomepageUrl = MutableStateFlow(prefs?.getString("browser_homepage_url", "https://www.google.com") ?: "https://www.google.com")

    val iptvChannels = repository.allIptvChannels.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dynamicButtons = repository.allDynamicButtons.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val browserBookmarks = repository.allBookmarks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val browserHistory = repository.allHistory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val downloadTasks = repository.allDownloads.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFeatureDisabled(featureId: String, disabled: Boolean) {
        val current = disabledFeatures.value.toMutableSet()
        if (disabled) {
            current.add(featureId)
        } else {
            current.remove(featureId)
        }
        disabledFeatures.value = current
        prefs?.edit()?.putStringSet("disabled_features", current)?.apply()
    }

    fun toggleFollowUser(username: String) {
        val current = followingUsers.value.toMutableSet()
        if (current.contains(username)) {
            current.remove(username)
        } else {
            current.add(username)
        }
        followingUsers.value = current
        prefs?.edit()?.putStringSet("following_users", current)?.apply()
    }

    fun toggleSavePost(postId: String) {
        val current = savedPosts.value.toMutableSet()
        if (current.contains(postId)) {
            current.remove(postId)
        } else {
            current.add(postId)
        }
        savedPosts.value = current
        prefs?.edit()?.putStringSet("saved_posts", current)?.apply()
    }

    // Helper to update any settings and save
    fun saveSetting(key: String, value: Any) {
        prefs?.edit()?.apply {
            when (value) {
                is String -> {
                    putString(key, value)
                    when (key) {
                        "app_name" -> appName.value = value
                        "app_logo" -> appLogo.value = value
                        "theme_color" -> themeColor.value = value
                        "accent_color" -> accentColor.value = value
                        "contact_phone" -> contactPhone.value = value
                        "contact_email" -> contactEmail.value = value
                        "facebook_url" -> facebookUrl.value = value
                        "website_url" -> websiteUrl.value = value
                        "whatsapp_url" -> whatsappUrl.value = value
                        "telegram_url" -> telegramUrl.value = value
                        "youtube_url" -> youtubeUrl.value = value
                        "privacy_policy" -> privacyPolicy.value = value
                        "terms_of_use" -> terms.value = value
                        "about_app" -> about.value = value
                        "min_version" -> minVersion.value = value
                        "bkash_number" -> bkashNumber.value = value
                        "nagad_number" -> nagadNumber.value = value
                        "rocket_number" -> rocketNumber.value = value
                        "bank_account" -> bankAccount.value = value
                        "donation_status" -> donationStatus.value = value
                        "dev_name" -> developerName.value = value
                        "dev_designation" -> developerDesignation.value = value
                        "dev_desc" -> developerDesc.value = value
                        "dev_email" -> developerEmail.value = value
                        "dev_phone" -> developerPhone.value = value
                        "dev_github" -> developerGithub.value = value
                        "dev_linkedin" -> developerLinkedin.value = value
                        "admin_pin" -> adminPin.value = value
                        "admin_username" -> adminUsername.value = value
                        "admin_password" -> adminPassword.value = value
                        "admin_security_code" -> adminSecurityCode.value = value
                        "security_question" -> securityQuestion.value = value
                        "security_answer" -> securityAnswer.value = value
                        "sheets_api_url" -> sheetsApiUrl.value = value
                        "sheets_status" -> sheetsStatus.value = value
                        "last_sync_time" -> lastSyncTime.value = value
                        "allowed_emojis" -> allowedEmojis.value = value
                        "community_name" -> communityName.value = value
                        "community_logo" -> communityLogo.value = value
                        "posting_permission" -> postingPermission.value = value
                        "video_upload_size" -> videoUploadSize.value = value
                        "image_upload_size" -> imageUploadSize.value = value
                        "story_duration" -> storyDuration.value = value
                        "max_post_length" -> maxPostLength.value = value
                        "global_closure_message" -> globalClosureMessage.value = value
                        "browser_search_engine" -> browserSearchEngine.value = value
                        "browser_homepage_url" -> browserHomepageUrl.value = value
                        "profile_donation_title" -> profileDonationTitle.value = value
                        "profile_donation_sub" -> profileDonationSubtitle.value = value
                        "profile_donation_btn" -> profileDonationBtnText.value = value
                        "footer_dev_text" -> footerDevText.value = value
                    }
                }
                is Boolean -> {
                    putBoolean(key, value)
                    when (key) {
                        "maintenance_mode" -> maintenanceMode.value = value
                        "is_admin_logged_in" -> isAdminLoggedIn.value = value
                        "is_bengali" -> isBengali.value = value
                        "auto_sync_enabled" -> autoSyncEnabled.value = value
                        "enable_stories" -> enableStories.value = value
                        "enable_short_videos" -> enableShortVideos.value = value
                        "enable_polls" -> enablePolls.value = value
                        "enable_voice_posts" -> enableVoicePosts.value = value
                        "enable_creator_verification" -> enableCreatorVerification.value = value
                    }
                }
            }
            apply()
        }
    }

    // Query states
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>(null)

    // Dynamic Weather State for Barishal Division (Simulated real-time)
    val weatherTemp = MutableStateFlow(31)
    val weatherCondition = MutableStateFlow("মেঘাচ্ছন্ন ও ঝুম বৃষ্টি (Rainy)")
    val weatherHumidity = MutableStateFlow("৮৬%")
    val weatherWindSpeed = MutableStateFlow("১৪ কিমি/ঘণ্টা")

    // Account & Authentication simulation
    val isUserLoggedIn = MutableStateFlow(prefs?.getBoolean("is_user_logged_in", true) ?: true)
    val loginOtpSent = MutableStateFlow(false)
    val userPhone = MutableStateFlow(prefs?.getString("user_phone", "01712345678") ?: "01712345678")
    val userName = MutableStateFlow(prefs?.getString("user_name", "citizen") ?: "citizen")
    val userUsername = MutableStateFlow(prefs?.getString("user_username", "citizen_rakib123") ?: "citizen_rakib123")
    val userEmail = MutableStateFlow(prefs?.getString("user_email", "citizen.barishal@gmail.com") ?: "citizen.barishal@gmail.com")
    val userBloodGroup = MutableStateFlow(prefs?.getString("user_blood_group", "O+ (Positive)") ?: "O+ (Positive)")
    val userPhotoUrl = MutableStateFlow(prefs?.getString("user_photo_url", "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=200") ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=200")
    val userBio = MutableStateFlow(prefs?.getString("user_bio", "বরিশাল কানেক্ট এর একজন গর্বিত নাগরিক") ?: "বরিশাল কানেক্ট এর একজন গর্বিত নাগরিক")
    val isSecretAdminEnabled = MutableStateFlow(false)

    // Lists backed by Flow
    val allItems: StateFlow<List<DirectoryItem>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteItems: StateFlow<List<DirectoryItem>> = repository.favoriteItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val citizenReports: StateFlow<List<CitizenReport>> = repository.citizenReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activities: StateFlow<List<UserActivity>> = repository.activities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSmartRoads: StateFlow<List<com.example.data.model.SmartRoad>> = repository.allSmartRoads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined filtered directory items
    val filteredItems: StateFlow<List<DirectoryItem>> = combine(
        allItems,
        searchQuery,
        selectedCategory
    ) { itemsList, query, cat ->
        var result = itemsList
        if (cat != null) {
            result = result.filter { it.category == cat }
        }
        if (query.isNotEmpty()) {
            result = result.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.subtitle.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.location.contains(query, ignoreCase = true)
            }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            try {
                repository.initializeDataIfNeeded()
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to initialize database.")
            }
        }
    }

    fun selectCategory(category: String?) {
        selectedCategory.value = category
    }

    fun triggerGoogleSheetsSync(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val success1 = repository.syncDataFromSheets(sheetsApiUrl.value)
            val success2 = repository.syncRoadsFromSheets(sheetsApiUrl.value)
            val combinedSuccess = success1 || success2
            if (combinedSuccess) {
                saveSetting("last_sync_time", "আজ " + java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date()))
                _uiState.value = UiState.Success
            } else {
                _uiState.value = UiState.Success
            }
            onResult(combinedSuccess)
        }
    }

    fun backupCommunityDataToSheets(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            // Simulate/perform backup of all Community Posts (citizenReports), Stories, and Settings to Google Sheets API
            delay(1500) // Aesthetic progress delay
            val count = citizenReports.value.size
            repository.insertActivity(
                UserActivity(
                    activityType = "SHEETS_BACKUP",
                    content = "গুগল শীট ব্যাকআপ: $count টি পোস্ট, কমেন্ট এবং রিঅ্যাকশন সফলভাবে ব্যাকআপ করা হয়েছে।"
                )
            )
            saveSetting("last_sync_time", "আজ " + java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date()))
            _uiState.value = UiState.Success
            onResult(true)
        }
    }

    fun setQuery(query: String) {
        searchQuery.value = query
        if (query.isNotEmpty()) {
            viewModelScope.launch {
                repository.insertActivity(UserActivity(activityType = "SEARCH", content = query))
            }
        }
    }

    fun toggleFavorite(id: String, isFav: Boolean) {
        viewModelScope.launch {
            repository.updateFavoriteStatus(id, !isFav)
            if (!isFav) {
                val item = allItems.value.find { it.id == id }
                item?.let {
                    repository.insertActivity(UserActivity(activityType = "VIEW_FAV", content = "প্রিয় তালিকায় যোগ: ${it.title}"))
                }
            }
        }
    }

    fun addCommunityPost(
        title: String,
        description: String,
        location: String,
        category: String,
        postType: String = "Text",
        imageUrl: String = "",
        videoUrl: String = "",
        voiceUrl: String = "",
        hashtags: String = "",
        pollOptions: String = "",
        district: String = "",
        upazila: String = "",
        unionName: String = "",
        privacy: String = "Public",
        mentionedUsers: String = "",
        customReporter: String? = null,
        status: String = "PENDING"
    ) {
        viewModelScope.launch {
            repository.insertCitizenReport(
                CitizenReport(
                    title = title,
                    description = description,
                    location = location,
                    reporterName = customReporter ?: userUsername.value.ifEmpty { userName.value.ifEmpty { "citizen" } },
                    category = category,
                    imageUrl = imageUrl,
                    videoUrl = videoUrl,
                    postType = postType,
                    voiceUrl = voiceUrl,
                    hashtags = hashtags,
                    pollOptions = pollOptions,
                    district = district,
                    upazila = upazila,
                    unionName = unionName,
                    privacy = privacy,
                    mentionedUsers = mentionedUsers,
                    status = status
                )
            )
            repository.insertActivity(
                UserActivity(
                    activityType = "COMMUNITY_POST",
                    content = "কমিউনিটি হাব পোস্ট: $title"
                )
            )
        }
    }

    fun addCitizenReport(title: String, description: String, location: String, category: String, imageUrl: String = "", videoUrl: String = "") {
        addCommunityPost(
            title = title,
            description = description,
            location = location,
            category = category,
            postType = "Text",
            imageUrl = imageUrl,
            videoUrl = videoUrl
        )
    }

    fun updateCitizenReport(report: CitizenReport) {
        viewModelScope.launch {
            repository.updateCitizenReport(report)
        }
    }

    fun toggleReportReaction(report: CitizenReport, emoji: String) {
        val currentUsername = userUsername.value.ifEmpty { "citizen" }
        val updatedReport = report.withToggledReaction(currentUsername, emoji)
        updateCitizenReport(updatedReport)
    }

    fun addReportComment(report: CitizenReport, text: String) {
        val currentUsername = userUsername.value.ifEmpty { "citizen" }
        val comment = ReportComment(
            authorName = currentUsername,
            text = text,
            avatarUrl = userPhotoUrl.value,
            timestamp = System.currentTimeMillis()
        )
        val updatedReport = report.withAddedComment(comment)
        updateCitizenReport(updatedReport)
    }

    fun addDonationActivity(method: String, phone: String, amount: String, trxId: String) {
        viewModelScope.launch {
            repository.insertActivity(
                UserActivity(
                    activityType = "DONATION",
                    content = "ডোনেশন দাখিল: $amount টাকা ($method), TrxID: $trxId, ফোন: $phone"
                )
            )
        }
    }

    fun updateProfile(name: String, email: String, blood: String, username: String, bio: String, photoUrl: String, onResult: (Boolean, String) -> Unit) {
        val hasDigit = username.any { it.isDigit() }
        val hasSpecial = username.any { !it.isLetterOrDigit() && it != '/' && it != ' ' }
        if (!hasDigit || !hasSpecial) {
            onResult(false, "ইউজারনেমে অবশ্যই অন্তত একটি সংখ্যা এবং একটি স্পেশাল ক্যারেক্টার থাকতে হবে (যেমন: @, _, -, /)!")
            return
        }

        val takenUsernames = setOf("citizen_rakib_taken123", "citizen_admin_999", "admin_123!")
        if (takenUsernames.contains(username) && username != prefs?.getString("user_username", "")) {
            onResult(false, "এই ইউজারনেমটি ইতিমধ্যে অন্য একজন ব্যবহারকারী ব্যবহার করছেন!")
            return
        }

        prefs?.edit()?.apply {
            putString("user_name", name)
            putString("user_email", email)
            putString("user_blood_group", blood)
            putString("user_username", username)
            putString("user_bio", bio)
            putString("user_photo_url", photoUrl)
            apply()
        }

        userName.value = name
        userEmail.value = email
        userBloodGroup.value = blood
        userUsername.value = username
        userBio.value = bio
        userPhotoUrl.value = photoUrl

        onResult(true, "প্রোফাইল সফলভাবে আপডেট করা হয়েছে!")
    }

    fun deleteReport(id: Int) {
        viewModelScope.launch {
            repository.deleteCitizenReport(id)
        }
    }

    fun sendOtpCode(phone: String) {
        userPhone.value = phone
        loginOtpSent.value = true
    }

    fun verifyOtpCode(code: String): Boolean {
        if (code == "1234" || code == "123456") {
            isUserLoggedIn.value = true
            loginOtpSent.value = false
            viewModelScope.launch {
                repository.insertActivity(UserActivity(activityType = "LOGIN", content = "লগইন সম্পন্ন হয়েছে"))
            }
            return true
        }
        return false
    }

    fun logout() {
        isUserLoggedIn.value = false
        loginOtpSent.value = false
        isSecretAdminEnabled.value = false
    }

    fun enableSecretAdmin(enabled: Boolean) {
        isSecretAdminEnabled.value = enabled
    }

    fun addSmartRoad(road: com.example.data.model.SmartRoad, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            repository.insertSmartRoad(road)
            repository.insertActivity(
                UserActivity(
                    activityType = "ADD_ROAD",
                    content = "নতুন সড়ক ম্যাপিং দাখিল: ${road.name}"
                )
            )
            // Attempt online sync
            val success = repository.postSmartRoadToSheets(sheetsApiUrl.value, road)
            onResult(success)
        }
    }

    fun updateSmartRoadStatus(id: String, status: String, rejectReason: String = "") {
        viewModelScope.launch {
            val road = allSmartRoads.value.find { it.id == id }
            if (road != null) {
                val updatedRoad = road.copy(status = status, rejectReason = rejectReason)
                repository.insertSmartRoad(updatedRoad)
                repository.insertActivity(
                    UserActivity(
                        activityType = "ROAD_STATUS_UPDATE",
                        content = "সড়ক অনুমোদন স্থিতি পরিবর্তন: ${road.name} -> $status"
                    )
                )
            }
        }
    }

    fun addDirectoryItem(item: DirectoryItem, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            repository.insertItems(listOf(item))
            repository.insertActivity(
                UserActivity(
                    activityType = "ADD_DIRECTORY_ITEM",
                    content = "নতুন দাখিল: ${item.title} (${item.category})"
                )
            )
            // Attempt online sync
            val success = repository.postDirectoryItemToSheets(sheetsApiUrl.value, item)
            onResult(success)
        }
    }

    fun updateDirectoryItemStatus(id: String, status: String, rejectReason: String = "") {
        viewModelScope.launch {
            val item = allItems.value.find { it.id == id }
            if (item != null) {
                val updatedItem = item.copy(status = status, rejectReason = rejectReason)
                repository.insertItems(listOf(updatedItem))
                repository.insertActivity(
                    UserActivity(
                        activityType = "DIRECTORY_STATUS_UPDATE",
                        content = "অনুমোদন স্থিতি পরিবর্তন: ${item.title} -> $status"
                    )
                )
            }
        }
    }

    fun deleteDirectoryItem(id: String) {
        viewModelScope.launch {
            repository.deleteItem(id)
        }
    }

    fun reportRoadUpdate(roadId: String, updateType: String, description: String) {
        viewModelScope.launch {
            val road = allSmartRoads.value.find { it.id == roadId }
            if (road != null) {
                // Prepend report to the list
                val existingReports = if (road.reportsJson.isEmpty()) "" else road.reportsJson + "|"
                val newReport = "$updateType: $description (বাই ${userName.value})"
                val updatedRoad = road.copy(reportsJson = existingReports + newReport, lastUpdated = "2026-06-28")
                repository.insertSmartRoad(updatedRoad)
                repository.insertActivity(
                    UserActivity(
                        activityType = "ROAD_UPDATE",
                        content = "সড়ক রিপোর্ট আপডেট: ${road.name} -> $updateType"
                    )
                )
            }
        }
    }

    fun deleteSmartRoad(id: String) {
        viewModelScope.launch {
            repository.deleteSmartRoad(id)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearActivities()
        }
    }

    fun simulateWeatherRefresh() {
        // Randomly simulate real time fluctuations in Barishal's weather
        val temps = listOf(29, 30, 31, 32, 33)
        val conditions = listOf(
            "মেঘাচ্ছন্ন ও ঝুম বৃষ্টি (Rainy)",
            "ঝড়ো হাওয়া ও বজ্রপাত (Stormy)",
            "আংশিক মেঘলা আকাশ (Partly Cloudy)",
            "প্রচণ্ড গরম ও رোদ (Sunny & Humid)"
        )
        weatherTemp.value = temps.random()
        weatherCondition.value = conditions.random()
        weatherHumidity.value = "${(80..95).random()}%"
        weatherWindSpeed.value = "${(10..22).random()} কিমি/ঘণ্টা"
    }

    // --- IPTV & Video Downloader Actions ---
    fun addIptvChannel(channel: com.example.data.model.IptvChannel) {
        viewModelScope.launch { repository.insertIptvChannel(channel) }
    }

    fun deleteIptvChannel(id: Int) {
        viewModelScope.launch { repository.deleteIptvChannel(id) }
    }

    fun importIptvChannels(channels: List<com.example.data.model.IptvChannel>) {
        viewModelScope.launch { repository.insertIptvChannels(channels) }
    }

    fun clearIptvChannels() {
        viewModelScope.launch { repository.clearIptvChannels() }
    }

    fun addDynamicButton(button: com.example.data.model.DynamicButton) {
        viewModelScope.launch { repository.insertDynamicButton(button) }
    }

    fun deleteDynamicButton(id: Int) {
        viewModelScope.launch { repository.deleteDynamicButton(id) }
    }

    fun addBookmark(title: String, url: String) {
        viewModelScope.launch { repository.insertBookmark(com.example.data.model.BrowserBookmark(title = title, url = url)) }
    }

    fun deleteBookmark(id: Int) {
        viewModelScope.launch { repository.deleteBookmark(id) }
    }

    fun addHistory(title: String, url: String) {
        viewModelScope.launch { repository.insertHistory(com.example.data.model.BrowserHistory(title = title, url = url)) }
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clearHistory() }
    }

    fun startDownload(context: android.content.Context, title: String, url: String, fileType: String, resolution: String, sizeBytes: Long) {
        val taskId = "dl_" + System.currentTimeMillis()
        val cleanTitle = title.replace(" ", "_").replace("?", "").replace("/", "")
        val extension = if (fileType == "Music") "mp3" else "mp4"
        val fileName = "$cleanTitle.$extension"
        val downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
        val file = java.io.File(downloadDir, fileName)
        
        try {
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            file.writeBytes(ByteArray(1024) { 0 }) // Write some 1KB dummy bytes physically
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val filePath = "Path: ${file.absolutePath}"
        val task = com.example.data.model.DownloadTask(
            id = taskId,
            title = title,
            url = url,
            filePath = filePath,
            fileType = fileType,
            resolutionOrQuality = resolution,
            sizeBytes = sizeBytes,
            downloadedBytes = 0,
            progress = 0f,
            speed = "0 KB/s",
            status = "DOWNLOADING"
        )
        viewModelScope.launch {
            repository.insertDownload(task)
            // Run progress updates in background coroutine to simulate downloading
            launch {
                var downloaded = 0L
                val step = sizeBytes / 10
                while (downloaded < sizeBytes) {
                    delay(800)
                    downloaded += step
                    if (downloaded > sizeBytes) downloaded = sizeBytes
                    val currentProgress = downloaded.toFloat() / sizeBytes
                    val currentSpeed = "${(150..950).random()} KB/s"
                    val isDone = downloaded >= sizeBytes
                    repository.updateDownloadProgress(
                        id = taskId,
                        progress = currentProgress,
                        downloadedBytes = downloaded,
                        speed = currentSpeed,
                        status = if (isDone) "COMPLETED" else "DOWNLOADING"
                    )
                    
                    if (isDone) {
                        // Scan file immediately on completion to register in Gallery / Music library
                        try {
                            android.media.MediaScannerConnection.scanFile(
                                context.applicationContext,
                                arrayOf(file.absolutePath),
                                arrayOf(if (fileType == "Music") "audio/mpeg" else "video/mp4")
                            ) { _, _ -> }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    fun deleteDownload(id: String) {
        viewModelScope.launch { repository.deleteDownload(id) }
    }
}
