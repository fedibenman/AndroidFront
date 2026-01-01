package com.example.myapplication.directmessages.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.directmessages.model.User
import com.example.myapplication.community.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSearchScreen(
    onUserClick: (User) -> Unit,
    onNavigateBack: () -> Unit
) {
    // We can use a ViewModel to search/list users. 
    // Assuming we have a UserViewModel or similar. 
    // For now we will use a placeholder or assume UserViewModel exists.
    // If not we might need to use DirectMessagesViewModel if it has search capabilities.
    
    // Let's assume we fetch all users for now using UserViewModel (if exists)
    // or we create a simple search feature.
    
    // As per previous context, let's use a mock or basic implementation 
    // integrating with Comunity feature's User model
    
    val userViewModel: UserViewModel = viewModel()
    val users by userViewModel.users.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        userViewModel.loadUsers()
    }

    val filteredUsers = users.toList().filter { 
        it.name?.contains(searchQuery, ignoreCase = true) == true || 
        it.email?.contains(searchQuery, ignoreCase = true) == true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Message") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search users...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            LazyColumn {
                items(filteredUsers) { user ->
                    ListItem(
                        headlineContent = { Text(user.name ?: "Unknown") },
                        supportingContent = { Text(user.email ?: "") },
                        leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.clickable { onUserClick(user) }
                    )
                }
            }
        }
    }
}
