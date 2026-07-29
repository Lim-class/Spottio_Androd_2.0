package com.example.spottio.users.presentation.connections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.spottio.R
import com.example.spottio.profile.ui.ProfileFragment
import com.example.spottio.users.presentation.components.UserListScreen

class UserConnectionsFragment : Fragment() {

    companion object {
        fun newInstance(title: String, users: ArrayList<String>) = UserConnectionsFragment().apply {
            arguments = Bundle().apply {
                putString("TITLE", title)
                putStringArrayList("USER_LIST", users)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val title = arguments?.getString("TITLE") ?: "Connessioni"
        val userList = arguments?.getStringArrayList("USER_LIST") ?: arrayListOf()

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    UserListScreen(
                        title = title,
                        showSearch = false,
                        users = userList,
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