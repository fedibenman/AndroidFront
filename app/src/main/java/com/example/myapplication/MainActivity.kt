package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
import com.example.myapplication.community.ui.screens.*
import com.example.myapplication.community.viewmodel.PostViewModel
import com.example.myapplication.ui.auth.*
import com.example.myapplication.ui.components.MainBottomNavigationBar
import com.example.myapplication.ui.theme.LocalThemeManager
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.ThemeManager
import com.example.myapplication.viewModel.AiConversationViewModel
import com.example.myapplication.ui.ChatPage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeManager = ThemeManager.getInstance(this)

            CompositionLocalProvider(LocalThemeManager provides themeManager) {
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

    val postViewModel: PostViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val tokenAuthManager: TokenAuthManager = viewModel()

    // --- FIX: ne doit PAS être dans NavHost ---
    LaunchedEffect(Unit) {
        tokenAuthManager.checkExistingAuth { isAuthenticated ->
            if (isAuthenticated) {
                navController.navigate("chat") {
                    popUpTo("login") { inclusive = true }
                }
            } else {
                navController.navigate("login") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
    }

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
                onPasswordChanged = { navController.popBackStack("login", false) },
                onBackToLogin = { navController.popBackStack("login", false) }
            )
        }

        // COMMUNITY LIST
        composable("community") {
            LaunchedEffect(Unit) { postViewModel.loadPosts() }
            MainScreen(navController = navController) {
                CommunityScreen(
                    postViewModel = postViewModel,
                    onCreatePost = { navController.navigate("create_post") },
                    onEditPost = { post -> navController.navigate("edit_post/${post._id}") },
                    onDeletePost = { post -> post._id?.let { postViewModel.deletePost(it) {} } },
                    onNavigateToChat = { navController.navigate("chat_rooms") },
                    onNavigateToNotifications = { navController.navigate("notifications") }
                )
            }
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
        ) {
            val postId = it.arguments?.getString("postId") ?: return@composable
            EditPostScreen(
                postId = postId,
                postViewModel = postViewModel,
                onBack = { navController.popBackStack() },
                onUpdated = { navController.popBackStack() }
            )
        }

        // CHAT ROOMS LIST
        composable("chat_rooms") {
            LaunchedEffect(Unit) { chatViewModel.loadRooms() }
            MainScreen(navController = navController) {
                ChatListScreen(
                    viewModel = chatViewModel,
                    onRoomClick = { room ->
                        chatViewModel.selectRoom(room)
                        navController.navigate("chat_room")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable("chat_room") {
            MainScreen(navController = navController) {
                ChatScreen(
                    viewModel = chatViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // DIRECT MESSAGES
        composable("direct_messages") {
            val dmViewModel: com.example.myapplication.directmessages.viewmodel.DirectMessagesViewModel = viewModel()
            val currentProfile by tokenAuthManager.currentUser
            val userId = currentProfile?.id ?: ""
            
            LaunchedEffect(userId) { 
                if (userId.isNotBlank()) {
                    dmViewModel.loadConversations(userId) 
                }
            }
            
            MainScreen(navController = navController) {
                com.example.myapplication.directmessages.ui.DirectMessagesListScreen(
                    viewModel = dmViewModel,
                    userId = userId,
                    onConversationClick = { conversation ->
                        navController.navigate("direct_message_chat/${conversation._id}")
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onNewMessage = { navController.navigate("user_search") }
                )
            }
        }

        composable(
            "direct_message_chat/{conversationId}",
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) {
            val conversationId = it.arguments?.getString("conversationId") ?: return@composable
            val dmViewModel: com.example.myapplication.directmessages.viewmodel.DirectMessagesViewModel = viewModel()
            val currentProfile by tokenAuthManager.currentUser
            val userId = currentProfile?.id ?: ""
            
            LaunchedEffect(conversationId, userId) {
                if (userId.isNotBlank()) {
                    dmViewModel.loadConversationById(conversationId, userId)
                }
            }
            
            MainScreen(navController = navController) {
                com.example.myapplication.directmessages.ui.DirectMessageScreen(
                    viewModel = dmViewModel,
                    userId = userId,
                    currentUserName = currentProfile?.name ?: "User",
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable("user_search") {
            val dmViewModel: com.example.myapplication.directmessages.viewmodel.DirectMessagesViewModel = viewModel()
            val currentProfile by tokenAuthManager.currentUser
            val userId = currentProfile?.id ?: ""
            
            MainScreen(navController = navController) {
                com.example.myapplication.directmessages.ui.UserSearchScreen(
                    onUserClick = { user ->
                        android.util.Log.d("MainActivity", "User clicked in search: ${user.name} (${user._id})")
                        android.util.Log.d("MainActivity", "Current userId: $userId")
                        
                        if (userId.isBlank()) {
                            android.util.Log.e("MainActivity", "userId is blank, cannot start conversation")
                        } else {
                            if (user._id.isNullOrBlank()) {
                                android.util.Log.e("MainActivity", "Target user ID is null or blank")
                            } else {
                                android.util.Log.d("MainActivity", "Starting conversation between $userId and ${user._id}")
                                dmViewModel.startConversation(userId, user._id) { conversation ->
                                android.util.Log.d("MainActivity", "Conversation started successfully: ${conversation._id}")
                                android.util.Log.d("MainActivity", "Navigating to direct_message_chat/${conversation._id}")
                                navController.navigate("direct_message_chat/${conversation._id}") {
                                    popUpTo("direct_messages") { inclusive = false }
                                }
                            }
                        }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // MAIN CHATPAGE (AI)
        composable("chat") {
            val vm: AiConversationViewModel = viewModel()
            MainScreen(navController = navController) {
                ChatPage(viewModel = vm)
            }
        }

        composable("image_analysis") {
            MainScreen(navController = navController) {
                ImageAnalysisScreen(onBack = { navController.popBackStack() })
            }
        }

        composable("profile") {
            MainScreen(navController = navController) {
                ProfileScreen(onBack = { navController.popBackStack() })
            }
        }

        composable("home") {
            Text("Home screen (replace with your app's home)")
        }
    }
}


@Composable
fun MainScreen(navController: NavHostController, content: @Composable () -> Unit) {
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            if (currentRoute in listOf("chat", "profile", "community", "image_analysis")) {
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
    MyApplicationTheme { Text("Preview") }
}
