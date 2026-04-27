package com.example.swift.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import com.example.swift.ui.screens.*
import com.example.swift.ui.theme.*
import com.example.swift.viewmodel.AuthViewModel
import com.example.swift.viewmodel.BookingViewModel

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Main : Screen("main")
    data object Schedule : Screen("schedule")
    data object CoachSelection : Screen("coach_selection")
    data object PassengerDetails : Screen("passenger_details")
    data object SeatSelection : Screen("seat_selection")
    data object PaymentSummary : Screen("payment_summary")
    data object TicketReceipt : Screen("ticket_receipt")
    data object RefundPolicy : Screen("refund_policy")
    data object ScheduleInfo : Screen("schedule_info")
    data object Announcement : Screen("announcement")
}

sealed class BottomTab(val route: String, val label: String) {
    data object Home : BottomTab("tab_home", "Halaman Utama")
    data object Inbox : BottomTab("tab_inbox", "Kotak Masuk")
    data object Account : BottomTab("tab_account", "Akun")
}

@Composable
fun SwiftNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val bookingViewModel: BookingViewModel = viewModel()

    NavHost(
        navController = navController, 
        startDestination = Screen.Splash.route,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            )
        }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                isLoggedIn = authViewModel.isLoggedIn
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Main.route) {
            MainScreenWithBottomNav(
                authViewModel = authViewModel,
                bookingViewModel = bookingViewModel,
                onSearchClick = {
                    navController.navigate(Screen.Schedule.route)
                },
                onRefundPolicyClick = {
                    navController.navigate(Screen.RefundPolicy.route)
                },
                onScheduleInfoClick = {
                    navController.navigate(Screen.ScheduleInfo.route)
                },
                onAnnouncementClick = {
                    navController.navigate(Screen.Announcement.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Schedule.route) {
            ScheduleScreen(
                bookingViewModel = bookingViewModel,
                onTimeSelected = {
                    navController.navigate(Screen.CoachSelection.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CoachSelection.route) {
            CoachSelectionScreen(
                bookingViewModel = bookingViewModel,
                onCoachSelected = {
                    navController.navigate(Screen.PassengerDetails.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PassengerDetails.route) {
            PassengerDetailsScreen(
                bookingViewModel = bookingViewModel,
                onNextClicked = {
                    navController.navigate(Screen.SeatSelection.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SeatSelection.route) {
            SeatSelectionScreen(
                bookingViewModel = bookingViewModel,
                onSaveClicked = {
                    navController.navigate(Screen.PaymentSummary.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PaymentSummary.route) {
            PaymentSummaryScreen(
                bookingViewModel = bookingViewModel,
                authViewModel = authViewModel,
                onPaymentComplete = {
                    navController.navigate(Screen.TicketReceipt.route) {
                        popUpTo(Screen.Main.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TicketReceipt.route) {
            TicketReceiptScreen(
                bookingViewModel = bookingViewModel,
                onBackToHome = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.RefundPolicy.route) {
            RefundPolicyScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ScheduleInfo.route) {
            ScheduleInfoScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Announcement.route) {
            AnnouncementScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainScreenWithBottomNav(
    authViewModel: AuthViewModel,
    bookingViewModel: BookingViewModel,
    onSearchClick: () -> Unit,
    onRefundPolicyClick: () -> Unit,
    onScheduleInfoClick: () -> Unit,
    onAnnouncementClick: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(BottomTab.Home, BottomTab.Inbox, BottomTab.Account)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SwiftWhite,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home
                                    1 -> if (selectedTab == 1) Icons.Filled.Inbox else Icons.Outlined.Inbox
                                    else -> if (selectedTab == 2) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle
                                },
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SwiftRed,
                            selectedTextColor = SwiftRed,
                            unselectedIconColor = SwiftGray,
                            unselectedTextColor = SwiftGray,
                            indicatorColor = SwiftRed.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    bookingViewModel = bookingViewModel,
                    onSearchClick = onSearchClick,
                    onRefundPolicyClick = onRefundPolicyClick,
                    onScheduleInfoClick = onScheduleInfoClick,
                    onAnnouncementClick = onAnnouncementClick
                )
                1 -> InboxPlaceholder()
                2 -> AccountScreen(
                    authViewModel = authViewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
private fun InboxPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Inbox,
                contentDescription = null,
                tint = SwiftGrayMedium,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Belum ada pesan",
                style = MaterialTheme.typography.titleMedium,
                color = SwiftGray
            )
            Text(
                "Pesan dan notifikasi akan muncul di sini",
                style = MaterialTheme.typography.bodySmall,
                color = SwiftGrayMedium
            )
        }
    }
}
