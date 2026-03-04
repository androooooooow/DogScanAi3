package com.firstapp.dogscanai.fragment_activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dogscanai.utils.SessionManager
import com.firstapp.dogscanai.R
import fragment_activity.ScanHistoryActivity
import network.api.RetrofitClient
import network.model.ScanHistoryResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("HomeFragment", "=== HOME FRAGMENT STARTED ===")

        sessionManager = SessionManager(requireContext())

        // Show username
        val user = sessionManager.getUser()
        view.findViewById<TextView>(R.id.tv_username)?.text = user?.username ?: "User"

        // History button
        view.findViewById<ImageView>(R.id.btn_history)?.setOnClickListener {
            startActivity(Intent(requireContext(), ScanHistoryActivity::class.java))
        }

        // Load real scan counts
        loadScanStats(view)
    }

    override fun onResume() {
        super.onResume()
        // Refresh every time user comes back to home screen
        view?.let {
            it.findViewById<TextView>(R.id.tv_username)?.text =
                sessionManager.getUser()?.username ?: "User"
            loadScanStats(it)
        }
    }

    private fun loadScanStats(view: View) {
        val token = sessionManager.getBearerToken() ?: return

        val tvTotalScans = view.findViewById<TextView>(R.id.tv_total_scans)
        val tvThisWeek   = view.findViewById<TextView>(R.id.tv_this_week) // ✅ correct ID

        RetrofitClient.instance.getScanHistory(token)
            .enqueue(object : Callback<List<ScanHistoryResponse>> {
                override fun onResponse(
                    call: Call<List<ScanHistoryResponse>>,
                    response: Response<List<ScanHistoryResponse>>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful) {
                        val scans = response.body() ?: emptyList()

                        // Total scans
                        val totalScans = scans.size

                        // This week — same logic as web DashboardPage.jsx
                        val thisWeek = scans.count { scan ->
                            try {
                                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                                sdf.timeZone = TimeZone.getTimeZone("UTC")
                                val scanTime = sdf.parse(scan.scanned_at ?: "")?.time ?: 0L
                                val diffDays = (System.currentTimeMillis() - scanTime) / (1000 * 60 * 60 * 24)
                                diffDays in 0..7
                            } catch (e: Exception) { false }
                        }

                        Log.d("HomeFragment", "Total: $totalScans | This week: $thisWeek")

                        activity?.runOnUiThread {
                            tvTotalScans?.text = totalScans.toString()
                            tvThisWeek?.text   = thisWeek.toString()
                        }
                    }
                }

                override fun onFailure(call: Call<List<ScanHistoryResponse>>, t: Throwable) {
                    Log.e("HomeFragment", "Failed to load scan stats: ${t.message}")
                }
            })
    }
}