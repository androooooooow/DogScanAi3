package fragment_activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firstapp.dogscanai.R // Siguraduhin na tama ang package name mo rito

data class SearchItem(val id: Int, val title: String, val description: String, val imageRes: Int)

class SearchAdapter(private var items: List<SearchItem>) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.itemImage)
        val txtTitle: TextView = view.findViewById(R.id.itemTitle)
        val txtDesc: TextView = view.findViewById(R.id.itemDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Gagamitin natin yung card layout na ginawa natin kanina
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.txtTitle.text = item.title
        holder.txtDesc.text = item.description
        holder.img.setImageResource(item.imageRes)
    }

    override fun getItemCount() = items.size

    // Function para sa real-time search filtering
    fun filterList(filteredNames: List<SearchItem>) {
        this.items = filteredNames
        notifyDataSetChanged()
    }
}