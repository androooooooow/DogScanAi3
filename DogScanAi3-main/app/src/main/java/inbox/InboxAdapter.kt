package inbox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firstapp.dogscanai.R

// A simple adapter that just creates 15 dummy inbox items
class InboxAdapter : RecyclerView.Adapter<InboxAdapter.InboxViewHolder>() {

    class InboxViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inbox_message, parent, false)
        return InboxViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 15 // Display 15 dummy rows
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        // In a real app, you would set the sender name, message, etc. here
    }
}