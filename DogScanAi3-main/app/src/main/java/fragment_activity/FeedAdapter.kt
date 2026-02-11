package fragment_activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firstapp.dogscanai.R

// A simple adapter that just creates 10 dummy posts
class FeedAdapter : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    // This creates a new view holder when the RecyclerView needs one.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.feed_item, parent, false)
        return FeedViewHolder(view)
    }

    // This returns the total number of items in the list. We'll hardcode 10 for now.
    override fun getItemCount(): Int {
        return 10
    }

    // This binds the data to the view holder for a specific position.
    // Since we have no data, we do nothing here for this example.
    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        // You would set user name, caption, images etc. here
    }

    // This class holds the views for a single item in the list.
    class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // You would find your TextViews and ImageViews here, e.g.:
        // val userName: TextView = itemView.findViewById(R.id.user_name)
    }
}