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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import org.json.JSONObject
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
import com.example.myapplication.storyCreator.ViewModel.CommunityProjectViewModel
import com.example.myapplication.storyCreator.ViewModel.StoryProjectViewModel
import com.example.myapplication.storyCreator.Views.FlowBuilderScreen
import com.example.myapplication.storyCreator.Views.ProjectsMainScreen
import com.example.myapplication.directmessages.viewmodel.DirectMessagesViewModel
import com.example.myapplication.directmessages.ui.DirectMessageScreen
import com.example.myapplication.calls.IncomingCallActivity
import android.content.Intent
import com.example.myapplication.ui.auth.*
import com.example.myapplication.storyCreator.ViewModel.AuthViewModel
import com.example.myapplication.ui.auth.ChatPage
import com.example.myapplication.ui.auth.CodeInputScreen
import com.example.myapplication.ui.auth.ImageAnalysisScreen
import com.example.myapplication.ui.auth.LoginScreen
import com.example.myapplication.ui.auth.NewPasswordScreen
import com.example.myapplication.ui.auth.ProfileScreen
import com.example.myapplication.ui.auth.RequestResetCodeScreen
import com.example.myapplication.ui.auth.SignupScreen
import com.example.myapplication.ui.auth.TokenAuthManager
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
                    val tokenAuthManager = remember { TokenAuthManager() }
                    AppNavHost(
                        navController = navController,
                        tokenAuthManager = tokenAuthManager
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {

    val postViewModel: PostViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()

    val storyProjectViewModel: StoryProjectViewModel = viewModel()
    val communityProjectViewModel: CommunityProjectViewModel = viewModel()
    val dmViewModel: DirectMessagesViewModel = viewModel()
    val context = androidx.compose.ui.platform.LocalContext.current

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


    // Sync current user with projects
    LaunchedEffect(tokenAuthManager.currentUser.value) {
        val user = tokenAuthManager.currentUser.value
        communityProjectViewModel.setCurrentUserId(user?.id)
        
        // Listen for incoming calls once logged in
        user?.id?.let { userId ->
            dmViewModel.listenForIncomingCalls(userId)
            dmViewModel.loadConversations(userId)
        }
    }

    // Global Call Listener
    val incomingCall by dmViewModel.incomingCall.collectAsState()
    LaunchedEffect(incomingCall) {
        incomingCall?.let { call ->
            val callJson = JSONObject().apply {
                put("callId", call.callId)
                put("callerId", call.callerId)
                put("callerName", call.callerName)
                put("calleeId", call.calleeId)
                put("calleeName", call.calleeName)
                put("conversationId", call.conversationId)
                put("timestamp", call.timestamp)
            }.toString()

            val intent = Intent(context, IncomingCallActivity::class.java).apply {
                putExtra("callRequest", callJson)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
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
                    onNavigateToNotifications = { navController.navigate("notifications") },
                    onNavigateToDM = { user ->
                        val currentUserId = tokenAuthManager.currentUser.value?.id ?: ""
                        val targetUserId = user._id ?: ""
                        if (currentUserId.isNotEmpty() && targetUserId.isNotEmpty()) {
                            dmViewModel.startConversation(currentUserId, targetUserId) { conversation ->
                                dmViewModel.selectConversation(conversation)
                                navController.navigate("direct_message/${conversation._id}")
                            }
                        }
                    }
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

        composable("chat_rooms") {
            val user = tokenAuthManager.currentUser.value
            LaunchedEffect(Unit) { 
                chatViewModel.loadRooms() 
                user?.id?.let { dmViewModel.loadConversations(it) }
            }
            MainScreen(navController = navController) {
                ChatListScreen(
                    viewModel = chatViewModel,
                    dmViewModel = dmViewModel,
                    currentUserId = user?.id ?: "",
                    onRoomClick = { room ->
                        chatViewModel.selectRoom(room)
                        navController.navigate("chat_room")
                    },
                    onConversationClick = { conversation ->
                        dmViewModel.selectConversation(conversation)
                        navController.navigate("direct_message/${conversation._id}")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(
            "direct_message/{conversationId}",
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { entry ->
            val conversationId = entry.arguments?.getString("conversationId") ?: return@composable
            val user = tokenAuthManager.currentUser.value
            LaunchedEffect(conversationId) {
                user?.id?.let { dmViewModel.loadConversationById(conversationId, it) }
            }
            MainScreen(navController) {
                DirectMessageScreen(
                    viewModel = dmViewModel,
                    userId = user?.id ?: "",
                    currentUserName = user?.name ?: "Me",
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
