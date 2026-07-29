package com.example.spottio.users.presentation.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.spottio.R
import com.example.spottio.profile.ui.ProfileFragment
import com.example.spottio.users.presentation.components.UserListScreen
import com.example.spottio.users.data.repository.UserRepository
import com.example.spottio.utils.GenericViewModelFactory

class SearchUsersFragment : Fragment() {

    private var currentUserUid: String = "Guest"
    private lateinit var viewModel: SearchUsersViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val prefs = requireActivity().getSharedPreferences("SpottioPrefs", Context.MODE_PRIVATE)
        currentUserUid = prefs.getString("uid_attivo", "Guest") ?: "Guest"

        val factory = GenericViewModelFactory { SearchUsersViewModel(UserRepository()) }
        viewModel = ViewModelProvider(this, factory)[SearchUsersViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    val searchResults by viewModel.searchResults.collectAsState()
                    val searchQuery by viewModel.searchQuery.collectAsState()
                    val errorMsg by viewModel.error.collectAsState()

                    errorMsg?.let { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }

                    UserListScreen(
                        title = "Utenti",
                        searchQuery = searchQuery,
                        onQueryChange = { query ->
                            viewModel.onQueryChanged(query, currentUserUid)
                        },
                        showSearch = true,
                        users = searchResults,
                        onUserClick = { selectedUid ->
                            parentFragmentManager.beginTransaction()
                                .replace(
                                    R.id.fragment_container,
                                    ProfileFragment.newInstance(selectedUid)
                                )
                                .addToBackStack(null)
                                .commit()
                        }
                    )
                }
            }
        }
    }
}