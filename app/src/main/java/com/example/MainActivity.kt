package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.achievements.AchievementsScreen
import com.example.ui.admin.AdminPanelScreen
import com.example.ui.analytics.ReportsScreen
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.RegisterScreen
import com.example.ui.dashboard.HomeScreen
import com.example.ui.leaderboard.LeaderboardScreen
import com.example.ui.quiz.QuizScreen
import com.example.ui.quiz.ReviewAnswersScreen
import com.example.ui.result.ResultScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.QuizMasterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val appSettings by mainViewModel.appSettings.collectAsState()
            val currentUser by mainViewModel.currentUser.collectAsState()

            QuizMasterTheme(darkTheme = appSettings.isDarkMode) {
                val navController = rememberNavController()
                val startDestination = if (currentUser != null) "main" else "login"

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("login") {
                        LoginScreen(
                            viewModel = mainViewModel,
                            onNavigateToRegister = { navController.navigate("register") },
                            onLoginSuccess = {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            viewModel = mainViewModel,
                            onNavigateToLogin = { navController.popBackStack() },
                            onRegisterSuccess = {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("main") {
                        MainAppShell(
                            viewModel = mainViewModel,
                            onStartQuiz = { category, difficulty ->
                                mainViewModel.startQuiz(category, difficulty)
                                navController.navigate("quiz")
                            },
                            onNavigateToLeaderboard = { navController.navigate("leaderboard_full") },
                            onNavigateToAchievements = { navController.navigate("achievements") },
                            onNavigateToAdmin = { navController.navigate("admin") },
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("quiz") {
                        QuizScreen(
                            viewModel = mainViewModel,
                            onQuizSubmitted = {
                                navController.navigate("result") {
                                    popUpTo("quiz") { inclusive = true }
                                }
                            },
                            onExitQuiz = { navController.popBackStack() }
                        )
                    }

                    composable("result") {
                        ResultScreen(
                            viewModel = mainViewModel,
                            onReviewAnswers = { navController.navigate("review") },
                            onRetakeQuiz = {
                                val currentSession = mainViewModel.quizSession.value
                                if (currentSession != null) {
                                    mainViewModel.startQuiz(currentSession.categoryName, currentSession.difficulty)
                                    navController.navigate("quiz") {
                                        popUpTo("result") { inclusive = true }
                                    }
                                } else {
                                    navController.popBackStack("main", false)
                                }
                            },
                            onHome = {
                                navController.popBackStack("main", false)
                            }
                        )
                    }

                    composable("review") {
                        ReviewAnswersScreen(
                            viewModel = mainViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("achievements") {
                        AchievementsScreen(viewModel = mainViewModel)
                    }

                    composable("leaderboard_full") {
                        LeaderboardScreen(viewModel = mainViewModel)
                    }

                    composable("admin") {
                        AdminPanelScreen(
                            viewModel = mainViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppShell(
    viewModel: MainViewModel,
    onStartQuiz: (String, String) -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedBottomTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedBottomTab == 0,
                    onClick = { selectedBottomTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 1,
                    onClick = { selectedBottomTab = 1 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Reports") },
                    label = { Text("Reports") }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 2,
                    onClick = { selectedBottomTab = 2 },
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard") },
                    label = { Text("Rankings") }
                )
                NavigationBarItem(
                    selected = selectedBottomTab == 3,
                    onClick = { selectedBottomTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            when (selectedBottomTab) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onStartQuiz = onStartQuiz,
                    onNavigateToLeaderboard = onNavigateToLeaderboard,
                    onNavigateToAchievements = onNavigateToAchievements,
                    onNavigateToAdmin = onNavigateToAdmin
                )
                1 -> ReportsScreen(viewModel = viewModel)
                2 -> LeaderboardScreen(viewModel = viewModel)
                3 -> SettingsScreen(viewModel = viewModel, onLogout = onLogout)
            }
        }
    }
}
