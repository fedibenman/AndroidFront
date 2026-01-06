package com.example.myapplication.community.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.directmessages.model.User
import com.example.myapplication.directmessages.data.DirectMessagesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val repository = DirectMessagesRepository.getInstance()
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    fun loadUsers() {
        viewModelScope.launch {
            _users.value = repository.getAllUsers()
        }
    }
    
    fun searchUsers(query: String) {
        viewModelScope.launch {
            val allUsers: Iterable<User> = repository.getAllUsers()
            _users.value = allUsers.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.email.contains(query, ignoreCase = true) 
            }
        }
    }
}
