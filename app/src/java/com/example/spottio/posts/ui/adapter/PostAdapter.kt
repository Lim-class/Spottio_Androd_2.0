package com.example.spottio.posts.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.RecyclerView
import com.example.spottio.posts.data.Post
import com.example.spottio.posts.ui.screen.PostItem

class PostAdapter(
    private var postList: List<Post>,
    private val currentUser: String,
    private val isAdmin: Boolean,
    private val interactionListener: PostInteractionListener
) : RecyclerView.Adapter<PostAdapter.ComposeViewHolder>() {

    interface PostInteractionListener {
        fun onUserClick(userId: String)
        fun onEditClick(post: Post, position: Int)
        fun onCommentClick(post: Post, position: Int)
        fun onReportClick(post: Post)
        fun onShareClick(post: Post, shareContent: String)
        fun onImageZoomClick(imageView: View, mediaUri: String) // Mantenuta per retrocompatibilità
        fun onLikeClick(post: Post, position: Int)
        fun onDeleteClick(post: Post, position: Int)
    }

    fun setPostList(postList: List<Post>) {
        this.postList = postList
        notifyDataSetChanged()
    }

    // Usiamo il ComposeViewHolder per contenere la ComposeView
    class ComposeViewHolder(val composeView: ComposeView) : RecyclerView.ViewHolder(composeView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComposeViewHolder {
        val composeView = ComposeView(parent.context).apply {
            // Ottimizza il view pooling per Compose in RecyclerView
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return ComposeViewHolder(composeView)
    }

    override fun onBindViewHolder(holder: ComposeViewHolder, position: Int) {
        val post = postList[position]

        holder.composeView.setContent {
            // Chiamiamo il nostro nuovo Composable
            PostItem(
                post = post,
                currentUser = currentUser,
                isAdmin = isAdmin,
                position = position,
                interactionListener = interactionListener
            )
        }
    }

    override fun getItemCount(): Int = postList.size
}