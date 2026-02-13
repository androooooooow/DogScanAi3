package com.firstapp.dogscanai.fragment_activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firstapp.dogscanai.R

class FeedAdapter : RecyclerView.Adapter<FeedAdapter.ViewHolder>() {

    // Updated sample data
    private val feedItems = listOf(
        FeedItem("Andrew Johnson", "11/02/2026", "13 Likes", R.drawable.husky),
        FeedItem("Dog Lover", "10/02/2026", "10 Likes", R.drawable.husky),
        FeedItem("Canine Friend", "09/02/2026", "8 Likes", R.drawable.husky),
        FeedItem("Pet Owner", "08/02/2026", "15 Likes", R.drawable.husky),
        FeedItem("Animal Rescue", "07/02/2026", "20 Likes", R.drawable.husky)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.feed_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = feedItems[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = feedItems.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.tv_user_name)
        private val postDate: TextView = itemView.findViewById(R.id.tv_date)
        private val likesCount: TextView = itemView.findViewById(R.id.tv_likes)
        private val userImage: ImageView = itemView.findViewById(R.id.iv_user_image)
        private val postImage: ImageView = itemView.findViewById(R.id.iv_post_image)

        fun bind(feedItem: FeedItem) {
            userName.text = feedItem.userName
            postDate.text = feedItem.date
            likesCount.text = feedItem.likes

            // Set images
            userImage.setImageResource(feedItem.imageResId)
            postImage.setImageResource(feedItem.imageResId)
        }
    }

    data class FeedItem(
        val userName: String,
        val date: String,
        val likes: String,
        val imageResId: Int
    )
}