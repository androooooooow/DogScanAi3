package fragment_activity

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.firstapp.dogscanai.R
import network.model.RetrofitClient
import network.model.SaveScanRequest
import network.model.SaveScanResponse
import network.model.ScanPrediction
import network.model.UploadImageResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class DogScanResultFragment : Fragment() {

    private val TAG = "DogScanResultFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dog_scan_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val scanType  = arguments?.getString("SCAN_TYPE") ?: "breed"
            val title     = arguments?.getString("BREED")     ?: "Unknown"
            val accuracy  = arguments?.getDouble("ACCURACY")  ?: 0.0
            val path      = arguments?.getString("PATH")
            val details   = arguments?.getString("DETAILS")   ?: "No details available"
            val className = arguments?.getString("CLASS_NAME") ?: ""

            // Mode label
            val cardColor = if (scanType == "disease") "#B00020" else "#4A69FF"
            view.findViewById<TextView>(R.id.tv_mode_label)?.text =
                if (scanType == "disease") "TOP DISEASE" else "TOP BREED"
            view.findViewById<CardView>(R.id.result_card)
                ?.setCardBackgroundColor(android.graphics.Color.parseColor(cardColor))

            // Text fields
            view.findViewById<TextView>(R.id.top_breed_name)?.text = title
            view.findViewById<TextView>(R.id.top_breed_accuracy)?.text =
                if (scanType == "disease") String.format("%.1f%% Confidence", accuracy)
                else String.format("%.1f%% Match", accuracy)
            view.findViewById<ProgressBar>(R.id.accuracy_progress_bar)?.progress = accuracy.toInt()
            view.findViewById<TextView>(R.id.other_breeds_placeholder)?.text = details
            view.findViewById<TextView>(R.id.tv_details_header)?.text =
                if (scanType == "disease") "Disease Details" else "Analysis Details"

            // Image preview
            if (!path.isNullOrEmpty()) {
                val imgFile = File(path)
                if (imgFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    view.findViewById<ImageView>(R.id.scanned_dog_image)?.setImageBitmap(bitmap)
                } else {
                    view.findViewById<ImageView>(R.id.scanned_dog_image)
                        ?.setImageResource(R.drawable.aspin)
                }
            }

            // Retry button
            view.findViewById<Button>(R.id.retry_button)?.setOnClickListener {
                activity?.supportFragmentManager?.popBackStack()
                activity?.let { act ->
                    act.findViewById<View>(R.id.previewView)?.visibility    = View.VISIBLE
                    act.findViewById<View>(R.id.bottom_actions)?.visibility = View.VISIBLE
                    act.findViewById<View>(R.id.filter_toggle)?.visibility  = View.VISIBLE
                    act.findViewById<View>(R.id.scan_frame)?.visibility     = View.VISIBLE
                    act.findViewById<View>(R.id.scan_hint)?.visibility      = View.VISIBLE
                }
            }

            // ✅ FIXED Save button — upload image first, then save scan with server URL
            view.findViewById<Button>(R.id.save_button)?.setOnClickListener {
                uploadImageThenSave(
                    view      = view,
                    path      = path ?: "",
                    title     = title,
                    className = className,
                    accuracy  = accuracy,
                    scanType  = scanType
                )
            }

            // View History button
            view.findViewById<Button>(R.id.view_history_button)?.setOnClickListener {
                val intent = Intent(requireContext(), ScanHistoryActivity::class.java)
                startActivity(intent)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            Toast.makeText(context, "Error displaying result", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ STEP 1 — Upload the image file to the server, get back a real URL
    private fun uploadImageThenSave(
        view: View,
        path: String,
        title: String,
        className: String,
        accuracy: Double,
        scanType: String
    ) {
        val prefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token == null) {
            Toast.makeText(context, "Please login to save results", Toast.LENGTH_SHORT).show()
            return
        }

        val saveBtn = view.findViewById<Button>(R.id.save_button)
        saveBtn?.isEnabled = false
        saveBtn?.text = "Uploading..."

        val file = File(path)

        // If no valid file path, save with empty image_url
        if (path.isEmpty() || !file.exists()) {
            Log.w(TAG, "No image file found, saving without image")
            saveScanToDatabase(
                view      = view,
                imageUrl  = "",
                title     = title,
                className = className,
                accuracy  = accuracy,
                scanType  = scanType,
                token     = token
            )
            return
        }

        // Build multipart request
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart   = MultipartBody.Part.createFormData("image", file.name, requestFile)

        Log.d(TAG, ">>> Uploading image: ${file.name}")

        RetrofitClient.instance.uploadImage("Bearer $token", imagePart)
            .enqueue(object : Callback<UploadImageResponse> {
                override fun onResponse(
                    call: Call<UploadImageResponse>,
                    response: Response<UploadImageResponse>
                ) {
                    val serverImageUrl = response.body()?.image_url

                    Log.d(TAG, ">>> Upload response code: ${response.code()}")
                    Log.d(TAG, ">>> Server image URL: $serverImageUrl")

                    if (response.isSuccessful && !serverImageUrl.isNullOrEmpty()) {
                        // ✅ STEP 2 — Now save scan with the real server URL
                        saveBtn?.text = "Saving..."
                        saveScanToDatabase(
                            view      = view,
                            imageUrl  = serverImageUrl,  // ✅ real server URL, not local path
                            title     = title,
                            className = className,
                            accuracy  = accuracy,
                            scanType  = scanType,
                            token     = token
                        )
                    } else {
                        Log.e(TAG, ">>> Upload failed: ${response.errorBody()?.string()}")
                        saveBtn?.isEnabled = true
                        saveBtn?.text = "Save Result"
                        Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UploadImageResponse>, t: Throwable) {
                    Log.e(TAG, ">>> Upload error: ${t.message}", t)
                    saveBtn?.isEnabled = true
                    saveBtn?.text = "Save Result"
                    Toast.makeText(context, "Upload error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // ✅ STEP 2 — Save scan to database using the uploaded server image URL
    private fun saveScanToDatabase(
        view: View,
        imageUrl: String,   // ✅ now always a server URL like http://192.168.137.1:5000/uploads/abc.jpg
        title: String,
        className: String,
        accuracy: Double,
        scanType: String,
        token: String
    ) {
        Log.d(TAG, ">>> Saving scan with image_url: $imageUrl")

        val saveBtn = view.findViewById<Button>(R.id.save_button)

        val prediction = ScanPrediction(
            rank         = 1,
            class_name   = className,
            display_name = title,
            confidence   = accuracy
        )

        val request = SaveScanRequest(
            image_url   = imageUrl,   // ✅ server URL, not local path
            predictions = listOf(prediction),
            scan_type   = scanType
        )

        RetrofitClient.instance.saveScan("Bearer $token", request)
            .enqueue(object : Callback<SaveScanResponse> {
                override fun onResponse(
                    call: Call<SaveScanResponse>,
                    response: Response<SaveScanResponse>
                ) {
                    Log.d(TAG, ">>> Save code: ${response.code()}")
                    Log.d(TAG, ">>> Save body: ${response.body()}")

                    if (response.isSuccessful && response.body()?.success == true) {
                        saveBtn?.text = "Saved ✓"
                        Toast.makeText(context, "Result saved successfully!", Toast.LENGTH_SHORT).show()
                        view.findViewById<Button>(R.id.view_history_button)?.visibility = View.VISIBLE
                    } else {
                        saveBtn?.isEnabled = true
                        saveBtn?.text = "Save Result"
                        Toast.makeText(
                            context,
                            "Failed to save: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<SaveScanResponse>, t: Throwable) {
                    saveBtn?.isEnabled = true
                    saveBtn?.text = "Save Result"
                    Log.e(TAG, ">>> Save error: ${t.message}", t)
                    Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    companion object {
        fun newInstance(
            breed: String,
            accuracy: Double,
            path: String,
            details: String,
            scanType: String = "breed",
            className: String = ""
        ): DogScanResultFragment {
            return DogScanResultFragment().apply {
                arguments = Bundle().apply {
                    putString("SCAN_TYPE",  scanType)
                    putString("BREED",      breed)
                    putDouble("ACCURACY",   accuracy)
                    putString("PATH",       path)
                    putString("DETAILS",    details)
                    putString("CLASS_NAME", className)
                }
            }
        }
    }
}