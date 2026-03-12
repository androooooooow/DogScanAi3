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
                        Toast.makeText(this@ScanHistoryActivity, "Failed to load history", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<ScanHistoryResponse>>, t: Throwable) {
                    Log.e(TAG, "History network error", t)
                    Toast.makeText(this@ScanHistoryActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
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

            val topPrediction = scan.predictions?.minByOrNull { it.rank ?: 99 }
            val breedInfo = topPrediction?.breed_info

            // Title
            itemView.findViewById<TextView>(R.id.tv_scan_title)?.text =
                topPrediction?.display_name ?: "Unknown"

            // Scan type badge
            val scanType = scan.scan_type?.uppercase() ?: "SCAN"
            itemView.findViewById<TextView>(R.id.tv_scan_type)?.text = scanType

            // Date
            itemView.findViewById<TextView>(R.id.tv_scan_date)?.text =
                formatDate(scan.scanned_at)

            // Confidence
            itemView.findViewById<TextView>(R.id.tv_scan_confidence)?.text =
                String.format("%.2f%%", topPrediction?.confidence ?: 0.0)

            // ✅ Temperament (e.g. "loyal, adaptable, resilient")
            val temperament = breedInfo?.temperament
            itemView.findViewById<TextView>(R.id.tv_scan_temperament)?.apply {
                if (!temperament.isNullOrEmpty()) {
                    visibility = View.VISIBLE
                    text = temperament.joinToString(", ")
                } else {
                    visibility = View.GONE
                }
            }

            // ✅ Origin
            val origin = breedInfo?.origin
            itemView.findViewById<TextView>(R.id.tv_scan_origin)?.apply {
                if (!origin.isNullOrEmpty()) {
                    visibility = View.VISIBLE
                    text = "Origin: $origin"
                } else {
                    visibility = View.GONE
                }
            }

            // ✅ Description
            val description = breedInfo?.description
            itemView.findViewById<TextView>(R.id.tv_scan_description)?.apply {
                if (!description.isNullOrEmpty()) {
                    visibility = View.VISIBLE
                    text = description
                } else {
                    visibility = View.GONE
                }
            }

            // ✅ Breed DB image (from breed_info)
            val breedDbImageView = itemView.findViewById<ImageView>(R.id.iv_breed_info_image)
            val breedImageUrl = breedInfo?.image_url
            if (!breedImageUrl.isNullOrEmpty()) {
                val fullBreedImageUrl = if (breedImageUrl.startsWith("http")) {
                    breedImageUrl
                } else {
                    "$SERVER_IP$breedImageUrl"
                }
                breedDbImageView?.visibility = View.VISIBLE
                Glide.with(this)
                    .load(fullBreedImageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.aspin)
                    .error(R.drawable.aspin)
                    .centerCrop()
                    .into(breedDbImageView!!)
            } else {
                breedDbImageView?.visibility = View.GONE
            }

            // Scanned image
            val imageView = itemView.findViewById<ImageView>(R.id.iv_scan_image)
            val fullImageUrl = scan.image_url
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