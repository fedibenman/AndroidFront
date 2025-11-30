package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.chat.ui.ChatListScreen
import com.example.myapplication.chat.ui.ChatScreen
import com.example.myapplication.chat.viewmodel.ChatViewModel
import com.example.myapplication.community.ui.screens.CommunityScreen
import com.example.myapplication.ui.auth.TokenAuthManager
import com.example.myapplication.ui.auth.ImageAnalysisScreen
import com.example.myapplication.community.ui.screens.CreatePostScreen
import com.example.myapplication.community.ui.screens.EditPostScreen
import com.example.myapplication.community.viewmodel.PostViewModel
import com.example.myapplication.ui.auth.ChatPage
import com.example.myapplication.ui.auth.LoginScreen
import com.example.myapplication.ui.auth.SignupScreen
import com.example.myapplication.ui.auth.AuthViewModel
import com.example.myapplication.ui.auth.RequestResetCodeScreen
import com.example.myapplication.ui.auth.CodeInputScreen
import com.example.myapplication.ui.auth.NewPasswordScreen
import com.example.myapplication.ui.auth.ProfileScreen
import com.example.myapplication.ui.components.MainBottomNavigationBar
import com.example.myapplication.ui.theme.LocalThemeManager
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.ThemeManager
import com.example.myapplication.viewModel.AiConversationViewModel

/**
 * MainActivity hosts the Compose navigation graph.
 *
 * This file keeps the new ThemeManager logic (composition local) and both:
 * - the community/chat list screen ("community", "chat_rooms", "chat_room")
 * - the ChatPage used elsewhere (route "chat")
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Initialize ThemeManager and provide it to composition
            val themeManager = ThemeManager.getInstance(this)

            androidx.compose.runtime.CompositionLocalProvider(
                LocalThemeManager provides themeManager
            ) {
                MyApplicationTheme(darkTheme = themeManager.isDarkMode) {
                    val navController = rememberNavController()
                    AppNavHost(navController = navController)
                }
            }
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    // Shared view models
    val postViewModel: PostViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    // Token auth manager for checking existing authentication
    val tokenAuthManager: TokenAuthManager = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            val vm: AuthViewModel = viewModel()
            LoginScreen(
                viewModel = vm,
                onSignupRequested = { navController.navigate("signup") },
                onLoginSuccess = { navController.navigate("chat") },
                onForgotPassword = { navController.navigate("forgot_password_request") }
            )
        }
        // Check authentication state on startup and route accordingly
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
                onCodeVerified = { navController.navigate("forgot_password_new_password") },
                onBackToLogin = { navController.popBackStack("login", inclusive = false) }
            )
        }

        composable("forgot_password_new_password") {
            val vm: AuthViewModel = viewModel()
            NewPasswordScreen(
                viewModel = vm,
                onPasswordChanged = { navController.popBackStack("login", inclusive = false) },
                onBackToLogin = { navController.popBackStack("login", inclusive = false) }
            )
        }

        // Community section: list, create, edit
        composable("community") {
            // Refresh posts when entering the screen
            LaunchedEffect(Unit) {
                postViewModel.loadPosts()
            }
            
            MainScreen(
                navController = navController,
                content = {
                    CommunityScreen(
                        postViewModel = postViewModel,
                        onCreatePost = { navController.navigate("create_post") },
                        onEditPost = { post -> navController.navigate("edit_post/${post._id}") },
                        onDeletePost = { post -> post._id?.let { postViewModel.deletePost(it) {} } },
                        onNavigateToChat = { navController.navigate("chat_rooms") },
                        onNavigateToNotifications = { navController.navigate("notifications") }
                    )
                }
            )
        }
        composable("notifications") {
            com.example.myapplication.community.ui.screens.NotificationScreen(
                postViewModel = postViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("create_post") {
            CreatePostScreen(
                postViewModel = postViewModel,
                onBack = { navController.popBackStack() },
                onPostCreated = { navController.popBackStack() }
            )
        }

        composable(
            "edit_post/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            EditPostScreen(
                postId = postId,
                postViewModel = postViewModel,
                onBack = { navController.popBackStack() },
                onUpdated = { navController.popBackStack() }
            )
        }

        // Chat rooms list and room
        composable("chat_rooms") {
            LaunchedEffect(Unit) { chatViewModel.loadRooms() }
            MainScreen(navController = navController) {
                ChatListScreen(viewModel = chatViewModel, onRoomClick = { room ->
                    chatViewModel.selectRoom(room)
                    navController.navigate("chat_room")
                })
            }
        }

        composable("chat_room") {
            MainScreen(navController = navController) {
                ChatScreen(viewModel = chatViewModel)
            }
        }

        // ChatPage route used elsewhere (keeps the second chat)
        composable("chat") {
            val vm: AiConversationViewModel = viewModel()
            MainScreen(navController = navController) {
                ChatPage(viewModel = vm)
            }
        }

        composable("profile") {
            MainScreen(navController = navController) {
                ProfileScreen(onBack = { navController.popBackStack() })
            }
        }

        // Fallback simple home
        composable("home") {
            Text(text = "Home screen (replace with your app's home)")
        }
    }
}

@Composable
fun MainScreen(navController: NavHostController, content: @Composable () -> Unit) {
    Scaffold(bottomBar = {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        // Only show bottom navigation for main screens (chat, profile, community, image_analysis)
        if (currentRoute in listOf("chat", "profile", "community", "image_analysis")) {
            MainBottomNavigationBar(navController = navController)
        }
    }) { padding ->
        Box(modifier = Modifier.padding(padding)) { content() }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    MyApplicationTheme { Text("Preview") }
}
