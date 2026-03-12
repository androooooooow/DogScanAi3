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
import com.bumptech.glide.Glide
import com.firstapp.dogscanai.R
import network.api.RetrofitClient
import network.model.BreedDetailResponse
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
            val scanType  = arguments?.getString("SCAN_TYPE")  ?: "breed"
            val title     = arguments?.getString("BREED")      ?: "Unknown"
            val accuracy  = arguments?.getDouble("ACCURACY")   ?: 0.0
            val path      = arguments?.getString("PATH")
            val details   = arguments?.getString("DETAILS")    ?: "No details available"
            val className = arguments?.getString("CLASS_NAME") ?: ""
            val breedId   = arguments?.getInt("BREED_ID")      ?: -1
            val allBreeds = arguments?.getString("ALL_BREEDS") ?: ""  // ✅ NEW

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

            // Scanned image preview
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

            // Fetch breed details
            if (scanType == "breed" && breedId > 0) {
                fetchBreedDetails(view, breedId)
            }

            // ✅ Manual Save button
            val saveBtn = view.findViewById<Button>(R.id.save_button)
            saveBtn?.visibility = View.VISIBLE
            saveBtn?.setOnClickListener {
                val prefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
                val token = prefs.getString("token", null)

                if (token == null) {
                    Toast.makeText(context, "Please login first to save", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                saveBtn.isEnabled = false
                saveBtn.text = "Saving..."

                uploadImageThenSave(
                    view      = view,
                    path      = path ?: "",
                    title     = title,
                    className = className,
                    accuracy  = accuracy,
                    scanType  = scanType,
                    token     = token,
                    breedId   = breedId,
                    allBreeds = allBreeds  // ✅ NEW
                )
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

            // View History button
            view.findViewById<Button>(R.id.view_history_button)?.setOnClickListener {
                startActivity(Intent(requireContext(), ScanHistoryActivity::class.java))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            Toast.makeText(context, "Error displaying result", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchBreedDetails(view: View, breedId: Int) {
        val prefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        RetrofitClient.instance.getBreedDetail("Bearer $token", breedId)
            .enqueue(object : Callback<BreedDetailResponse> {
                override fun onResponse(
                    call: Call<BreedDetailResponse>,
                    response: Response<BreedDetailResponse>
                ) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        val breed = response.body()
                        if (breed != null) displayBreedDetails(view, breed)
                    } else {
                        Log.e(TAG, ">>> Breed detail error: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<BreedDetailResponse>, t: Throwable) {
                    Log.e(TAG, ">>> Breed detail network error: ${t.message}")
                }
            })
    }

    private fun displayBreedDetails(view: View, breed: BreedDetailResponse) {
        activity?.runOnUiThread {
            view.findViewById<CardView>(R.id.card_breed_info)?.visibility = View.VISIBLE

            val breedImageView = view.findViewById<ImageView>(R.id.iv_breed_db_image)
            if (!breed.image_url.isNullOrEmpty()) {
                val fullImageUrl = if (breed.image_url.startsWith("http")) {
                    breed.image_url
                } else {
                    "http://192.168.137.1:5000${breed.image_url}"
                }
                Glide.with(this)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.aspin)
                    .error(R.drawable.aspin)
                    .centerCrop()
                    .into(breedImageView!!)
            }

            view.findViewById<TextView>(R.id.tv_temperament)?.apply {
                if (!breed.temperamentText.isNullOrEmpty()) {
                    visibility = View.VISIBLE
                    text = breed.temperamentText
                }
            }

            view.findViewById<TextView>(R.id.tv_origin)?.apply {
                if (!breed.origin.isNullOrEmpty()) {
                    visibility = View.VISIBLE
                    text = "Origin: ${breed.origin}"
                }
            }

            view.findViewById<TextView>(R.id.tv_size)?.apply {
                if (!breed.size.isNullOrEmpty()) {
                    visibility = View.VISIBLE
                    text = "Size: ${breed.size}"
                }
            }

            if (breed.lifespan_min != null && breed.lifespan_max != null) {
                view.findViewById<TextView>(R.id.tv_lifespan)?.apply {
                    visibility = View.VISIBLE
                    text = "Lifespan: ${breed.lifespan_min}–${breed.lifespan_max} years"
                }
            }

            view.findViewById<TextView>(R.id.tv_breed_description)?.apply {
                if (!breed.description.isNullOrEmpty()) {
                    visibility = View.VISIBLE
                    text = breed.description
                }
            }
        }
    }

    // ✅ Parse allBreeds string into ScanPrediction list
    private fun parseAllBreeds(allBreeds: String, fallbackClassName: String, fallbackTitle: String, fallbackAccuracy: Double, fallbackBreedId: Int): List<ScanPrediction> {
        if (allBreeds.isBlank()) {
            return listOf(ScanPrediction(
                rank         = 1,
                class_name   = fallbackClassName,
                display_name = fallbackTitle,
                confidence   = fallbackAccuracy,
                breed_id     = if (fallbackBreedId > 0) fallbackBreedId else null
            ))
        }

        return allBreeds.split(";;").mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size >= 4) {
                val rank        = parts[0].toIntOrNull() ?: 1
                val clsName     = parts[1]
                val dispName    = parts[2]
                val conf        = parts[3].toDoubleOrNull() ?: 0.0
                val bId         = parts.getOrNull(4)?.toIntOrNull()
                ScanPrediction(
                    rank         = rank,
                    class_name   = clsName,
                    display_name = dispName,
                    confidence   = conf,
                    breed_id     = bId
                )
            } else null
        }
    }

    private fun uploadImageThenSave(
        view: View,
        path: String,
        title: String,
        className: String,
        accuracy: Double,
        scanType: String,
        token: String,
        breedId: Int = -1,
        allBreeds: String = ""  // ✅ NEW
    ) {
        val saveBtn = view.findViewById<Button>(R.id.save_button)
        val file = File(path)

        if (path.isEmpty() || !file.exists()) {
            saveScanToDatabase(view, "", title, className, accuracy, scanType, token, breedId, allBreeds)
            return
        }

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart   = MultipartBody.Part.createFormData("image", file.name, requestFile)

        RetrofitClient.instance.uploadImage("Bearer $token", imagePart)
            .enqueue(object : Callback<UploadImageResponse> {
                override fun onResponse(
                    call: Call<UploadImageResponse>,
                    response: Response<UploadImageResponse>
                ) {
                    val serverImageUrl = response.body()?.image_url
                    if (response.isSuccessful && !serverImageUrl.isNullOrEmpty()) {
                        saveScanToDatabase(view, serverImageUrl, title, className, accuracy, scanType, token, breedId, allBreeds)
                    } else {
                        activity?.runOnUiThread {
                            saveBtn?.isEnabled = true
                            saveBtn?.text = "Save Result"
                            Toast.makeText(context, "Upload failed: ${response.code()}", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onFailure(call: Call<UploadImageResponse>, t: Throwable) {
                    activity?.runOnUiThread {
                        saveBtn?.isEnabled = true
                        saveBtn?.text = "Save Result"
                        Toast.makeText(context, "Upload error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                }
            })
    }

    private fun saveScanToDatabase(
        view: View,
        imageUrl: String,
        title: String,
        className: String,
        accuracy: Double,
        scanType: String,
        token: String,
        breedId: Int = -1,
        allBreeds: String = ""  // ✅ NEW
    ) {
        val saveBtn = view.findViewById<Button>(R.id.save_button)

        // ✅ Build full predictions list including rank 2, 3, etc.
        val predictions = parseAllBreeds(allBreeds, className, title, accuracy, breedId)
        Log.d(TAG, ">>> Saving ${predictions.size} predictions")

        val request = SaveScanRequest(
            image_url   = imageUrl,
            predictions = predictions,
            scan_type   = scanType
        )

        RetrofitClient.instance.saveScan("Bearer $token", request)
            .enqueue(object : Callback<SaveScanResponse> {
                override fun onResponse(
                    call: Call<SaveScanResponse>,
                    response: Response<SaveScanResponse>
                ) {
                    if (!isAdded) return
                    activity?.runOnUiThread {
                        if (response.isSuccessful && response.body()?.success == true) {
                            saveBtn?.text = "Saved ✓"
                            saveBtn?.isEnabled = false
                            Toast.makeText(context, "Saved to history!", Toast.LENGTH_SHORT).show()
                            view.findViewById<Button>(R.id.view_history_button)?.visibility = View.VISIBLE
                        } else {
                            Log.e(TAG, ">>> Save failed: ${response.code()} ${response.errorBody()?.string()}")
                            saveBtn?.isEnabled = true
                            saveBtn?.text = "Save Result"
                            Toast.makeText(context, "Save failed: ${response.code()}", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onFailure(call: Call<SaveScanResponse>, t: Throwable) {
                    activity?.runOnUiThread {
                        saveBtn?.isEnabled = true
                        saveBtn?.text = "Save Result"
                        Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
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
            className: String = "",
            breedId: Int = -1,
            allBreeds: String = ""  // ✅ NEW
        ): DogScanResultFragment {
            return DogScanResultFragment().apply {
                arguments = Bundle().apply {
                    putString("SCAN_TYPE",  scanType)
                    putString("BREED",      breed)
                    putDouble("ACCURACY",   accuracy)
                    putString("PATH",       path)
                    putString("DETAILS",    details)
                    putString("CLASS_NAME", className)
                    putInt("BREED_ID",      breedId)
                    putString("ALL_BREEDS", allBreeds)  // ✅ NEW
                }
            }
        }
    }
}