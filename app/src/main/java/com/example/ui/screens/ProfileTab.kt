package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DirectoryItem
import com.example.data.model.UserActivity
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.GlowButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel

@Composable
fun ProfileTab(
    viewModel: BarishalViewModel,
    onNavigateToAdmin: () -> Unit,
    onNavigateToItemDetail: (DirectoryItem) -> Unit
) {
    val isLoggedIn by viewModel.isUserLoggedIn.collectAsState()
    val isOtpSent by viewModel.loginOtpSent.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isLoggedIn) {
            // Login view
            LoginCard(
                isOtpSent = isOtpSent,
                onSendOtp = { phone ->
                    if (phone.length < 11) {
                        Toast.makeText(context, "সঠিক মোবাইল নম্বর দিন", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.sendOtpCode(phone)
                        Toast.makeText(context, "ওটিপি কোড পাঠানো হয়েছে: ১২৩৪ (OTP: 1234)", Toast.LENGTH_LONG).show()
                    }
                },
                onVerifyOtp = { code ->
                    val success = viewModel.verifyOtpCode(code)
                    if (success) {
                        Toast.makeText(context, "লগইন সফল হয়েছে!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "ভুল ওটিপি কোড (১২৩৪ ট্রাই করুন)", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } else {
            // Logged In view
            ProfileDashboard(
                viewModel = viewModel,
                onNavigateToAdmin = onNavigateToAdmin,
                onNavigateToItemDetail = onNavigateToItemDetail
            )
        }
    }
}

@Composable
fun LoginCard(
    isOtpSent: Boolean,
    onSendOtp: (String) -> Unit,
    onVerifyOtp: (String) -> Unit
) {
    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "User Login",
            tint = NeonCyan,
            modifier = Modifier.size(80.dp)
        )
        
        Text(
            text = "ব্যবহারকারী লগইন",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "মোবাইল নম্বর দিয়ে ওটিপি ভেরিফাই সম্পন্ন করুন",
            color = TextCyan,
            fontSize = 12.sp
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isOtpSent) {
                    GlassTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = "মোবাইল নম্বর",
                        placeholder = "যেমন: ০১৭১২৩৪৫৬৭৮",
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = NeonCyan) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        testTag = "login_phone_input"
                    )
                    
                    GlowButton(
                        text = "ওটিপি কোড পাঠান (Get OTP)",
                        onClick = { onSendOtp(phoneInput) },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "get_otp_button"
                    )
                } else {
                    Text(
                        text = "আমরা $phoneInput নম্বরে একটি ৪ ডিজিটের ওটিপি পাঠিয়েছি। ডেমো ওটিপি: ১২৩৪",
                        color = TextCyan,
                        fontSize = 11.sp
                    )
                    
                    GlassTextField(
                        value = otpInput,
                        onValueChange = { otpInput = it },
                        label = "ওটিপি কোড লিখুন (OTP Code)",
                        placeholder = "৪ ডিজিটের কোড (১২৩৪)",
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NeonCyan) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        testTag = "login_otp_input"
                    )
                    
                    GlowButton(
                        text = "লগইন সম্পন্ন করুন (Verify)",
                        onClick = { onVerifyOtp(otpInput) },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "verify_otp_button"
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileDashboard(
    viewModel: BarishalViewModel,
    onNavigateToAdmin: () -> Unit,
    onNavigateToItemDetail: (DirectoryItem) -> Unit
) {
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userPhone by viewModel.userPhone.collectAsState()
    val userBlood by viewModel.userBloodGroup.collectAsState()
    val userUsername by viewModel.userUsername.collectAsState()
    val userBio by viewModel.userBio.collectAsState()
    val userPhotoUrl by viewModel.userPhotoUrl.collectAsState()
    val favorites by viewModel.favoriteItems.collectAsState()
    val history by viewModel.activities.collectAsState()
    val isAdminEnabled by viewModel.isSecretAdminEnabled.collectAsState()

    val pDonationTitle by viewModel.profileDonationTitle.collectAsState()
    val pDonationSub by viewModel.profileDonationSubtitle.collectAsState()
    val pDonationBtnText by viewModel.profileDonationBtnText.collectAsState()
    val devFooterText by viewModel.footerDevText.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var donationAmount by remember { mutableStateOf(5000) }
    
    // Developer tap counter for secret admin access
    var developerTapCount by remember { mutableStateOf(0) }
    val context = LocalContext.current

    if (showEditDialog) {
        EditProfileDialog(
            currentName = userName,
            currentEmail = userEmail,
            currentBlood = userBlood,
            currentUsername = userUsername,
            currentBio = userBio,
            currentPhotoUrl = userPhotoUrl,
            onDismiss = { showEditDialog = false },
            onSave = { name, email, blood, username, bio, photoUrl ->
                viewModel.updateProfile(name, email, blood, username, bio, photoUrl) { success, msg ->
                    if (success) {
                        showEditDialog = false
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("profile_dashboard_scroll"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Profile Card with Avatar
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar Placeholder or Loaded Image
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(DarkNavySurface),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userPhotoUrl.isNotEmpty() && userPhotoUrl.startsWith("http")) {
                            AsyncImage(
                                model = userPhotoUrl,
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(NeonCyan, ElectricBlue)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userName.firstOrNull()?.toString()?.uppercase() ?: "U",
                                    color = DarkNavyBackground,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@$userUsername",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (userBio.isNotEmpty()) {
                            Text(
                                text = userBio,
                                color = TextGray,
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "ফোন: $userPhone",
                            color = TextCyan,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "রক্তের গ্রুপ: $userBlood",
                            color = RedEmergency,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.testTag("edit_profile_button")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", tint = NeonCyan)
                    }
                }
            }
        }

        // 2. Secret Portal Unlock Banner
        if (isAdminEnabled) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToAdmin)
                        .testTag("admin_portal_unlocked_banner"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = RedEmergency.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, RedEmergency)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = RedEmergency)
                            Column {
                                Text(text = "হাইডেন অ্যাডমিন প্যানেল", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "সিটিজেন রিপোর্ট ও ডিরেক্টরি ডেটা ম্যানেজ করুন", color = TextGray, fontSize = 11.sp)
                            }
                        }
                        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = RedEmergency)
                    }
                }
            }
        }

        // 3. Cyclone Relief donation tracker
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.VolunteerActivism, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(18.dp))
                            Text(text = pDonationTitle, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "$pDonationSub: $donationAmount টাকা", color = TextCyan, fontSize = 11.sp)
                    }
                    
                    GlowButton(
                        text = pDonationBtnText,
                        onClick = {
                            donationAmount += 500
                            Toast.makeText(context, "$pDonationBtnText অবদানের জন্য ধন্যবাদ!", Toast.LENGTH_SHORT).show()
                        },
                        containerColor = NeonTeal,
                        textColor = DarkNavyBackground,
                        modifier = Modifier.height(36.dp)
                    )
                }
            }
        }

        // 4. Favorites list
        item {
            SectionHeader(title = "প্রিয় ডিরেক্টরি তালিকা", subtitle = "আপনার বুকমার্ক করা সেবাসমূহ")
        }

        if (favorites.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(text = "কোন বুকমার্ক সংরক্ষিত নেই।", color = TextGray, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(favorites) { item ->
                SearchResultRow(item = item, onClick = { onNavigateToItemDetail(item) })
            }
        }

        // 5. User action history list
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "সাম্প্রতিক কার্যক্রম", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "অ্যাপে আপনার শেষ ভিজিট বা অনুসন্ধানসমূহ", color = TextCyan, fontSize = 11.sp)
                }
                
                Text(
                    text = "মুছে ফেলুন",
                    color = RedEmergency,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            viewModel.clearSearchHistory()
                            Toast.makeText(context, "কার্যক্রমের হিস্টোরি মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                        .padding(8.dp)
                        .testTag("clear_history_button")
                )
            }
        }

        if (history.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                    border = BorderStroke(1.dp, GlassBorder)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(text = "কোন সাম্প্রতিক কার্যক্রমের রেকর্ড নেই।", color = TextGray, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(history.take(8)) { act ->
                HistoryRow(activity = act)
            }
        }

        // 6. Developer info, terms & conditions, secret portal activator
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = GlassBorder)
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = devFooterText,
                    color = TextGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable {
                            developerTapCount++
                            if (developerTapCount >= 5) {
                                if (!isAdminEnabled) {
                                    viewModel.enableSecretAdmin(true)
                                    Toast.makeText(context, "🎉 অভিনন্দন! সিক্রেট অ্যাডমিন পোর্টাল আনলক হয়েছে!", Toast.LENGTH_LONG).show()
                                } else {
                                    viewModel.enableSecretAdmin(false)
                                    Toast.makeText(context, "অ্যাডমিন পোর্টাল হাইড করা হয়েছে", Toast.LENGTH_SHORT).show()
                                }
                                developerTapCount = 0
                            } else {
                                val remaining = 5 - developerTapCount
                                Toast.makeText(context, "অ্যাডমিন পোর্টাল খুলতে আর $remaining বার চাপুন", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .testTag("secret_dev_button")
                )
                
                Text(
                    text = "গোপনীয়তা নীতি • ব্যবহারের শর্তাবলী • হেল্পলাইন",
                    color = TextCyan,
                    fontSize = 11.sp,
                    modifier = Modifier.clickable {
                        Toast.makeText(context, "বরিশাল কানেক্ট পলিসি লোড হচ্ছে...", Toast.LENGTH_SHORT).show()
                    }
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                GlowButton(
                    text = "লগআউট করুন (Logout)",
                    onClick = {
                        viewModel.logout()
                        Toast.makeText(context, "লগআউট সম্পন্ন হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    containerColor = RedEmergency.copy(alpha = 0.2f),
                    textColor = RedEmergency,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    testTag = "logout_button"
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentEmail: String,
    currentBlood: String,
    currentUsername: String,
    currentBio: String,
    currentPhotoUrl: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }
    var emailInput by remember { mutableStateOf(currentEmail) }
    var bloodInput by remember { mutableStateOf(currentBlood) }
    var usernameInput by remember { mutableStateOf(currentUsername) }
    var bioInput by remember { mutableStateOf(currentBio) }
    var photoUrlInput by remember { mutableStateOf(currentPhotoUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "প্রোফাইল এডিট করুন", color = Color.White) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                GlassTextField(value = nameInput, onValueChange = { nameInput = it }, label = "নাম (Name)", testTag = "edit_name_input")
                
                GlassTextField(
                    value = usernameInput, 
                    onValueChange = { usernameInput = it }, 
                    label = "ইউজারনেম (Username - সংখ্যা ও স্পেশাল ক্যারেক্টার আবশ্যক)", 
                    placeholder = "যেমন: rakib_99!",
                    testTag = "edit_username_input"
                )
                
                GlassTextField(value = bioInput, onValueChange = { bioInput = it }, label = "আপনার সম্পর্কে (Bio)", testTag = "edit_bio_input")
                GlassTextField(value = photoUrlInput, onValueChange = { photoUrlInput = it }, label = "প্রোফাইল ছবি লিংক (Photo URL)", testTag = "edit_photo_url_input")
                GlassTextField(value = emailInput, onValueChange = { emailInput = it }, label = "ইমেইল (Email)", testTag = "edit_email_input")
                GlassTextField(value = bloodInput, onValueChange = { bloodInput = it }, label = "রক্তের গ্রুপ (Blood Group)", testTag = "edit_blood_input")
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(nameInput, emailInput, bloodInput, usernameInput, bioInput, photoUrlInput) }) {
                Text(text = "সংরক্ষণ করুন", color = NeonCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "বাতিল", color = TextGray)
            }
        },
        containerColor = DarkNavySurface
    )
}

@Composable
fun HistoryRow(activity: UserActivity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkNavySurfaceCard)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = when (activity.activityType) {
                    "SEARCH" -> Icons.Default.Search
                    "LOGIN" -> Icons.Default.Login
                    "REPORT" -> Icons.Default.Description
                    else -> Icons.Default.History
                },
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = activity.content,
                color = TextWhite,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        Text(
            text = "রেকর্ড",
            color = TextGray.copy(alpha = 0.5f),
            fontSize = 9.sp
        )
    }
}
