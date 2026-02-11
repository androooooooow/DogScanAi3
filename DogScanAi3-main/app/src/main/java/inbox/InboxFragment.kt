package inbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firstapp.dogscanai.R

class InboxFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the CORRECT layout for the whole screen
        val view = inflater.inflate(R.layout.fragment_inbox, container, false)

        // Find the RecyclerView within this fragment's view
        val inboxRecyclerView: RecyclerView = view.findViewById(R.id.inbox_recycler_view)

        // Set up the RecyclerView - VARIABLE NAMES ARE NOW CORRECT
        inboxRecyclerView.layoutManager = LinearLayoutManager(context)
        inboxRecyclerView.adapter = InboxAdapter() // Use the adapter you created

        return view
    }
}