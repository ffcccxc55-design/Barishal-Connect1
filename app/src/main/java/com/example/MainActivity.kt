package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.AppDatabase
import com.example.data.repository.BarishalRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BarishalViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Lightweight local database & repository constructor initialization
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = BarishalRepository(database.directoryDao())
        val prefs = applicationContext.getSharedPreferences("barishal_connect_prefs", android.content.Context.MODE_PRIVATE)
        val viewModel = BarishalViewModel(repository, prefs)
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController,
                    startDestination = "splash",
                    enterTransition = { fadeIn(animationSpec = tween(300)) },
                    exitTransition = { fadeOut(animationSpec = tween(300)) }
                ) {
                    // 1. Animated Splash Screen
                    composable("splash") {
                        SplashScreen(
                            onNavigateToHome = {
                                navController.navigate("dashboard") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    // 2. Master Dashboard containing Bottom Tabs & Modal Drawer
                    composable("dashboard") {
                        MainDashboard(
                            viewModel = viewModel,
                            navController = navController
                        )
                    }
                    
                    // 3. Category Directories lists Screen
                    composable(
                        route = "category/{catId}",
                        arguments = listOf(navArgument("catId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val catId = backStackEntry.arguments?.getString("catId") ?: "hospital"
                        when (catId) {
                            "tourist" -> {
                                TourismScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            "gov" -> {
                                GovServiceScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            "business" -> {
                                BusinessDirectoryScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            else -> {
                                CategoryListScreen(
                                    category = catId,
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() },
                                    onItemClick = { item ->
                                        navController.navigate("detail/${item.id}")
                                    }
                                )
                            }
                        }
                    }

                    // 3B. History Directory Module
                    composable("history_dashboard") {
                        HistoryScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 3C. Developer Bio Profile Screen
                    composable("developer_dashboard") {
                        DeveloperScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    
                    // 4. Listing Detail page
                    composable(
                        route = "detail/{itemId}",
                        arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                        ItemDetailScreen(
                            itemId = itemId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    
                    // 5. Citizen complaints lists & filing screen
                    composable("reports") {
                        CitizenReportScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    
                    // 6. Relief Donations & Blood Donors directories screen
                    composable("donation") {
                        DonationScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    
                    // 7. Click-to-dial SOS emergency directory screen
                    composable("emergency") {
                        EmergencyScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    
                    // 8. Hidden secret admin portal screen
                    composable("admin") {
                        AdminPanelScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 9. Premium Smart Road Mapping & GPS Navigation Module
                    composable("road_dashboard") {
                        com.example.ui.screens.SmartRoadScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 10. Skilled Worker Directory Module
                    composable("worker_dashboard") {
                        com.example.ui.screens.WorkerScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 11. Local Job System Module
                    composable("job_dashboard") {
                        com.example.ui.screens.JobScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 12. Hospital & Doctor Directory Module
                    composable("hospital_dashboard") {
                        com.example.ui.screens.HospitalDoctorScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 13. Education Directory Module
                    composable("education_dashboard") {
                        com.example.ui.screens.EducationScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 14. Weather & Climate Dashboard
                    composable("weather_dashboard") {
                        com.example.ui.screens.WeatherScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 15. Smart Agriculture & Crop Information Module
                    composable("agriculture_dashboard") {
                        com.example.ui.screens.AgricultureScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 16. Live Market Price Dashboard
                    composable("market_dashboard") {
                        com.example.ui.screens.MarketScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 17. Bus & Launch Schedule (Transport) Module
                    composable("transport_dashboard") {
                        com.example.ui.screens.TransportScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 18. Disaster Management & Shelter Alerts System
                    composable("disaster_dashboard") {
                        com.example.ui.screens.DisasterScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 19. Smart Lightweight Web Browser with Video Downloader
                    composable("browser") {
                        com.example.ui.screens.BrowserScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // 20. Live IPTV & Sports Channel Player
                    composable("iptv") {
                        com.example.ui.screens.IptvScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

