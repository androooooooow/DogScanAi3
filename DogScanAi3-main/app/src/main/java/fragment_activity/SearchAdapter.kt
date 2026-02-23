package fragment_activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firstapp.dogscanai.R
import com.firstapp.dogscanai.databinding.ItemSearchBinding

class SearchAdapter(
    private var items: List<SearchItem>,
    private val onItemClick: (SearchItem) -> Unit
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSearchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            itemTitle.text = item.title
            itemDescription.text = item.description

            // If imageUrl is available (from database), use Glide to load it
            // Otherwise, fall back to the local drawable resource
            if (!item.imageUrl.isNullOrEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.aspin) // shown while loading
                    .error(R.drawable.aspin)        // shown if load fails
                    .centerCrop()
                    .into(itemImage)
            } else {
                itemImage.setImageResource(item.imageRes)
            }

            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount() = items.size

    fun filterList(newList: List<SearchItem>) {
        items = newList
        notifyDataSetChanged()
    }
}