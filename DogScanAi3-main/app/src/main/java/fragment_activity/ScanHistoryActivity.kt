package fragment_activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firstapp.dogscanai.R
import network.api.RetrofitClient
import network.model.ScanHistoryResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanHistoryActivity : AppCompatActivity() {
    private val TAG = "ScanHistoryActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_history)

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token == null) {
            Toast.makeText(this, "Please login to view history", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadHistory("Bearer $token")

        findViewById<View>(R.id.btn_back)?.setOnClickListener {
            finish()
        }
    }

    private fun loadHistory(token: String) {
        RetrofitClient.instance.getScanHistory(token)
            .enqueue(object : Callback<List<ScanHistoryResponse>> {
                override fun onResponse(
                    call: Call<List<ScanHistoryResponse>>,
                    response: Response<List<ScanHistoryResponse>>
                ) {
                    if (response.isSuccessful) {
                        val scans = response.body() ?: emptyList()
                        displayHistory(scans)
                    } else {
                        Log.e(TAG, "History error: ${response.code()}")
                        Toast.makeText(
                            this@ScanHistoryActivity,
                            "Failed to load history",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<ScanHistoryResponse>>, t: Throwable) {
                    Log.e(TAG, "History network error", t)
                    Toast.makeText(
                        this@ScanHistoryActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun displayHistory(scans: List<ScanHistoryResponse>) {
        val container = findViewById<LinearLayout>(R.id.history_container)
        val emptyView = findViewById<TextView>(R.id.tv_empty)

        container?.removeAllViews()

        if (scans.isEmpty()) {
            emptyView?.visibility = View.VISIBLE
            return
        }

        emptyView?.visibility = View.GONE

        val inflater = layoutInflater
        scans.forEach { scan ->
            val itemView = inflater.inflate(R.layout.item_scan_history, container, false)
            itemView.findViewById<TextView>(R.id.tv_scan_title)?.text =
                scan.top_prediction ?: "Unknown"
            itemView.findViewById<TextView>(R.id.tv_scan_type)?.text =
                scan.scan_type?.uppercase() ?: "SCAN"
            itemView.findViewById<TextView>(R.id.tv_scan_date)?.text =
                scan.scanned_at ?: ""
            itemView.findViewById<TextView>(R.id.tv_scan_confidence)?.text =
                "${scan.confidence ?: 0.0}%"
            container?.addView(itemView)
        }
    }
}