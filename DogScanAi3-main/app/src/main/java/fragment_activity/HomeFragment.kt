package fragment_activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.firstapp.dogscanai.R
import com.firstapp.dogscanai.utils.SessionManager
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

        val user = sessionManager.getUser()
        view.findViewById<TextView>(R.id.tv_username)?.text = user?.username ?: "User"

        view.findViewById<androidx.cardview.widget.CardView>(R.id.card_scan_new)
            ?.setOnClickListener {
                startActivity(Intent(requireContext(), CameraActivity::class.java))
            }

        view.findViewById<androidx.cardview.widget.CardView>(R.id.card_history)
            ?.setOnClickListener {
                startActivity(Intent(requireContext(), ScanHistoryActivity::class.java))
            }

        view.findViewById<androidx.cardview.widget.CardView>(R.id.card_contributer)
            ?.setOnClickListener {
                startActivity(Intent(requireContext(), ContributorLeaderboardActivity::class.java))
            }

        view.findViewById<androidx.cardview.widget.CardView>(R.id.card_chatbot)
            ?.setOnClickListener {
                startActivity(Intent(requireContext(), ChatActivity::class.java))
            }

        loadScanStats(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let {
            it.findViewById<TextView>(R.id.tv_username)?.text =
                sessionManager.getUser()?.username ?: "User"
            loadScanStats(it)
        }
    }

    private fun loadScanStats(view: View) {
        val token = sessionManager.getBearerToken() ?: return

        val tvTotalScans = view.findViewById<TextView>(R.id.tv_total_scans)
        val tvThisWeek   = view.findViewById<TextView>(R.id.tv_this_week)

        RetrofitClient.instance.getScanHistory(token)
            .enqueue(object : Callback<List<ScanHistoryResponse>> {
                override fun onResponse(
                    call: Call<List<ScanHistoryResponse>>,
                    response: Response<List<ScanHistoryResponse>>
                ) {
                    if (!isAdded) return

                    if (response.isSuccessful) {
                        val scans = response.body() ?: emptyList()
                        val totalScans = scans.size
                        val thisWeek = scans.count { scan ->
                            try {
                                val sdf = SimpleDateFormat(
                                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                    Locale.getDefault()
                                )
                                sdf.timeZone = TimeZone.getTimeZone("UTC")
                                val scanTime = sdf.parse(scan.scanned_at ?: "")?.time ?: 0L
                                val diffDays =
                                    (System.currentTimeMillis() - scanTime) / (1000 * 60 * 60 * 24)
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

                override fun onFailure(
                    call: Call<List<ScanHistoryResponse>>,
                    t: Throwable
                ) {
                    Log.e("HomeFragment", "Failed to load scan stats: ${t.message}")
                }
            })
    }
}