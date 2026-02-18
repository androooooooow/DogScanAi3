package fragment_activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
            itemImage.setImageResource(item.imageRes)

            // Dito gumagana ang clickable item
            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount() = items.size

    fun filterList(newList: List<SearchItem>) {
        items = newList
        notifyDataSetChanged()
    }
}