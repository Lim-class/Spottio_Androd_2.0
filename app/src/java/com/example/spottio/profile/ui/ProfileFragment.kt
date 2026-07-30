package com.example.spottio.profile.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ablanco.zoomy.Zoomy
import com.example.spottio.auth.AuthManager
import com.example.spottio.feed.data.Comment
import com.example.spottio.utils.InteractionTracker
import com.example.spottio.posts.shared.data.Post
import com.example.spottio.feed.ui.adapter.PostAdapter
import com.example.spottio.feed.ui.dialogs.PostDialogHelper
import com.example.spottio.posts.shared.repository.PostRepository
import com.example.spottio.profile.ui.components.ProfileUIManager
import com.example.spottio.profile.presentation.ProfileCoordinator
import com.example.spottio.profile.presentation.ProfileViewModel
import com.example.spottio.reports.Report

class ProfileFragment : Fragment() {

    private var targetUser: String? = null
    private lateinit var currentUser: String

    private lateinit var viewModel: ProfileViewModel
    private lateinit var uiManager: ProfileUIManager
    private lateinit var coordinator: ProfileCoordinator

    companion object {
        @JvmStatic
        fun newInstance(targetUser: String): ProfileFragment {
            return ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString("ARG_TARGET_USER", targetUser)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        targetUser = arguments?.getString("ARG_TARGET_USER")
        currentUser = AuthManager.getCurrentUserUid(requireContext())
        if (targetUser.isNullOrEmpty()) {
            targetUser = currentUser
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        uiManager = ProfileUIManager(requireContext())
        coordinator = ProfileCoordinator(this, currentUser, targetUser)
        val userIsAdmin = AuthManager.isCurrentUserAdmin(requireContext())

        // Listener del PostAdapter
        val profilePostListener = object : PostAdapter.PostInteractionListener {
            val postRepository = PostRepository()

            override fun onUserClick(userId: String) {}

            override fun onLikeClick(post: Post, position: Int) {
                val wasLiked = post.likes.contains(currentUser)
                post.toggleLike(currentUser)
                post.postId?.let {
                    postRepository.toggleLike(it, post.likes)
                    if (!wasLiked) {
                        post.category?.let { cat -> InteractionTracker.trackInteraction(currentUser, cat) }
                    }
                }
            }

            override fun onDeleteClick(post: Post, position: Int) {
                PostDialogHelper.showDeletePostDialog(requireContext()) {
                    post.postId?.let {
                        postRepository.deletePost(it) {
                            Toast.makeText(context, "Post eliminato", Toast.LENGTH_SHORT).show()
                            viewModel.loadUserProfile(targetUser!!, currentUser)
                        }
                    }
                }
            }

            override fun onEditClick(post: Post, position: Int) {
                PostDialogHelper.showEditPostDialog(requireContext(), post.text) { newText ->
                    post.postId?.let {
                        postRepository.updatePostText(it, newText) {
                            post.text = newText
                            Toast.makeText(context, "Post modificato", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onCommentClick(post: Post, position: Int) {
                PostDialogHelper.showCommentsSheet(requireContext(), post.comments) { commentText ->
                    val newComment = Comment(currentUser, commentText)
                    post.addComment(newComment)
                    post.postId?.let {
                        postRepository.updateComments(it, post.comments) {}
                    }
                }
            }

            override fun onReportClick(post: Post) {
                PostDialogHelper.showReportDialog(requireContext()) { reason, desc ->
                    val report = Report(post.postId ?: "", post.user, currentUser, reason, desc, post.text ?: "")
                    postRepository.submitReport(report,
                        onSuccess = { Toast.makeText(context, "Segnalazione inviata", Toast.LENGTH_SHORT).show() },
                        onFailure = { Toast.makeText(context, "Errore invio segnalazione", Toast.LENGTH_SHORT).show() }
                    )
                }
            }

            override fun onShareClick(post: Post, shareContent: String) {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, shareContent)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(sendIntent, "Condividi post tramite:"))
            }

            override fun onImageZoomClick(imageView: View, mediaUri: String) {
                Zoomy.Builder(requireActivity())
                    .target(imageView)
                    .enableImmersiveMode(false)
                    .animateZooming(true)
                    .register()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        viewModel.loadUserProfile(targetUser!!, currentUser)

        return ComposeView(requireContext()).apply {
            setContent {
                ProfileScreen(
                    viewModel = viewModel,
                    uiManager = uiManager,
                    coordinator = coordinator,
                    currentUser = currentUser,
                    targetUser = targetUser!!,
                    userIsAdmin = userIsAdmin,
                    profilePostListener = profilePostListener
                )
            }
        }
    }
}