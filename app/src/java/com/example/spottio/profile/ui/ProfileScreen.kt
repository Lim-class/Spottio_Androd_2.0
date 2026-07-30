package com.example.spottio.profile.ui

import android.app.Activity
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spottio.R
import com.example.spottio.feed.ui.adapter.PostAdapter
import com.example.spottio.profile.presentation.ProfileCoordinator
import com.example.spottio.profile.presentation.ProfileViewModel
import com.example.spottio.profile.ui.components.ProfileUIManager
import com.example.spottio.utils.language.LanguageSelectorManager

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    uiManager: ProfileUIManager,
    coordinator: ProfileCoordinator,
    currentUser: String,
    targetUser: String,
    userIsAdmin: Boolean,
    profilePostListener: PostAdapter.PostInteractionListener
) {
    val context = LocalContext.current
    val isMyProfile = coordinator.isMyProfile()

    // Stati osservati dal ViewModel
    val username by viewModel.username.observeAsState("Caricamento...")
    val bio by viewModel.bio.observeAsState("Nessuna bio inserita")
    val followersCount by viewModel.followersCount.observeAsState(0)
    val followingCount by viewModel.followingCount.observeAsState(0)
    val isFollowing by viewModel.isFollowing.observeAsState(false)
    val profileImageUrl by viewModel.profileImageUrl.observeAsState("")
    val isVerified by viewModel.isVerified.observeAsState(false)
    val userPosts by viewModel.userPosts.observeAsState(emptyList())
    val followersList by viewModel.followersList.observeAsState(emptyList())
    val followingList by viewModel.followingList.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // --- HEADER PROFILO (Foto, Nome, Bio, Settings) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Immagine Profilo usando Glide tramite AndroidView per perfetta retrocompatibilità
            AndroidView(
                factory = { ctx ->
                    ImageView(ctx).apply {
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            (80 * ctx.resources.displayMetrics.density).toInt(),
                            (80 * ctx.resources.displayMetrics.density).toInt()
                        )
                        setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"))
                    }
                },
                update = { imageView ->
                    uiManager.loadProfileImage(profileImageUrl, imageView)
                },
                modifier = Modifier
                    .size(80.dp)
                    .clickable(enabled = isMyProfile) {
                        uiManager.showEditProfileImageDialog(profileImageUrl) { newUrl ->
                            viewModel.updateProfileImageUrl(currentUser, newUrl)
                        }
                    }
            )

            // Dati utente
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                // Riga Nome + Spunta + Edit
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = username,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )

                    if (isVerified) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_verified_blue),
                            contentDescription = "Verificato",
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(20.dp)
                        )
                    }

                    if (isMyProfile) {
                        IconButton(
                            onClick = {
                                uiManager.showEditUsernameDialog(username) { newName ->
                                    viewModel.updateUsername(currentUser, newName)
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_edit),
                                contentDescription = "Modifica username",
                                tint = Color.Gray
                            )
                        }
                    }
                }

                // Riga Bio + Edit
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = bio.ifEmpty { "Nessuna bio inserita" },
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.weight(1f)
                    )

                    if (isMyProfile) {
                        IconButton(
                            onClick = {
                                uiManager.showEditBioDialog(bio) { newBio ->
                                    viewModel.updateBio(currentUser, newBio)
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_edit),
                                contentDescription = "Modifica bio",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }

            // Pulsante Impostazioni (solo mio profilo)
            if (isMyProfile) {
                IconButton(onClick = { coordinator.navigateToSettings() }) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_preferences),
                        contentDescription = "Impostazioni"
                    )
                }
            }
        }

        // --- SEZIONE FOLLOWERS / FOLLOWING ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { coordinator.navigateToFollowers(followersList) }
                    .padding(8.dp)
            ) {
                Text(text = followersCount.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = stringResource(id = R.string.label_followers), fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.width(32.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { coordinator.navigateToFollowing(followingList) }
                    .padding(8.dp)
            ) {
                Text(text = followingCount.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = stringResource(id = R.string.label_following), fontSize = 14.sp)
            }
        }

        // --- PULSANTI AZIONE ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isMyProfile) {
                Button(
                    onClick = { uiManager.performLogout(context as Activity) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Logout", color = Color.White)
                }

                // Language Selector tramite AndroidView per non rompere la logica esistente
                AndroidView(
                    factory = { ctx ->
                        val view = LayoutInflater.from(ctx).inflate(R.layout.layout_language_selector, null)
                        val langManager = LanguageSelectorManager(ctx, view)
                        langManager.init(object : LanguageSelectorManager.LanguageSelectionListener {
                            override fun onLanguageSelected(code: String, flag: String, name: String) {
                                coordinator.changeLanguage(code, flag, name)
                            }
                        })
                        view
                    },
                    modifier = Modifier.weight(1f)
                )

            } else {
                Button(
                    onClick = { viewModel.toggleFollow(targetUser, currentUser, isFollowing) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowing) Color.LightGray else Color(0xFF002D57)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isFollowing) stringResource(id = R.string.btn_following) else stringResource(id = R.string.btn_follow_action),
                        color = if (isFollowing) Color.Black else Color.White
                    )
                }

                Button(
                    onClick = { coordinator.navigateToChat(username) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Chat")
                }
            }
        }

        // --- TASTO ADMIN (solo se admin e mio profilo) ---
        if (userIsAdmin && isMyProfile) {
            Button(
                onClick = { coordinator.navigateToAdminReports() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(text = stringResource(id = R.string.btn_manage_reports))
            }
        }

        // --- LISTA POST RECENTI ---
        Text(
            text = stringResource(id = R.string.label_recent_posts),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
        )

        // --- RECYCLERVIEW CONFINATA NELLA SUA AREA ---
        AndroidView(
            factory = { ctx ->
                RecyclerView(ctx).apply {
                    // FIX 1: Diamo confini rigidi nativi alla RecyclerView in modo che non strabordi
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    layoutManager = LinearLayoutManager(ctx)
                    adapter = PostAdapter(userPosts, currentUser, userIsAdmin, profilePostListener)
                }
            },
            update = { recyclerView ->
                val currentAdapter = recyclerView.adapter as? PostAdapter
                if (currentAdapter == null) {
                    recyclerView.adapter = PostAdapter(userPosts, currentUser, userIsAdmin, profilePostListener)
                } else {
                    currentAdapter.setPostList(userPosts)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds() // FIX 2: Taglia via graficamente qualsiasi cosa provi a uscire dall'area!
        )
    }
}