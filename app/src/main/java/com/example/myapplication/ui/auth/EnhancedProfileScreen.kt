package com.example.myapplication.ui.auth

import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*

@Composable
fun EnhancedProfileScreen(
    viewModel: EnhancedProfileViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = viewModel.profile
    val themeManager = ThemeManager.getInstance(LocalContext.current)
    val isDarkMode by themeManager.isDarkMode.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Themed Background (Light: background_general + clouds, Dark: night + glowing stars)
        com.example.myapplication.ui.components.ThemedBackground()
        
        if (viewModel.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (viewModel.error != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Error loading profile",
                    fontFamily = PressStart,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                PixelatedButton(onClick = { viewModel.refreshProfile() }) {
                    Text("Retry", fontFamily = PressStart, fontSize = 10.sp)
                }
            }
        } else if (profile != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Header
                item {
                    Text(
                        text = "PROFILE",
                        fontFamily = PressStart,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 60.dp, start = 20.dp, bottom = 20.dp)
                    )
                }
                
                // Profile Header with Favorite Game Background
                item {
                    ProfileHeader(profile = profile, viewModel = viewModel)
                }
                
                // Bio Section
                if (!profile.bio.isNullOrBlank()) {
                    item {
                        BioSection(bio = profile.bio)
                    }
                }
                
                // Level & XP Progress
                item {
                    LevelProgressBar(
                        level = viewModel.currentLevel,
                        currentXP = viewModel.currentXP,
                        nextLevelXP = viewModel.nextLevelXP,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }
                
                // Achievements
                if (viewModel.hasAchievements) {
                    item {
                        AchievementsSection(achievements = viewModel.achievements)
                    }
                }
                
                // Stats Grid
                item {
                    StatsGrid(viewModel = viewModel)
                }
                
                // Recent Activity
                if (viewModel.recentActivities.isNotEmpty()) {
                    item {
                        RecentActivitySection(activities = viewModel.recentActivities)
                    }
                }
                
                // Gamer DNA Section
                item {
                    GamerDNASection(profile = profile)
                }
                
                // Logout Button
                item {
                    PixelatedButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        containerColor = PixelRed
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("LOGOUT", fontFamily = PressStart, fontSize = 12.sp)
                    }
                }
            }
        }
        
        // Theme Toggle - placed last with zIndex to appear above all content
        AnimatedThemeToggle(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 50.dp, end = 20.dp)
                .zIndex(10f)
        )
        
        // Edit Profile Sheet
        if (viewModel.showingEditProfile) {
            EditProfileScreen(
                viewModel = viewModel,
                onDismiss = { viewModel.showEditProfile(false) }
            )
        }
    }
}

