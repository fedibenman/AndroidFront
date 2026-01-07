package com.example.myapplication


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.ui.ChatPage
import com.example.myapplication.ui.auth.LoginScreen
import com.example.myapplication.ui.auth.SignupScreen
import com.example.myapplication.ui.auth.RequestResetCodeScreen
import com.example.myapplication.ui.auth.CodeInputScreen
import com.example.myapplication.ui.auth.NewPasswordScreen
import com.example.myapplication.ui.auth.SplashScreen
import com.example.myapplication.ui.auth.EnhancedProfileScreen
import com.example.myapplication.ui.auth.EnhancedProfileViewModel
import com.example.myapplication.ui.components.MainBottomNavigationBar
import com.example.myapplication.ui.theme.AppTheme
import com.example.myapplication.viewModel.AiConversationViewModel
import com.example.myapplication.community.ui.screens.CommunityScreen
import com.example.myapplication.community.ui.screens.CreatePostScreen
import com.example.myapplication.community.ui.screens.EditPostScreen
import com.example.myapplication.community.viewmodel.PostViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.myapplication.storyCreator.ViewModel.AuthViewModel
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.home.HomeViewModel
import com.example.myapplication.ui.game.GameDetailsScreen
import com.example.myapplication.ui.game.GameDetailsViewModel
import com.example.myapplication.ui.collection.MyCollectionScreen
import com.example.myapplication.ui.collection.CollectionViewModel
import com.example.myapplication.ui.mission.MissionChecklistScreen
import com.example.myapplication.ui.mission.MissionChecklistViewModel
import com.example.myapplication.ui.teammate.TeammateFinderScreen
import com.example.myapplication.ui.teammate.TeammateFinderViewModel
import com.example.myapplication.ui.auth.ImageAnalysisScreen
import com.example.myapplication.storyCreator.Views.ProjectsMainScreen
import com.example.myapplication.storyCreator.ViewModel.StoryProjectViewModel
import com.example.myapplication.storyCreator.ViewModel.CommunityProjectViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}


