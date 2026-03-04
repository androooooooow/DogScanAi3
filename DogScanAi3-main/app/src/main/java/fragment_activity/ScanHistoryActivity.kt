package fragment_activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firstapp.dogscanai.R
import network.api.RetrofitClient
import network.model.ScanHistoryResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ScanHistoryActivity : AppCompatActivity() {
    private val TAG = "ScanHistoryActivity"

    // ✅ Your server IP — change this if your IP changes
    private val SERVER_IP = "http://192.168.137.1:5000"

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
                        Log.d(TAG, ">>> Loaded ${scans.size} scans")
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

            // Text fields
            itemView.findViewById<TextView>(R.id.tv_scan_title)?.text =
                scan.top_prediction ?: "Unknown"
            itemView.findViewById<TextView>(R.id.tv_scan_type)?.text =
                scan.scan_type?.uppercase() ?: "SCAN"
            itemView.findViewById<TextView>(R.id.tv_scan_date)?.text =
                formatDate(scan.scanned_at)
            itemView.findViewById<TextView>(R.id.tv_scan_confidence)?.text =
                String.format("%.2f%%", scan.confidence ?: 0.0)

            // ✅ Load image using Glide
            val imageView = itemView.findViewById<ImageView>(R.id.iv_scan_image)
            val imageUrl = scan.image_url

            Log.d(TAG, ">>> Scan: ${scan.top_prediction} | image_url: $imageUrl")

            // ✅ Fix: web scans save with localhost — replace with real IP for mobile
            val fullImageUrl = imageUrl
                ?.replace("http://localhost:5000", SERVER_IP)
                ?.replace("http://127.0.0.1:5000", SERVER_IP)

            if (!fullImageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(fullImageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.aspin)
                    .error(R.drawable.aspin)
                    .centerCrop()
                    .into(imageView)
            } else {
                imageView?.setImageResource(R.drawable.aspin)
            }

            container?.addView(itemView)
        }
    }

    // Format ISO date string to readable format
    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "--"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM d, yyyy hh:mm a", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)
            if (date != null) outputFormat.format(date) else dateString
        } catch (e: Exception) {
            dateString
        }
    }
}