@Composable
private fun ProfileHeader(profile: com.example.myapplication.DTOs.Profile, viewModel: EnhancedProfileViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Favorite Game Background (blurred)
        if (profile.favoriteGame != null) {
            AsyncImage(
                model = profile.favoriteGame.coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ),
                    RoundedCornerShape(16.dp)
                )
                .border(2.dp, PixelBlack, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture with Favorite Game Frame
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                if (profile.favoriteGame != null) {
                    AsyncImage(
                        model = profile.favoriteGame.coverUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(3.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(12.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Name
            Text(
                text = profile.name ?: "User",
                fontFamily = PressStart,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Email
            Text(
                text = profile.email ?: "",
                fontFamily = PressStart,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            // Favorite Game
            if (profile.favoriteGame != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = profile.favoriteGame.name,
                        fontFamily = PressStart,
                        fontSize = 10.sp,
                        color = Color(0xFFFFD700)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Edit Button
            PixelatedButton(
                onClick = { viewModel.showEditProfile(true) },
                containerColor = PixelBlue
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("EDIT PROFILE", fontFamily = PressStart, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun BioSection(bio: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "ABOUT ME",
            fontFamily = PressStart,
            fontSize = 14.sp,
            color = Color(0xFFFFD700)
        )
        Spacer(modifier = Modifier.height(8.dp))
        PixelatedCard {
            Text(
                text = bio,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun AchievementsSection(achievements: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "ACHIEVEMENTS",
            fontFamily = PressStart,
            fontSize = 14.sp,
            color = Color(0xFFFFD700)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(achievements) { achievementId ->
                AchievementBadge(achievementId = achievementId, isUnlocked = true)
            }
            // Show some locked achievements
            items(listOf("collector", "speedrunner", "social").filter { it !in achievements }) { achievementId ->
                AchievementBadge(achievementId = achievementId, isUnlocked = false)
            }
        }
    }
}

@Composable
private fun StatsGrid(viewModel: EnhancedProfileViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "GAMING STATS",
            fontFamily = PressStart,
            fontSize = 14.sp,
            color = Color(0xFFFFD700)
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                icon = Icons.Default.Gamepad,
                title = "Played",
                value = "${viewModel.totalGamesPlayed}",
                color = PixelGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.PlayArrow,
                title = "Playing",
                value = "0",
                color = PixelBlue,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.Favorite,
                title = "Wishlist",
                value = "0",
                color = PixelRed,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                icon = Icons.Default.CheckCircle,
                title = "Complete",
                value = "0",
                color = Color(0xFFFFD700),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.Schedule,
                title = "Hours",
                value = "${viewModel.totalHoursPlayed}",
                color = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.Star,
                title = "Avg Rating",
                value = String.format("%.1f", viewModel.averageRating),
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RecentActivitySection(activities: List<com.example.myapplication.model.RecentActivity>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "RECENT ACTIVITY",
            fontFamily = PressStart,
            fontSize = 14.sp,
            color = Color(0xFFFFD700)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            activities.take(5).forEach { activity ->
                ActivityRow(activity = activity)
            }
        }
    }
}

@Composable
private fun GamerDNASection(profile: com.example.myapplication.DTOs.Profile) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Gamer Tags
        if (profile.gamerTags != null) {
            Text(
                text = "GAMER TAGS",
                fontFamily = PressStart,
                fontSize = 14.sp,
                color = Color(0xFFFFD700)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                profile.gamerTags.psn?.let {
                    GamerTagRow(platform = "PSN", username = it, icon = Icons.Default.Gamepad)
                }
                profile.gamerTags.xbox?.let {
                    GamerTagRow(platform = "Xbox", username = it, icon = Icons.Default.Gamepad)
                }
                profile.gamerTags.steam?.let {
                    GamerTagRow(platform = "Steam", username = it, icon = Icons.Default.Computer)
                }
                profile.gamerTags.discord?.let {
                    GamerTagRow(platform = "Discord", username = it, icon = Icons.Default.Chat)
                }
                profile.gamerTags.nintendo?.let {
                    GamerTagRow(platform = "Nintendo", username = it, icon = Icons.Default.Gamepad)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Play Styles
        if (!profile.playStyles.isNullOrEmpty()) {
            Text(
                text = "PLAY STYLES",
                fontFamily = PressStart,
                fontSize = 14.sp,
                color = Color(0xFFFFD700)
            )
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                profile.playStyles.forEach { style ->
                    PlayStyleChip(style = style, isSelected = true, onClick = {})
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Languages
        if (!profile.languages.isNullOrEmpty()) {
            Text(
                text = "LANGUAGES",
                fontFamily = PressStart,
                fontSize = 14.sp,
                color = Color(0xFFFFD700)
            )
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                profile.languages.forEach { language ->
                    LanguageChip(language = language, isSelected = true, onClick = {})
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Availability
        if (!profile.availability.isNullOrBlank()) {
            Text(
                text = "AVAILABILITY",
                fontFamily = PressStart,
                fontSize = 14.sp,
                color = Color(0xFFFFD700)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = profile.availability,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun DarkModeBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    )
}

@Composable
private fun LightModeBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.myapplication.R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        
        // Seamless Animated Clouds (iOS style)
        com.example.myapplication.ui.components.AnimatedClouds()
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Use fully qualified name to avoid recursion
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
