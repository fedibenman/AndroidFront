package com.example.myapplication.ui.teammate

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.Repository.NearbyUser
import com.example.myapplication.ui.components.ThemedBackground
import com.example.myapplication.ui.theme.PressStart
import com.example.myapplication.ui.theme.PrimaryGold
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyTeammatesScreen(
    onBack: () -> Unit,
    onUserTap: (String) -> Unit = {},
    viewModel: NearbyTeammatesViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.checkLocationPermission(context)
        if (isGranted) {
            viewModel.updateLocation(context)
        }
    }

    LaunchedEffect(viewModel.userId) {
        viewModel.checkLocationPermission(context)
        if (viewModel.hasLocationPermission) {
            viewModel.updateLocation(context)
        }
        // Socket connection is now handled globally in MainScreen
    }

    // Notifications are now handled globally in MainScreen

    // Convert nearby users to radar dots
    val radarDots = remember(viewModel.nearbyUsers, viewModel.searchRange) {
        viewModel.nearbyUsers.map { user ->
            val distanceRatio = (user.distance.toFloat() / viewModel.searchRange).coerceIn(0f, 1f)
            val angle = Random.nextFloat() * 360f
            val (x, y) = polarToNormalized(distanceRatio, angle)
            RadarDot(
                normalizedX = x,
                normalizedY = y,
                color = PrimaryGold,
                userId = user.id,
                userName = user.name,
                distance = user.distance
            )
        }
    }

    // Bottom Sheet State
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    var selectedUser by remember { mutableStateOf<NearbyUser?>(null) }
    var showFullProfile by remember { mutableStateOf(false) }

    fun showUserCard(userId: String) {
        val user = viewModel.nearbyUsers.find { it.id == userId }
        if (user != null) {
            selectedUser = user
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ThemedBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = PrimaryGold
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "NEARBY GAMERS",
                    fontFamily = PressStart,
                    fontSize = 16.sp,
                    color = PrimaryGold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Location Permission Check
            if (!viewModel.hasLocationPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PrimaryGold,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "LOCATION REQUIRED",
                            fontFamily = PressStart,
                            fontSize = 12.sp,
                            color = PrimaryGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Enable location to find nearby gamers",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                        ) {
                            Text("ENABLE LOCATION", fontFamily = PressStart, fontSize = 10.sp)
                        }
                    }
                }
            } else {
                // Range Slider
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "RANGE",
                                fontFamily = PressStart,
                                fontSize = 10.sp,
                                color = PrimaryGold
                            )
                            Text(
                                text = "${viewModel.searchRange} km",
                                fontFamily = PressStart,
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                        
                        Slider(
                            value = viewModel.searchRange.toFloat(),
                            onValueChange = { viewModel.updateSearchRange(it.toInt()) },
                            valueRange = 5f..100f,
                            steps = 19,
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryGold,
                                activeTrackColor = PrimaryGold,
                                inactiveTrackColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Game Filter
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(1f) // Ensure dropdown is on top
                ) {
                    Text(
                        text = "FILTER BY GAME:",
                        fontFamily = PressStart,
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Relative layout for dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = viewModel.gameSearchQuery,
                            onValueChange = { viewModel.updateGameSearchQuery(it) },
                            placeholder = { 
                                Text("Type to search game...", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f)) 
                            },
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 14.sp,
                                color = Color.White
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                cursorColor = PrimaryGold
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                if (viewModel.gameSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { 
                                        viewModel.updateGameSearchQuery("")
                                        viewModel.clearGameFilter()
                                    }) {
                                        Icon(
                                            Icons.Default.Close, 
                                            contentDescription = "Clear",
                                            tint = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                } else {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        )

                        // Autocomplete Dropdown
                        if (viewModel.matchingGames.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 60.dp) // Offset below TextField
                                    .heightIn(max = 200.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                LazyColumn {
                                    items(viewModel.matchingGames) { (name, id) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { viewModel.selectGame(name, id) }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = PrimaryGold,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = name,
                                                color = Color.White,
                                                fontSize = 14.sp
                                            )
                                        }
                                        Divider(color = Color.White.copy(alpha = 0.1f))
                                    }
                                }
                            }
                        }
                    }

                    // Show current active filter
                    if (viewModel.selectedGameName != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Filtering: ",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = viewModel.selectedGameName ?: "",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Radar Animation
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RadarAnimation(
                        modifier = Modifier.fillMaxSize(),
                        isSearching = viewModel.isSearching,
                        userDots = radarDots,
                        range = viewModel.searchRange,
                        onUserTapped = { dot ->
                            showUserCard(dot.userId)
                        }
                    )
                }

                // Nearby Users List
                if (viewModel.nearbyUsers.isNotEmpty()) {
                    Text(
                        text = "FOUND ${viewModel.nearbyUsers.size} GAMERS",
                        fontFamily = PressStart,
                        fontSize = 10.sp,
                        color = PrimaryGold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.nearbyUsers) { user ->
                            NearbyUserCard(user = user, onClick = { showUserCard(user.id) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Start/Stop Button
                Button(
                    onClick = {
                        if (viewModel.isSearching) {
                            viewModel.stopSearching()
                        } else {
                            viewModel.updateLocation(context)
                            viewModel.startSearching()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.isSearching) Color.Red else PrimaryGold
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        if (viewModel.isSearching) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (viewModel.isSearching) "STOP SEARCHING" else "START SEARCHING",
                        fontFamily = PressStart,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Bottom Sheet (M3)
        if (selectedUser != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedUser = null },
                sheetState = sheetState,
                containerColor = Color(0xFF2C2C2C)
            ) {
                selectedUser?.let { user ->
                    GamerCard(
                        user = user,
                        onProfileClick = {
                            showFullProfile = true
                        },
                        onInviteClick = {
                            showFullProfile = true
                        }
                    )
                }
                Spacer(Modifier.height(50.dp)) // Extra padding
            }
        }

        if (showFullProfile && selectedUser != null) {
            UserProfileDialog(
                userId = selectedUser!!.id,
                initialName = selectedUser!!.name,
                initialAvatar = selectedUser!!.avatar,
                onDismiss = { showFullProfile = false },
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun GamerCard(
    user: NearbyUser,
    onProfileClick: () -> Unit,
    onInviteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column {
                Text(
                    text = user.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${user.distance} km away",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryGold, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (user.favoriteGame != null) "Looking for ${user.favoriteGame.name}" else "Online",
                fontFamily = PressStart,
                fontSize = 10.sp,
                color = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onProfileClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View Profile", color = Color.White)
            }
            
            Button(
                onClick = onInviteClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Invite", color = Color.Black)
            }
        }
    }
}

@Composable
fun NearbyUserCard(
    user: NearbyUser,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryGold),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontFamily = PressStart,
                    fontSize = 10.sp,
                    color = Color.White
                )
                
                user.favoriteGame?.let { game ->
                    Text(
                        text = "Playing: ${game.name}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }
            
            Text(
                text = "${user.distance} km",
                fontFamily = PressStart,
                fontSize = 10.sp,
                color = PrimaryGold
            )
        }
    }
}
