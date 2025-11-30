package com.example.myapplication


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.ui.auth.ChatPage
import com.example.myapplication.ui.auth.LoginScreen
import com.example.myapplication.ui.auth.SignupScreen
import com.example.myapplication.ui.auth.AuthViewModel
import com.example.myapplication.ui.auth.TokenAuthManager
import com.example.myapplication.ui.auth.RequestResetCodeScreen
import com.example.myapplication.ui.auth.CodeInputScreen
import com.example.myapplication.ui.auth.NewPasswordScreen
import com.example.myapplication.ui.auth.ProfileScreen
import com.example.myapplication.ui.auth.ImageAnalysisScreen
import com.example.myapplication.ui.components.MainBottomNavigationBar
import com.example.myapplication.ui.theme.LocalThemeManager
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.ThemeManager
import com.example.myapplication.viewModel.AiConversationViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Initialize ThemeManager
            val themeManager = ThemeManager.getInstance(this)
            
            // Provide ThemeManager to the composition
            androidx.compose.runtime.CompositionLocalProvider(
                LocalThemeManager provides themeManager
            ) {
                MyApplicationTheme(
                    darkTheme = themeManager.isDarkMode
                ) {
                    val navController = rememberNavController()
                    AppNavHost(navController = navController)
                }
            }
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    // Token auth manager for checking existing authentication
    val tokenAuthManager: TokenAuthManager = viewModel()
    
    // Check authentication state on startup
    LaunchedEffect(Unit) {
        tokenAuthManager.checkExistingAuth { isAuthenticated ->
            if (isAuthenticated) {
                // Navigate to chat if already authenticated
                navController.navigate("chat") {
                    // Clear the back stack so user can't go back to login
                    popUpTo("login") { inclusive = true }
                }
            } else {
                // Navigate to login if not authenticated
                navController.navigate("login") {
                    // Clear any existing navigation stack
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                }
            }
        }
    }
    
    NavHost(navController = navController, startDestination = "loading") {
        // Loading screen while checking authentication
        composable("loading") {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Checking authentication...",
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                )
            }
        }
        composable("login") {
            val vm: AuthViewModel = viewModel()
            LoginScreen(
                viewModel = vm,
                onSignupRequested = { navController.navigate("signup") },
                onLoginSuccess = { navController.navigate("chat") },
                onForgotPassword = { navController.navigate("forgot_password_request") }
            )
        }
        composable("signup") {
            val vm: AuthViewModel = viewModel()
            SignupScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onSignupSuccess = { navController.popBackStack() }
            )
        }
        composable("forgot_password_request") {
            val vm: AuthViewModel = viewModel()
            RequestResetCodeScreen(
                viewModel = vm,
                onBackToLogin = { navController.popBackStack() },
                onCodeSent = { navController.navigate("forgot_password_code") }
            )
        }
        composable("forgot_password_code") {
            val vm: AuthViewModel = viewModel()
            CodeInputScreen(
                viewModel = vm,
                onCodeVerified = {
                    // When code is considered valid, go to new password screen
                    navController.navigate("forgot_password_new_password")
                },
                onBackToLogin = {
                    navController.popBackStack("login", inclusive = false)
                }
            )
        }
        composable("forgot_password_new_password") {
            val vm: AuthViewModel = viewModel()
            NewPasswordScreen(
                viewModel = vm,
                onPasswordChanged = {
                    // After successful reset, go back to login
                    navController.popBackStack("login", inclusive = false)
                },
                onBackToLogin = {
                    navController.popBackStack("login", inclusive = false)
                }
            )
        }
composable("chat") {
    val vm: AiConversationViewModel = viewModel()
    MainScreen(
        navController = navController,
        content = {
            ChatPage(
                viewModel = vm,
            )
        }
    )
}

composable("profile") {
    MainScreen(
        navController = navController,
        content = {
            ProfileScreen(
                onEditProfile = {
                    // TODO: Implement profile editing
                },
                onBack = { 
                    navController.popBackStack()
                },
                onLogout = {
                    tokenAuthManager.logout()
                    // Navigate back to login screen
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
    )
}

composable("image_analysis") {
    MainScreen(
        navController = navController,
        content = {
            ImageAnalysisScreen(
                onBack = { navController.popBackStack() }
            )
        }
    )
}
    }
}

/**
 * Main screen composable that includes bottom navigation
 */
@Composable
fun MainScreen(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            // Only show bottom navigation for main screens (chat, profile, and image_analysis)
            if (currentRoute in listOf("chat", "profile", "image_analysis")) {
                MainBottomNavigationBar(navController = navController)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    MyApplicationTheme {
        Text("Preview")
    }
}
