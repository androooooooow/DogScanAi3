package com.firstapp.dogscanai.fragment_activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dogscanai.utils.SessionManager // Siguraduhing naka-import ito
import com.firstapp.dogscanai.R

class HomeFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("HomeFragment", "=== HOME FRAGMENT STARTED ===")

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 1. I-initialize ang SessionManager
        sessionManager = SessionManager(requireContext())

        // 2. Hanapin ang TextView para sa pangalan
        val tvUsername: TextView = view.findViewById(R.id.tv_username)

        // 3. Kunin ang user details mula sa session
        val user = sessionManager.getUser()
        if (user != null) {
            // I-display ang pangalan mula sa database/signup
            tvUsername.text = user.name
        } else {
            tvUsername.text = "User" // Fallback kung walang mahanap
        }

        // --- RECYCLERVIEW SETUP ---
        val feedRecyclerView: RecyclerView = view.findViewById(R.id.feed_recycler_view)

        if (feedRecyclerView == null) {
            Log.e("HomeFragment", "ERROR: RecyclerView not found!")
            return view
        }

        val layoutManager = LinearLayoutManager(requireContext())
        feedRecyclerView.layoutManager = layoutManager

        try {
            val adapter = FeedAdapter()
            feedRecyclerView.adapter = adapter
            Log.d("HomeFragment", "Adapter created.")
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error: ${e.message}")
        }

        return view
    }
}