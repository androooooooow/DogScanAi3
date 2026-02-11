package fragment_activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fragment_activity.FeedAdapter
import com.firstapp.dogscanai.R

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val feedRecyclerView: RecyclerView = view.findViewById(R.id.feeds_recycler_view)
        feedRecyclerView.layoutManager = LinearLayoutManager(context)
        feedRecyclerView.adapter = FeedAdapter()

        return view
    }
}