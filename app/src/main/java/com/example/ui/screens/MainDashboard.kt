package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.theme.*
import com.example.ui.viewmodel.BarishalViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    viewModel: BarishalViewModel,
    navController: NavController
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    
    val context = LocalContext.current
    val isLoggedIn by viewModel.isUserLoggedIn.collectAsState()
    val userName by viewModel.userName.collectAsState()

    // Feature toggles and closure message states
    val disabledFeaturesSet by viewModel.disabledFeatures.collectAsState()
    val globalClosureMsg by viewModel.globalClosureMessage.collectAsState()
    var closureMsgToShow by remember { mutableStateOf<String?>(null) }

    fun checkAndNavigate(featureId: String, navigateAction: () -> Unit) {
        if (disabledFeaturesSet.contains(featureId)) {
            closureMsgToShow = globalClosureMsg
        } else {
            navigateAction()
        }
    }

    // Bottom Navigation Items
    val navItems = listOf(
        BottomNavItem("হোম", Icons.Default.Home, "tab_home"),
        BottomNavItem("খুঁজুন", Icons.Default.Search, "tab_search"),
        BottomNavItem("মানচিত্র", Icons.Default.Map, "tab_map"),
        BottomNavItem("বার্তা", Icons.Default.Notifications, "tab_notif"),
        BottomNavItem("প্রোফাইল", Icons.Default.Person, "tab_profile")
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkNavySurface,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .border(
                        BorderStroke(1.dp, GlassBorder),
                        RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                    )
            ) {
                // Drawer Profile Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(DarkNavySurface, DarkNavySurfaceCard)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(NeonCyan, ElectricBlue))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.firstOrNull()?.toString()?.uppercase() ?: "U",
                                color = DarkNavyBackground,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = if (isLoggedIn) userName else "অতিথি ব্যবহারকারী",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "বরিশাল কানেক্ট সুপার অ্যাপ",
                            color = TextCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                Divider(color = GlassBorder)

                // Drawer Links list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DrawerItemRow("হোম ড্যাশবোর্ড", Icons.Default.Dashboard, "drawer_home") {
                        selectedTab = 0
                        scope.launch { drawerState.close() }
                    }
                    
                    DrawerItemRow("আমার প্রোফাইল", Icons.Default.AccountCircle, "drawer_profile") {
                        selectedTab = 4
                        scope.launch { drawerState.close() }
                    }
                    
                    DrawerItemRow("প্রিয় তালিকা", Icons.Default.Favorite, "drawer_favs") {
                        selectedTab = 4 // Opens profile which contains favorites list
                        scope.launch { drawerState.close() }
                    }

                    DrawerItemRow("নাগরিক অভিযোগ", Icons.Default.Description, "drawer_reports") {
                        scope.launch { drawerState.close() }
                        checkAndNavigate("citizen_reports") {
                            navController.navigate("reports")
                        }
                    }

                    DrawerItemRow("ত্রাণ ও রক্তদান", Icons.Default.VolunteerActivism, "drawer_donation") {
                        scope.launch { drawerState.close() }
                        checkAndNavigate("donation") {
                            navController.navigate("donation")
                        }
                    }

                    DrawerItemRow("জরুরি SOS নম্বর", Icons.Default.Sos, "drawer_emergency") {
                        scope.launch { drawerState.close() }
                        checkAndNavigate("emergency_sos") {
                            navController.navigate("emergency")
                        }
                    }

                    DrawerItemRow("স্মার্ট সড়ক ও ট্রাফিক", Icons.Default.Traffic, "drawer_smart_road") {
                        scope.launch { drawerState.close() }
                        checkAndNavigate("road") {
                            navController.navigate("road_dashboard")
                        }
                    }

                    DrawerItemRow("দক্ষ কর্মী ডিরেক্টরি", Icons.Default.Engineering, "drawer_worker") {
                        scope.launch { drawerState.close() }
                        checkAndNavigate("worker") {
                            navController.navigate("worker_dashboard")
                        }
                    }

                    DrawerItemRow("চিকিৎসা সেবা ও ডাক্তার", Icons.Default.LocalHospital, "drawer_med") {
                        scope.launch { drawerState.close() }
                        checkAndNavigate("hospital") {
                            navController.navigate("hospital_dashboard")
                        }
                    }

                    DrawerItemRow("শিক্ষা প্রতিষ্ঠান ডিরেক্টরি", Icons.Default.School, "drawer_edu") {
                        scope.launch { drawerState.close() }
                        checkAndNavigate("school") {
                            navController.navigate("education_dashboard")
                        }
                    }

                    DrawerItemRow("আবহাওয়া ও পূর্বাভাস", Icons.Default.Cloud, "drawer_weather") {
                        scope.launch { drawerState.close() }
                        checkAndNavigate("weather") {
                            navController.navigate("weather_dashboard")
                        }
                    }

                    DrawerItemRow("স্মার্ট কৃষি ও ফসল", Icons.Default.Agriculture, "drawer_agriculture") {
                        scope.launch { drawerState.close() }
                        checkAndNavigate("agriculture") {
                            navController.navigate("agriculture_dashboard")
                        }
                    }

                    DrawerItemRow("লাইভ বাজার দর", Icons.Default.Storefront, "drawer_market") {
                        scope.launch { drawerState.close() }
                        checkAndNavigate("market") {
                            navController.navigate("market_dashboard")
                        }
                    }

                    DrawerItemRow("যাতায়াত ও সময়সূচী", Icons.Default.DirectionsBus, "drawer_transport") {
                        scope.launch { drawerState.close() }
                        checkAndNavigate("bus") {
                            navController.navigate("transport_dashboard")
                        }
                    }

                    DrawerItemRow("দুর্যোগ সতর্কীকরণ", Icons.Default.Campaign, "drawer_disaster") {
                        scope.launch { drawerState.close() }
                        checkAndNavigate("disaster") {
                            navController.navigate("disaster_dashboard")
                        }
                    }

                    DrawerItemRow("IPTV ও লাইভ খেলা", Icons.Default.Tv, "drawer_iptv") {
                        scope.launch { drawerState.close() }
                        checkAndNavigate("iptv") {
                            navController.navigate("iptv")
                        }
                    }

                    DrawerItemRow("স্মার্ট ব্রাউজার", Icons.Default.Language, "drawer_browser") {
                        scope.launch { drawerState.close() }
                        checkAndNavigate("browser") {
                            navController.navigate("browser")
                        }
                    }

                    Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 4.dp))

                    DrawerItemRow("দর্শনীয় স্থান ও পর্যটন", Icons.Default.Landscape, "drawer_tourist") {
                        scope.launch { drawerState.close() }
                        checkAndNavigate("tourist") {
                            navController.navigate("category/tourist")
                        }
                    }

                    DrawerItemRow("ইতিহাস ও ঐতিহ্য", Icons.Default.HistoryEdu, "drawer_history") {
                        scope.launch { drawerState.close() }
                        navController.navigate("history_dashboard")
                    }

                    DrawerItemRow("সরকারি দপ্তর ও কন্টাক্ট", Icons.Default.AccountBalance, "drawer_gov") {
                        scope.launch { drawerState.close() }
                        checkAndNavigate("gov") {
                            navController.navigate("category/gov")
                        }
                    }

                    DrawerItemRow("স্থানীয় ব্যবসা ডিরেক্টরি", Icons.Default.Storefront, "drawer_business") {
                        scope.launch { drawerState.close() }
                        checkAndNavigate("business") {
                            navController.navigate("category/business")
                        }
                    }

                    DrawerItemRow("ডেভেলপার প্রোফাইল", Icons.Default.Code, "drawer_developer") {
                        scope.launch { drawerState.close() }
                        navController.navigate("developer_dashboard")
                    }

                    Divider(color = GlassBorder, modifier = Modifier.padding(vertical = 8.dp))

                    DrawerItemRow("অ্যাপ শেয়ার করুন", Icons.Default.Share, "drawer_share") {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "বরিশাল বিভাগের সকল তথ্য ও সেবা এক অ্যাপে - বরিশাল কানেক্ট ডাউনলোড করুন!")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share app link"))
                        scope.launch { drawerState.close() }
                    }

                    DrawerItemRow("রেটিং দিন", Icons.Default.Star, "drawer_rate") {
                        Toast.makeText(context, "রেটিং দেয়ার জন্য ধন্যবাদ!", Toast.LENGTH_SHORT).show()
                        scope.launch { drawerState.close() }
                    }

                    DrawerItemRow("লগআউট", Icons.Default.Logout, "drawer_logout") {
                        viewModel.logout()
                        scope.launch { drawerState.close() }
                        Toast.makeText(context, "লগআউট সম্পন্ন হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        },
        modifier = Modifier.testTag("modal_navigation_drawer")
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val appNameState by viewModel.appName.collectAsState()
                        val appLogoState by viewModel.appLogo.collectAsState()
                        var logoTapCount by remember { mutableStateOf(0) }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .testTag("secret_admin_trigger")
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) {
                                    logoTapCount++
                                    if (logoTapCount >= 10) {
                                        logoTapCount = 0
                                        navController.navigate("admin")
                                    }
                                }
                        ) {
                            // Elegant B logo with blue/cyan gradient and shadow-like feel
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF0891B2), Color(0xFF1D4ED8))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = appLogoState,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                            }
                            
                            Column {
                                Text(
                                    text = appNameState,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    lineHeight = 16.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val isConnected = viewModel.sheetsStatus.collectAsState().value.contains("সংযুক্ত") || viewModel.sheetsStatus.collectAsState().value.contains("Connected")
                                    val pulseColor = if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444)
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(pulseColor, CircleShape)
                                    )
                                    Text(
                                        text = if (isConnected) "SHEETS LIVE" else "SHEETS OFFLINE",
                                        color = pulseColor,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("drawer_menu_button")
                        ) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        // Quick Profile shortcut avatar
                        IconButton(
                            onClick = { selectedTab = 4 },
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .testTag("top_bar_avatar_shortcut")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .border(BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.3f)), CircleShape)
                                    .padding(2.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(NeonCyan.copy(alpha = 0.15f), ElectricBlue.copy(alpha = 0.25f))
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userName.firstOrNull()?.toString()?.uppercase() ?: "U",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkNavyBackground
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = DarkNavySurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .background(DarkNavySurface)
                        .testTag("bottom_navigation_bar")
                ) {
                    navItems.forEachIndexed { idx, item ->
                        val selected = selectedTab == idx
                        NavigationBarItem(
                            selected = selected,
                            onClick = { selectedTab = idx },
                            label = { 
                                Text(
                                    text = item.label, 
                                    fontSize = 10.sp, 
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ) 
                            },
                            icon = { 
                                Icon(
                                    imageVector = item.icon, 
                                    contentDescription = item.label
                                ) 
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = NeonCyan,
                                indicatorColor = NeonCyan.copy(alpha = 0.2f),
                                unselectedIconColor = TextGray.copy(alpha = 0.5f),
                                unselectedTextColor = TextGray.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            },
            containerColor = DarkNavyBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> HomeTab(
                        viewModel = viewModel,
                        onCategoryClick = { category ->
                            checkAndNavigate(category) {
                                when (category) {
                                    "road" -> navController.navigate("road_dashboard")
                                    "worker" -> navController.navigate("worker_dashboard")
                                    "job" -> navController.navigate("job_dashboard")
                                    "hospital", "doctor" -> navController.navigate("hospital_dashboard")
                                    "school" -> navController.navigate("education_dashboard")
                                    "weather" -> navController.navigate("weather_dashboard")
                                    "agriculture" -> navController.navigate("agriculture_dashboard")
                                    "market" -> navController.navigate("market_dashboard")
                                    "bus", "launch", "transport" -> navController.navigate("transport_dashboard")
                                    "disaster" -> navController.navigate("disaster_dashboard")
                                    "browser" -> navController.navigate("browser")
                                    "iptv" -> navController.navigate("iptv")
                                    else -> navController.navigate("category/$category")
                                }
                            }
                        },
                        onNavigateToReports = {
                            checkAndNavigate("citizen_reports") {
                                navController.navigate("reports")
                            }
                        },
                        onNavigateToDonation = {
                            checkAndNavigate("donation") {
                                navController.navigate("donation")
                            }
                        },
                        onNavigateToEmergency = {
                            checkAndNavigate("emergency_sos") {
                                navController.navigate("emergency")
                            }
                        }
                    )
                    1 -> SearchTab(
                        viewModel = viewModel,
                        onItemClick = { item -> navController.navigate("detail/${item.id}") }
                    )
                    2 -> MapTab(viewModel = viewModel)
                    3 -> NotificationTab()
                    4 -> ProfileTab(
                        viewModel = viewModel,
                        onNavigateToAdmin = { navController.navigate("admin") },
                        onNavigateToItemDetail = { item -> navController.navigate("detail/${item.id}") }
                    )
                }
            }
        }
    }

    if (closureMsgToShow != null) {
        AlertDialog(
            onDismissRequest = { closureMsgToShow = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = "Warning", tint = Color.Red)
                    Text(text = "সেবা সাময়িক বন্ধ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Text(text = closureMsgToShow ?: "", color = TextGray, fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = { closureMsgToShow = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text(text = "ঠিক আছে", color = DarkNavyBackground, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkNavySurface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(BorderStroke(1.dp, GlassBorder), RoundedCornerShape(16.dp))
        )
    }
}

@Composable
fun DrawerItemRow(
    label: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = NeonCyan, modifier = Modifier.size(20.dp))
        Text(text = label, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val testTag: String
)