@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    // Shared ViewModel for Community features
    val postViewModel: PostViewModel = viewModel()
    
    // ViewModels for new features
    val homeViewModel: HomeViewModel = viewModel()
    val collectionViewModel: CollectionViewModel = viewModel()

    NavHost(navController = navController, startDestination = "splash") {
        // Splash Screen - Auto-login check
        composable("splash") {
            SplashScreen(navController = navController)
        }
        
        composable("login") {
            val vm: AuthViewModel = viewModel()
            LoginScreen(
                viewModel = vm,
                onSignupRequested = { navController.navigate("signup") },
                onLoginSuccess = { navController.navigate("home") },
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
        
        // NEW ROUTES
        composable("home") {
            MainScreen(navController = navController) {
                HomeScreen(navController, homeViewModel)
            }
        }
        
        composable("collection") {
            MainScreen(navController = navController) {
                MyCollectionScreen(navController, collectionViewModel)
            }
        }
        
        composable("finder") {
            val vm: TeammateFinderViewModel = viewModel()
            MainScreen(navController = navController) {
                TeammateFinderScreen(navController, vm)
            }
        }
        
        composable(
            "gameDetails/{gameId}/{gameName}",
            arguments = listOf(
                navArgument("gameId") { type = NavType.IntType },
                navArgument("gameName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getInt("gameId") ?: return@composable
            val gameName = backStackEntry.arguments?.getString("gameName") ?: ""
            val vm: GameDetailsViewModel = viewModel()
            GameDetailsScreen(navController, vm, gameId, gameName)
        }
        
        composable(
            "missions/{gameId}/{gameName}",
            arguments = listOf(
                navArgument("gameId") { type = NavType.IntType },
                navArgument("gameName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getInt("gameId") ?: return@composable
            val gameName = backStackEntry.arguments?.getString("gameName") ?: ""
            val vm: MissionChecklistViewModel = viewModel()
            MissionChecklistScreen(navController, vm, gameId, gameName)
        }

        composable("chat") {
            val aiViewModel: AiConversationViewModel = viewModel()
            MainScreen(
                navController = navController,
                content = {
                    ChatPage(viewModel = aiViewModel)
                }
            )
        }
        composable("profile") {
            val vm: EnhancedProfileViewModel = viewModel()
            MainScreen(
                navController = navController,
                content = {
                    EnhancedProfileScreen(
                        viewModel = vm,
                        onLogout = {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            )
        }
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
                        onDeletePost = { post -> post._id?.let { postViewModel.deletePost(it) {} } }
                    )
                }
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
        
        // Image Analysis Screen
        composable("image_analysis") {
            MainScreen(
                navController = navController,
                content = {
                    ImageAnalysisScreen(onBack = { navController.popBackStack() })
                }
            )
        }
        
        // Projects Screen
        composable("projects") {
            val storyVm: StoryProjectViewModel = viewModel()
            val communityVm: CommunityProjectViewModel = viewModel()
            MainScreen(
                navController = navController,
                content = {
                    ProjectsMainScreen(
                        storyProjectViewModel = storyVm,
                        communityProjectViewModel = communityVm,
                        onProjectClick = { projectId -> /* TODO: navigate to project details */ },
                        onCommunityProjectClick = { projectId -> /* TODO: navigate to community project */ },
                        onForkSuccess = { /* TODO: handle fork success */ }
                    )
                }
            )
        }
    }
}

/**
 * Main screen composable that includes floating bottom navigation
 */
@Composable
fun MainScreen(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomNav = currentRoute in listOf("home", "chat", "community", "collection", "profile", "finder", "image_analysis", "projects")
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Notification banner state
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    // Current user ID for accepting invites
    var currentUserId by remember { mutableStateOf<String?>(null) }
    
    // Invite popup state - store the full notification data
    var pendingInvite by remember { mutableStateOf<org.json.JSONObject?>(null) }
    var showInvitePopup by remember { mutableStateOf(false) }
    var showAcceptedToast by remember { mutableStateOf(false) }
    
    // Connect to Socket.IO when MainScreen is displayed (global for all authenticated screens)
    LaunchedEffect(Unit) {
        // Get userId from TokenDataStoreManager
        val tokenManager = com.example.myapplication.ui.auth.TokenDataStoreManager(context)
        tokenManager.userIdFlow.collect { userId ->
            if (!userId.isNullOrBlank()) {
                currentUserId = userId
                android.util.Log.d("MainScreen", "Connecting socket for user: $userId")
                com.example.myapplication.network.SocketManager.connect(userId)
            }
        }
    }
    
    // Collect notifications globally and show banner
    LaunchedEffect(Unit) {
        com.example.myapplication.network.SocketManager.notificationFlow.collect { notification ->
            val type = notification.optString("type", "New Notification")
            val fromUserName = notification.optJSONObject("fromUser")?.optString("name", "Someone") ?: "Someone"
            android.util.Log.d("MainScreen", "Showing notification banner: $type from $fromUserName")
            
            // Store the full notification for popup (only for GAME_INVITE)
            if (type == "GAME_INVITE") {
                pendingInvite = notification
                notificationMessage = "🎮 $fromUserName invited you to play!"
            } else if (type == "INVITE_ACCEPTED") {
                pendingInvite = null // Don't show popup for acceptance
                notificationMessage = "✅ $fromUserName accepted your invite! 🎉"
            } else {
                notificationMessage = "🔔 New notification from $fromUserName"
            }
            
            // Auto-hide after 6 seconds (longer to give time to tap)
            coroutineScope.launch {
                kotlinx.coroutines.delay(6000)
                notificationMessage = null
            }
        }
    }
    
    // Show accepted toast
    LaunchedEffect(showAcceptedToast) {
        if (showAcceptedToast) {
            kotlinx.coroutines.delay(3000)
            showAcceptedToast = false
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Main content - add bottom padding for nav bar space
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomNav) 90.dp else 0.dp)
        ) {
            content()
        }
        
        // Floating navigation bar overlay
        if (showBottomNav) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                MainBottomNavigationBar(navController = navController)
            }
        }
        
        // Custom notification banner at top - CLICKABLE
        androidx.compose.animation.AnimatedVisibility(
            visible = notificationMessage != null,
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { -it }),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = 32.dp) // Extra padding for status bar
                    .clickable {
                        // Open invite popup if we have pending invite
                        if (pendingInvite != null) {
                            showInvitePopup = true
                            notificationMessage = null
                        }
                    },
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50)
                )
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notificationMessage ?: "",
                        color = androidx.compose.ui.graphics.Color.White,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Tap to respond →",
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        // Accepted toast at top
        if (showAcceptedToast) {
            androidx.compose.material3.Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .padding(top = 32.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF2196F3)
                )
            ) {
                Text(
                    text = "✓ Invite accepted! Waiting for match...",
                    modifier = Modifier.padding(16.dp),
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    }
    
    // Invite popup dialog
    if (showInvitePopup && pendingInvite != null && currentUserId != null) {
        val invite = pendingInvite!!
        val fromUser = invite.optJSONObject("fromUser")
        val fromUserId = fromUser?.optString("_id", "") ?: ""
        val fromUserName = fromUser?.optString("name", "Unknown") ?: "Unknown"
        val gameId = invite.optInt("gameId", 0).takeIf { it != 0 }
        val notificationId = invite.optString("_id", null)
        
        com.example.myapplication.ui.components.InvitePopupDialog(
            fromUserId = fromUserId,
            fromUserName = fromUserName,
            gameId = gameId,
            notificationId = notificationId,
            myUserId = currentUserId!!,
            onDismiss = {
                showInvitePopup = false
                pendingInvite = null
            },
            onMatchSuccess = { name ->
                showAcceptedToast = true
                showInvitePopup = false
                pendingInvite = null
            },
            onStartChat = { otherUserId, otherUserName ->
                // Navigate to chat with this user
                // Using existing chat route format
                showInvitePopup = false
                pendingInvite = null
                navController.navigate("chat")
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    AppTheme {
        Text("Preview")
    }
}

