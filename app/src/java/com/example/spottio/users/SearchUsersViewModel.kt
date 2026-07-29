package com.example.spottio.users

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchUsersViewModel(private val repository: UserRepository) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<String>>(emptyList())
    val searchResults: StateFlow<List<String>> = _searchResults.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onQueryChanged(newQuery: String, currentUserUid: String) {
        _searchQuery.value = newQuery
        if (newQuery.length < 4) {
            _searchResults.value = emptyList()
        } else {
            searchUsers(newQuery, currentUserUid)
        }
    }

    private fun searchUsers(query: String, currentUserUid: String) {
        repository.searchUsers(
            query = query,
            currentUserUid = currentUserUid,
            onSuccess = { uids ->
                _searchResults.value = uids
            },
            onError = { e ->
                _error.value = "Errore ricerca utenti: ${e.message}"
            }
        )
    }

    fun clearError() {
        _error.value = null
    }

    fun clearResults() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }
}
