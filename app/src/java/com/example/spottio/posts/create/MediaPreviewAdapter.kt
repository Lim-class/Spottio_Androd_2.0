package com.example.spottio.posts.create

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.spottio.R

class MediaPreviewAdapter(private val mediaUris: List<Uri>) : RecyclerView.Adapter<MediaPreviewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPreview: ImageView = view.findViewById(R.id.ivPreviewItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uri = mediaUris[position]
        Glide.with(holder.itemView.context).load(uri).into(holder.ivPreview)
    }

    override fun getItemCount() = mediaUris.size
}