package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.GlowButton
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(
    viewModel: BarishalViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Collect state flows from viewmodel
    val devName by viewModel.developerName.collectAsState()
    val devDesignation by viewModel.developerDesignation.collectAsState()
    val devDesc by viewModel.developerDesc.collectAsState()
    val devEmail by viewModel.developerEmail.collectAsState()
    val devPhone by viewModel.developerPhone.collectAsState()
    val devGithub by viewModel.developerGithub.collectAsState()
    val devLinkedin by viewModel.developerLinkedin.collectAsState()
    
    // Additional flows stored in SharedPreferences
    val facebookUrl by viewModel.facebookUrl.collectAsState()
    val whatsappUrl by viewModel.whatsappUrl.collectAsState()
    val telegramUrl by viewModel.telegramUrl.collectAsState()
    val websiteUrl by viewModel.websiteUrl.collectAsState()
    val appVersion by viewModel.minVersion.collectAsState()

    val isAdmin by viewModel.isAdminLoggedIn.collectAsState()
    var isEditMode by remember { mutableStateOf(false) }

    // Edit field states
    var nameInput by remember(devName) { mutableStateOf(devName) }
    var designationInput by remember(devDesignation) { mutableStateOf(devDesignation) }
    var descInput by remember(devDesc) { mutableStateOf(devDesc) }
    var emailInput by remember(devEmail) { mutableStateOf(devEmail) }
    var phoneInput by remember(devPhone) { mutableStateOf(devPhone) }
    var githubInput by remember(devGithub) { mutableStateOf(devGithub) }
    var linkedinInput by remember(devLinkedin) { mutableStateOf(devLinkedin) }
    var versionInput by remember(appVersion) { mutableStateOf(appVersion) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "ডেভেলপার পরিচিতি", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("developer_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = {
                            if (isEditMode) {
                                // Save changes
                                viewModel.saveSetting("dev_name", nameInput)
                                viewModel.saveSetting("dev_designation", designationInput)
                                viewModel.saveSetting("dev_desc", descInput)
                                viewModel.saveSetting("dev_email", emailInput)
                                viewModel.saveSetting("dev_phone", phoneInput)
                                viewModel.saveSetting("dev_github", githubInput)
                                viewModel.saveSetting("dev_linkedin", linkedinInput)
                                viewModel.saveSetting("min_version", versionInput)
                                isEditMode = false
                                Toast.makeText(context, "ডেভেলপার তথ্য সফলভাবে আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                            } else {
                                isEditMode = true
                            }
                        }, modifier = Modifier.testTag("developer_edit_toggle")) {
                            Icon(
                                imageVector = if (isEditMode) Icons.Default.Save else Icons.Default.Edit,
                                contentDescription = if (isEditMode) "Save" else "Edit",
                                tint = NeonCyan
                            )
                        }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isEditMode) {
                // Edit Form UI
                SectionHeader(title = "তথ্য পরিবর্তন করুন", subtitle = "ডেভেলপার প্রোফাইলের ক্ষেত্রগুলো সংশোধন করুন")

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GlassTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = "ডেভেলপারের নাম",
                            modifier = Modifier.fillMaxWidth()
                        )

                        GlassTextField(
                            value = designationInput,
                            onValueChange = { designationInput = it },
                            label = "পদবী / ডেজিগনেশন",
                            modifier = Modifier.fillMaxWidth()
                        )

                        GlassTextField(
                            value = descInput,
                            onValueChange = { descInput = it },
                            label = "জীবনী / সংক্ষিপ্ত বিবরণ",
                            modifier = Modifier.fillMaxWidth()
                        )

                        GlassTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = "ইমেইল অ্যাড্রেস",
                            modifier = Modifier.fillMaxWidth()
                        )

                        GlassTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = "ফোন নম্বর",
                            modifier = Modifier.fillMaxWidth()
                        )

                        GlassTextField(
                            value = githubInput,
                            onValueChange = { githubInput = it },
                            label = "গিটহাব লিংক (GitHub)",
                            modifier = Modifier.fillMaxWidth()
                        )

                        GlassTextField(
                            value = linkedinInput,
                            onValueChange = { linkedinInput = it },
                            label = "লিংকডইন লিংক (LinkedIn)",
                            modifier = Modifier.fillMaxWidth()
                        )

                        GlassTextField(
                            value = versionInput,
                            onValueChange = { versionInput = it },
                            label = "অ্যাপ সংস্করণ (Version)",
                            modifier = Modifier.fillMaxWidth()
                        )

                        GlowButton(
                            text = "সংরক্ষণ করুন",
                            onClick = {
                                viewModel.saveSetting("dev_name", nameInput)
                                viewModel.saveSetting("dev_designation", designationInput)
                                viewModel.saveSetting("dev_desc", descInput)
                                viewModel.saveSetting("dev_email", emailInput)
                                viewModel.saveSetting("dev_phone", phoneInput)
                                viewModel.saveSetting("dev_github", githubInput)
                                viewModel.saveSetting("dev_linkedin", linkedinInput)
                                viewModel.saveSetting("min_version", versionInput)
                                isEditMode = false
                                Toast.makeText(context, "ডেভেলপার তথ্য সফলভাবে আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                            },
                            containerColor = NeonTeal,
                            textColor = DarkNavyBackground,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // Beautiful Profile View UI (Glassmorphic, styled)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Profile Glow Avatar Circle
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(BorderStroke(2.dp, NeonCyan), CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(NeonCyan.copy(alpha = 0.4f), Color.Transparent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Developer Avatar",
                                tint = NeonCyan,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Text(
                            text = devName,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = devDesignation,
                            color = TextCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            color = NeonTeal.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, NeonTeal.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "সংস্করণ: $appVersion",
                                color = NeonTeal,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Biography Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                            Text(text = "সংক্ষিপ্ত জীবনী (Biography)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = devDesc,
                            color = TextGray,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Interactive Social & Developer Links Grid
                SectionHeader(title = "যোগাযোগ ও সোশ্যাল মিডিয়া", subtitle = "সরাসরি কানেক্ট করুন")

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val links = listOf(
                        SocialLinkItem("ইমেইল করুন", devEmail, "mailto:$devEmail", Icons.Default.Email, NeonCyan),
                        SocialLinkItem("কল করুন", devPhone, "tel:$devPhone", Icons.Default.Call, NeonTeal),
                        SocialLinkItem("গিটহাব (GitHub)", "গিটহাব প্রোফাইল দেখুন", devGithub, Icons.Default.Terminal, Color.White),
                        SocialLinkItem("লিংকডইন (LinkedIn)", "পেশাদারী নেটওয়ার্ক", devLinkedin, Icons.Default.Business, ElectricBlue),
                        SocialLinkItem("ফেসবুক পেইজ", "অফিসিয়াল আপডেট", facebookUrl, Icons.Default.Share, ElectricBlue),
                        SocialLinkItem("হোয়াটসঅ্যাপ", "সরাসরি চ্যাট", whatsappUrl, Icons.Default.Forum, NeonTeal)
                    )

                    links.forEach { item ->
                        if (item.value.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "লিংক ওপেন করা যায়নি।", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(item.color.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(20.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = item.label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(text = item.value, color = TextGray, fontSize = 11.sp, maxLines = 1)
                                    }

                                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Skills and Tech stack
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(18.dp))
                            Text(text = "দক্ষতা ও টেকনোলজি (Skills)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        val skillsList = listOf(
                            "Kotlin", "Jetpack Compose", "Android SDK",
                            "Google Sheets API", "Room DB", "Coroutines",
                            "Material Design 3", "Clean Architecture"
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            mainAxisSpacing = 8.dp,
                            crossAxisSpacing = 8.dp
                        ) {
                            skillsList.forEach { skill ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(text = skill, color = NeonCyan) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = DarkNavyBackground
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }

        val mainAxisSpacingPx = mainAxisSpacing.roundToPx()
        val crossAxisSpacingPx = crossAxisSpacing.roundToPx()

        var xPosition = 0
        var yPosition = 0
        var rowHeight = 0

        val layoutWidth = constraints.maxWidth
        val layoutHeight = if (placeables.isEmpty()) 0 else run {
            var currentX = 0
            var currentY = 0
            var maxRowHeight = 0
            placeables.forEach { placeable ->
                if (currentX + placeable.width > layoutWidth) {
                    currentX = 0
                    currentY += maxRowHeight + crossAxisSpacingPx
                    maxRowHeight = 0
                }
                currentX += placeable.width + mainAxisSpacingPx
                maxRowHeight = maxOf(maxRowHeight, placeable.height)
            }
            currentY + maxRowHeight
        }

        layout(layoutWidth, layoutHeight) {
            placeables.forEach { placeable ->
                if (xPosition + placeable.width > layoutWidth) {
                    xPosition = 0
                    yPosition += rowHeight + crossAxisSpacingPx
                    rowHeight = 0
                }
                placeable.placeRelative(xPosition, yPosition)
                xPosition += placeable.width + mainAxisSpacingPx
                rowHeight = maxOf(rowHeight, placeable.height)
            }
        }
    }
}

data class SocialLinkItem(
    val label: String,
    val value: String,
    val url: String,
    val icon: ImageVector,
    val color: Color
)
