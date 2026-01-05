package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
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
import com.example.myapplication.community.ui.screens.CommunityScreen
import com.example.myapplication.community.ui.screens.CreatePostScreen
import com.example.myapplication.community.ui.screens.EditPostScreen
import com.example.myapplication.community.ui.screens.NotificationScreen
import com.example.myapplication.community.viewmodel.PostViewModel
import com.example.myapplication.storyCreator.ViewModel.CommunityProjectViewModel
import com.example.myapplication.storyCreator.ViewModel.StoryProjectViewModel
import com.example.myapplication.storyCreator.Views.FlowBuilderScreen
import com.example.myapplication.storyCreator.Views.ProjectsMainScreen
import com.example.myapplication.directmessages.viewmodel.DirectMessagesViewModel
import com.example.myapplication.directmessages.ui.DirectMessageScreen
import com.example.myapplication.calls.IncomingCallActivity
import android.content.Intent
import androidx.compose.runtime.collectAsState
import com.example.myapplication.ui.auth.AuthViewModel
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
fun AppNavHost(
    navController: NavHostController,
    tokenAuthManager: TokenAuthManager,
    modifier: Modifier = Modifier
) {
    val postViewModel: PostViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val storyProjectViewModel: StoryProjectViewModel = viewModel()
    val communityProjectViewModel: CommunityProjectViewModel = viewModel()
    val dmViewModel: DirectMessagesViewModel = viewModel()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Auto-navigate based on token
    LaunchedEffect(Unit) {
        tokenAuthManager.checkExistingAuth { isAuthenticated ->
            if (isAuthenticated) {
                navController.navigate("chat") {
                    popUpTo("login") { inclusive = true }
                }
            } else {
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
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
                onBackToLogin = { navController.popBackStack("login", false) }
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

        composable("community") {
            LaunchedEffect(Unit) { postViewModel.loadPosts() }
            MainScreen(navController) {
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
            MainScreen(navController) {
                NotificationScreen(
                    postViewModel = postViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
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

        composable("my_projects") {
            LaunchedEffect(Unit) {
                storyProjectViewModel.loadProjects()
                communityProjectViewModel.loadCommunityProjects()
            }
            MainScreen(navController) {
                ProjectsMainScreen(
                    storyProjectViewModel = storyProjectViewModel,
                    communityProjectViewModel = communityProjectViewModel,
                    onProjectClick = { id -> navController.navigate("flow_builder/$id") },
                    onCommunityProjectClick = { id -> navController.navigate("community_project_view/$id") },
                    onForkSuccess = { storyProjectViewModel.loadProjects() }
                )
            }
        }

        composable(
            route = "flow_builder/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("projectId")
            FlowBuilderScreen(
                projectId = id,
                viewModel = storyProjectViewModel,
                onPersist = { flow -> id?.let { storyProjectViewModel.saveFlowchart(it, flow) } }
            )
        }

        composable(
            "community_project_view/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("projectId")
            FlowBuilderScreen(
                projectId = id,
                viewModel = storyProjectViewModel,
                onPersist = {}
            )
        }

        composable("chat_rooms") {
            val user = tokenAuthManager.currentUser.value
            LaunchedEffect(Unit) { 
                chatViewModel.loadRooms() 
                user?.id?.let { dmViewModel.loadConversations(it) }
            }
            MainScreen(navController) {
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
            MainScreen(navController) {
                ChatScreen(
                    viewModel = chatViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable("chat") {
            val vm: AiConversationViewModel = viewModel()
            MainScreen(navController) {
                ChatPage(viewModel = vm)
            }
        }

        composable("image_analysis") {
            MainScreen(navController) {
                ImageAnalysisScreen(onBack = { navController.popBackStack() })
            }
        }

        composable("profile") {
            MainScreen(navController) {
                ProfileScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun MainScreen(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            if (currentRoute in listOf(
                    "chat",
                    "profile",
                    "community",
                    "image_analysis",
                    "my_projects"
                )
            ) {
                MainBottomNavigationBar(navController = navController)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}