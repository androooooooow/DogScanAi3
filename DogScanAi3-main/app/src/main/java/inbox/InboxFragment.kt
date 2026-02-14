package inbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firstapp.dogscanai.R

class InboxFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // I-inflate ang layout para sa fragment na ito
        val view = inflater.inflate(R.layout.fragment_inbox, container, false)

        // 1. Setup Back Button
        val btnBack: ImageView = view.findViewById(R.id.btn_back_inbox)
        btnBack.setOnClickListener {
            // Babalik sa nakaraang fragment (HomeFragment) na nása back stack
            parentFragmentManager.popBackStack()
        }

        // 2. Find and setup the RecyclerView
        val inboxRecyclerView: RecyclerView = view.findViewById(R.id.inbox_recycler_view)
        inboxRecyclerView.layoutManager = LinearLayoutManager(context)

        try {
            // Siguraduhing mayroon kang InboxAdapter class sa iyong 'inbox' package
            inboxRecyclerView.adapter = InboxAdapter()
        } catch (e: Exception) {
            // Fallback kung hindi pa nása-setup ang adapter
        }

        return view
    }
}