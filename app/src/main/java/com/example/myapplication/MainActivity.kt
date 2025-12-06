package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeManager = ThemeManager.getInstance(this)

            CompositionLocalProvider(LocalThemeManager provides themeManager) {
                MyApplicationTheme(darkTheme = themeManager.isDarkMode) {
                    val navController = rememberNavController()

                    // Initialize TokenAuthManager
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
    // Shared view models
    val postViewModel: PostViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val storyProjectViewModel: StoryProjectViewModel = viewModel()
    val communityProjectViewModel: CommunityProjectViewModel = viewModel()

    // Check for existing authentication on startup
    LaunchedEffect(Unit) {
        tokenAuthManager.checkExistingAuth { isAuthenticated ->
            if (isAuthenticated) {
                Log.d("MainActivity", "User authenticated: ${tokenAuthManager.currentUser.value?.name}")
                // User is already logged in, navigation will be handled by login screen
            } else {
                Log.d("MainActivity", "No existing authentication found")
            }
        }
    }

    // Sync current user ID to CommunityProjectViewModel whenever it changes
    LaunchedEffect(tokenAuthManager.currentUser.value) {
        val userId = tokenAuthManager.currentUser.value?.id
        Log.d("MainActivity", "Syncing user ID to CommunityProjectViewModel: $userId")
        communityProjectViewModel.setCurrentUserId(userId)
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

        // PASSWORD RESET 1 (REQUEST)
        composable("forgot_password_request") {
            val vm: AuthViewModel = viewModel()
            RequestResetCodeScreen(
                viewModel = vm,
                onBackToLogin = { navController.popBackStack() },
                onCodeSent = { navController.navigate("forgot_password_code") }
            )
        }

        // PASSWORD RESET 2 (CODE INPUT)
        composable("forgot_password_code") {
            val vm: AuthViewModel = viewModel()
            CodeInputScreen(
                viewModel = vm,
                onCodeVerified = { navController.navigate("forgot_password_new_password") },
                onBackToLogin = { navController.popBackStack("login", false) }
            )
        }

        // PASSWORD RESET 3 (NEW PASSWORD)
        composable("forgot_password_new_password") {
            val vm: AuthViewModel = viewModel()
            NewPasswordScreen(
                viewModel = vm,
                onPasswordChanged = { navController.popBackStack("login", false) },
                onBackToLogin = { navController.popBackStack("login", false) }
                onPasswordChanged = { navController.popBackStack("login", false) },
                onBackToLogin = { navController.popBackStack("login", false) }
            )
        }

        // Community section: list, create, edit
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
            MainScreen(navController) {
                com.example.myapplication.community.ui.screens.NotificationScreen(
                    postViewModel = postViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // CREATE POST
        composable("create_post") {
            CreatePostScreen(
                postViewModel = postViewModel,
                onBack = { navController.popBackStack() },
                onPostCreated = { navController.popBackStack() }
            )
        }

        // EDIT POST
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

        // MY PROJECTS (Story Projects with tabs)
        composable("my_projects") {
            LaunchedEffect(Unit) {
                storyProjectViewModel.loadProjects()
                communityProjectViewModel.loadCommunityProjects()
            }
            MainScreen(navController) {
                ProjectsMainScreen(
                    storyProjectViewModel = storyProjectViewModel,
                    communityProjectViewModel = communityProjectViewModel,
                    onProjectClick = { projectId ->
                        // Navigate to flow builder for personal projects
                        if (!projectId.isNullOrEmpty()) {
                            navController.navigate("flow_builder/$projectId")
                        } else {
                            Log.e("Navigation", "Cannot navigate: projectId is null or empty")
                        }
                    },
                    onCommunityProjectClick = { projectId ->
                        // Navigate to community project view
                        navController.navigate("community_project_view/$projectId")
                    },
                    onForkSuccess = {
                        // Refresh projects after forking
                        storyProjectViewModel.loadProjects()
                    }
                )
            }
        }

        // FLOW BUILDER
        composable(
            route = "flow_builder/{projectId}",
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")

            FlowBuilderScreen(
                projectId = projectId,
                viewModel = storyProjectViewModel,
                onPersist = { flowchartState ->
                    if (projectId != null) {
                        storyProjectViewModel.saveFlowchart(projectId, flowchartState)
                    }
                }
            )
        }

        // COMMUNITY PROJECT VIEW (Optional - for viewing a community project before forking)
        composable(
            route = "community_project_view/{projectId}",
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId")

            // You can create a read-only FlowBuilder view here
            // For now, just show it in the flow builder
            FlowBuilderScreen(
                projectId = projectId,
                viewModel = storyProjectViewModel,
                onPersist = { }
            )
        }

        // CHAT ROOMS
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

        // CHAT ROOM
        composable("chat_room") {
            MainScreen(navController = navController) {
                ChatScreen(
                    viewModel = chatViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // MAIN CHATPAGE (AI)
        composable("chat") {
            val vm: AiConversationViewModel = viewModel()
            MainScreen(navController) {
                ChatPage(viewModel = vm)
            }
        }

        // IMAGE ANALYSIS
        composable("image_analysis") {
            MainScreen(navController) {
                ImageAnalysisScreen(onBack = { navController.popBackStack() })
            }
        }

        // PROFILE
        composable("profile") {
            MainScreen(navController) {
                ProfileScreen(onBack = { navController.popBackStack() })
            }
        }

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
        // Show bottom navigation for main screens
        if (currentRoute in listOf(
                "chat",
                "profile",
                "community",
                "image_analysis",
                "my_projects",
                "community_projects"
            )) {
            MainBottomNavigationBar(navController = navController)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    MyApplicationTheme { Text("Preview") }
}