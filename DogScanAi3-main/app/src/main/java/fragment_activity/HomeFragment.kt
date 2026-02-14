package com.firstapp.dogscanai.fragment_activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dogscanai.utils.SessionManager //
import com.firstapp.dogscanai.R //
import inbox.InboxFragment // Siguraduhing tama ang package path nito

class HomeFragment : Fragment() {

    private lateinit var sessionManager: SessionManager //

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // I-inflate ang layout para sa fragment na ito
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("HomeFragment", "=== HOME FRAGMENT STARTED ===") //

        // 1. Initialize SessionManager
        sessionManager = SessionManager(requireContext())

        // 2. I-setup ang Username Display
        val tvUsername: TextView = view.findViewById(R.id.tv_username)
        val user = sessionManager.getUser() //

        // I-display ang pangalan mula sa database, fallback ang "User" kung wala
        tvUsername.text = user?.name ?: "User"

        // 3. Setup Notification Button Click
        val btnNotifications: ImageView = view.findViewById(R.id.btn_notifications)
        btnNotifications.setOnClickListener {
            navigateToInbox()
        }

        // 4. Setup RecyclerView para sa Feed
        setupRecyclerView(view)
    }

    private fun setupRecyclerView(view: View) {
        val feedRecyclerView: RecyclerView = view.findViewById(R.id.feed_recycler_view) //

        // Siguraduhin na ang layout manager ay naka-set
        feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        try {
            val adapter = FeedAdapter() //
            feedRecyclerView.adapter = adapter
            Log.d("HomeFragment", "Adapter created.") //
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error: ${e.message}") //
        }
    }

    private fun navigateToInbox() {
        val inboxFragment = InboxFragment()

        // Gagamit tayo ng requireActivity().supportFragmentManager para mahanap ang ID sa Dashboard
        requireActivity().supportFragmentManager.beginTransaction().apply {
            // Animation para smooth ang transition
            setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            // pinalitan natin ng 'fragmentContainer' para mag-match sa activity_dashboard.xml mo
            replace(R.id.fragmentContainer, inboxFragment)
            addToBackStack(null) // Para makabalik sa Home gamit ang back button
            commit()
        }

        Log.d("HomeFragment", "Navigating to InboxFragment...")
    }
}