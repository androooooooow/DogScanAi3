package fragment_activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firstapp.dogscanai.R
import com.firstapp.dogscanai.fragment_activity.ChatAdapter
import com.firstapp.dogscanai.fragment_activity.ChatMessage
import com.firstapp.dogscanai.fragment_activity.MessageRole
import com.firstapp.dogscanai.utils.SessionManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import network.api.ApiService
import network.api.RetrofitClient
import network.model.BreedDetailResponse
import network.model.SaveScanRequest
import network.model.SaveScanResponse
import network.model.ScanPrediction
import network.model.SendMessageRequest
import network.model.UploadImageResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class DogScanResultFragment : Fragment() {

    private val TAG = "DogScanResultFragment"

    // Scan state
    private var savedScanId: Int? = null

    // Chat state
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatInput: EditText
    private lateinit var chatSendButton: MaterialButton
    private lateinit var chatTypingIndicator: LinearLayout
    private lateinit var apiService: ApiService
    private lateinit var sessionManager: SessionManager
    private val chatMessages = mutableListOf<ChatMessage>()
    private var chatThreadId: Int? = null
    private var isChatLoading = false

    companion object {
        private const val BASE_URL = "http://192.168.137.1:5000/"

        fun newInstance(
            breed: String,
            accuracy: Double,
            path: String,
            details: String,
            scanType: String = "breed",
            className: String = "",
            breedId: Int = -1,
            allBreeds: String = ""
        ): DogScanResultFragment {
            return DogScanResultFragment().apply {
                arguments = Bundle().apply {
                    putString("SCAN_TYPE", scanType)
                    putString("BREED", breed)
                    putDouble("ACCURACY", accuracy)
                    putString("PATH", path)
                    putString("DETAILS", details)
                    putString("CLASS_NAME", className)
                    putInt("BREED_ID", breedId)
                    putString("ALL_BREEDS", allBreeds)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dog_scan_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        try {
            val scanType  = arguments?.getString("SCAN_TYPE")  ?: "breed"
            val title     = arguments?.getString("BREED")      ?: "Unknown"
            val accuracy  = arguments?.getDouble("ACCURACY")   ?: 0.0
            val path      = arguments?.getString("PATH")
            val details   = arguments?.getString("DETAILS")    ?: "No details available"
            val className = arguments?.getString("CLASS_NAME") ?: ""
            val breedId   = arguments?.getInt("BREED_ID")      ?: -1
            val allBreeds = arguments?.getString("ALL_BREEDS") ?: ""

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

            // Scanned image
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

            // ── Save button ──
            val saveBtn = view.findViewById<Button>(R.id.save_button)
            saveBtn?.visibility = View.VISIBLE
            saveBtn?.setOnClickListener {
                val token = sessionManager.getBearerToken() ?: run {
                    Toast.makeText(context, "Please login first to save", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                Log.d(TAG, "Save token: $token")
                saveBtn.isEnabled = false
                saveBtn.text = "Saving..."
                uploadImageThenSave(view, path ?: "", title, className, accuracy, scanType, token, breedId, allBreeds, false)
            }

            // ── Contribute button ──
            val contributeBtn = view.findViewById<Button>(R.id.contribute_button)
            if (scanType == "breed") {
                contributeBtn?.visibility = View.VISIBLE
                contributeBtn?.setOnClickListener {
                    val token = sessionManager.getBearerToken() ?: run {
                        Toast.makeText(context, "Please login first", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                    android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Contribute to Dataset")
                        .setMessage("This will save your scan AND share the image with our team to help improve the AI model. Continue?")
                        .setPositiveButton("Yes, Contribute") { _, _ ->
                            contributeBtn.isEnabled = false
                            contributeBtn.text = "Contributing..."
                            val existingId = savedScanId
                            if (existingId != null) {
                                contributeSavedScan(view, existingId, token)
                            } else {
                                uploadImageThenSave(view, path ?: "", title, className, accuracy, scanType, token, breedId, allBreeds, true)
                            }
                        }
                        .setNegativeButton("Cancel") { _, _ ->
                            contributeBtn.isEnabled = true
                            contributeBtn.text = "🐾 Contribute to Dataset"
                        }
                        .show()
                }
            } else {
                contributeBtn?.visibility = View.GONE
            }

            // ── Retry button ──
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

            // ── View History button ──
            view.findViewById<Button>(R.id.view_history_button)?.setOnClickListener {
                startActivity(Intent(requireContext(), ScanHistoryActivity::class.java))
            }

            // ── Setup Chat ──
            setupChat(view, title, accuracy, scanType)

        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            Toast.makeText(context, "Error displaying result", Toast.LENGTH_SHORT).show()
        }
    }

    // ─────────────────────────────────────────────────────────
    // CHAT
    // ─────────────────────────────────────────────────────────

    private fun setupChat(view: View, title: String, accuracy: Double, scanType: String) {
        chatRecyclerView    = view.findViewById(R.id.chatRecyclerView)
        chatInput           = view.findViewById(R.id.chatInput)
        chatSendButton      = view.findViewById(R.id.chatSendButton)
        chatTypingIndicator = view.findViewById(R.id.chatTypingIndicator)

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        apiService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        chatAdapter = ChatAdapter(chatMessages) { message ->
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("message", message))
            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
        }

        chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
            adapter = chatAdapter
        }

        chatSendButton.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            initChatThread(title, accuracy, scanType)
        }

        chatSendButton.setOnClickListener { sendChatMessage() }
        chatInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) { sendChatMessage(); true } else false
        }
    }

    private suspend fun initChatThread(title: String, accuracy: Double, scanType: String) {
        val token = sessionManager.getBearerToken() ?: run {
            addChatMessage("Please login to use the chat assistant.", MessageRole.ERROR)
            return
        }

        try {
            val scanContext = mapOf(
                "scan_type" to scanType,
                "top_breeds" to listOf(
                    mapOf("rank" to 1, "display_name" to title, "confidence" to accuracy)
                )
            )

            val thread = apiService.createScanThread(token, mapOf("scan_context" to scanContext))
            chatThreadId = thread.id
            Log.d(TAG, "Chat thread created: ${thread.id}")

            addChatMessage(
                "Hi! I'm Casper 🐾 I can see your scan result — **$title** with ${String.format("%.1f", accuracy)}% confidence. What would you like to know?",
                MessageRole.ASSISTANT
            )
            chatSendButton.isEnabled = true

        } catch (e: Exception) {
            Log.e(TAG, "Chat thread init error: ${e.message}")
            addChatMessage("Chat unavailable. Please check your connection.", MessageRole.ERROR)
        }
    }

    private fun sendChatMessage() {
        val text = chatInput.text.toString().trim()
        if (text.isEmpty() || isChatLoading) return

        val tid = chatThreadId ?: run {
            Toast.makeText(context, "Chat not ready yet.", Toast.LENGTH_SHORT).show()
            return
        }

        chatInput.setText("")
        addChatMessage(text, MessageRole.USER)
        showChatTyping(true)
        isChatLoading = true
        chatSendButton.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            val token = sessionManager.getBearerToken() ?: run {
                showChatTyping(false)
                addChatMessage("Session expired. Please log in again.", MessageRole.ERROR)
                isChatLoading = false
                chatSendButton.isEnabled = true
                return@launch
            }

            try {
                val result = apiService.sendMessage(token, tid, SendMessageRequest(text))
                showChatTyping(false)

                val lastUserIndex = chatMessages.indexOfLast { it.role == MessageRole.USER }
                if (lastUserIndex != -1) {
                    chatMessages[lastUserIndex] = ChatMessage(
                        id        = result.user_message.id,
                        content   = result.user_message.content,
                        role      = MessageRole.USER,
                        createdAt = result.user_message.created_at
                    )
                    chatAdapter.notifyItemChanged(lastUserIndex)
                }

                addChatMessage(result.assistant_message.content, MessageRole.ASSISTANT)

            } catch (e: Exception) {
                Log.e(TAG, "Chat send error: ${e.message}")
                showChatTyping(false)
                addChatMessage("Could not send message. Try again.", MessageRole.ERROR)
            } finally {
                isChatLoading = false
                chatSendButton.isEnabled = true
            }
        }
    }

    private fun addChatMessage(text: String, role: MessageRole) {
        activity?.runOnUiThread {
            chatMessages.add(ChatMessage(content = text, role = role))
            chatAdapter.notifyItemInserted(chatMessages.size - 1)
            chatRecyclerView.postDelayed({
                if (chatMessages.isNotEmpty()) chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
            }, 100)
        }
    }

    private fun showChatTyping(show: Boolean) {
        activity?.runOnUiThread {
            chatTypingIndicator.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    // ─────────────────────────────────────────────────────────
    // BREED DETAILS
    // ─────────────────────────────────────────────────────────

    private fun fetchBreedDetails(view: View, breedId: Int) {
        val token = sessionManager.getBearerToken() ?: return

        RetrofitClient.instance.getBreedDetail(token, breedId)
            .enqueue(object : Callback<BreedDetailResponse> {
                override fun onResponse(call: Call<BreedDetailResponse>, response: Response<BreedDetailResponse>) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        val breed = response.body()
                        if (breed != null) displayBreedDetails(view, breed)
                    } else {
                        Log.e(TAG, "Breed detail error: ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<BreedDetailResponse>, t: Throwable) {
                    Log.e(TAG, "Breed detail network error: ${t.message}")
                }
            })
    }

    private fun displayBreedDetails(view: View, breed: BreedDetailResponse) {
        activity?.runOnUiThread {
            view.findViewById<CardView>(R.id.card_breed_info)?.visibility = View.VISIBLE

            val breedImageView = view.findViewById<ImageView>(R.id.iv_breed_db_image)
            if (!breed.image_url.isNullOrEmpty()) {
                val fullImageUrl = if (breed.image_url.startsWith("http")) breed.image_url
                else "http://192.168.137.1:5000${breed.image_url}"
                Glide.with(this).load(fullImageUrl).placeholder(R.drawable.aspin).error(R.drawable.aspin).centerCrop().into(breedImageView!!)
            }

            view.findViewById<TextView>(R.id.tv_temperament)?.apply {
                if (!breed.temperamentText.isNullOrEmpty()) { visibility = View.VISIBLE; text = breed.temperamentText }
            }
            view.findViewById<TextView>(R.id.tv_origin)?.apply {
                if (!breed.origin.isNullOrEmpty()) { visibility = View.VISIBLE; text = "Origin: ${breed.origin}" }
            }
            view.findViewById<TextView>(R.id.tv_size)?.apply {
                if (!breed.size.isNullOrEmpty()) { visibility = View.VISIBLE; text = "Size: ${breed.size}" }
            }
            if (breed.lifespan_min != null && breed.lifespan_max != null) {
                view.findViewById<TextView>(R.id.tv_lifespan)?.apply {
                    visibility = View.VISIBLE; text = "Lifespan: ${breed.lifespan_min}–${breed.lifespan_max} years"
                }
            }
            view.findViewById<TextView>(R.id.tv_breed_description)?.apply {
                if (!breed.description.isNullOrEmpty()) { visibility = View.VISIBLE; text = breed.description }
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // SAVE / UPLOAD
    // ─────────────────────────────────────────────────────────

    private fun parseAllBreeds(
        allBreeds: String,
        fallbackClassName: String,
        fallbackTitle: String,
        fallbackAccuracy: Double,
        fallbackBreedId: Int
    ): List<ScanPrediction> {
        if (allBreeds.isBlank()) {
            return listOf(ScanPrediction(rank = 1, class_name = fallbackClassName, display_name = fallbackTitle, confidence = fallbackAccuracy, breed_id = if (fallbackBreedId > 0) fallbackBreedId else null))
        }
        return allBreeds.split(";;").mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size >= 4) ScanPrediction(rank = parts[0].toIntOrNull() ?: 1, class_name = parts[1], display_name = parts[2], confidence = parts[3].toDoubleOrNull() ?: 0.0, breed_id = parts.getOrNull(4)?.toIntOrNull())
            else null
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
        allBreeds: String = "",
        shareForTraining: Boolean = false
    ) {
        val saveBtn       = view.findViewById<Button>(R.id.save_button)
        val contributeBtn = view.findViewById<Button>(R.id.contribute_button)
        val file          = File(path)

        Log.d(TAG, "uploadImageThenSave — token: $token")
        Log.d(TAG, "uploadImageThenSave — path: $path, exists: ${file.exists()}")

        if (path.isEmpty() || !file.exists()) {
            saveScanToDatabase(view, "", title, className, accuracy, scanType, token, breedId, allBreeds, shareForTraining)
            return
        }

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart   = MultipartBody.Part.createFormData("image", file.name, requestFile)

        RetrofitClient.instance.uploadImage(token, imagePart)
            .enqueue(object : Callback<UploadImageResponse> {
                override fun onResponse(call: Call<UploadImageResponse>, response: Response<UploadImageResponse>) {
                    Log.d(TAG, "Upload response: ${response.code()}")
                    val serverImageUrl = response.body()?.image_url
                    if (response.isSuccessful && !serverImageUrl.isNullOrEmpty()) {
                        saveScanToDatabase(view, serverImageUrl, title, className, accuracy, scanType, token, breedId, allBreeds, shareForTraining)
                    } else {
                        Log.e(TAG, "Upload failed: ${response.code()} ${response.errorBody()?.string()}")
                        activity?.runOnUiThread {
                            saveBtn?.isEnabled = true; saveBtn?.text = "Save Result"
                            contributeBtn?.isEnabled = true; contributeBtn?.text = "🐾 Contribute to Dataset"
                            Toast.makeText(context, "Upload failed: ${response.code()}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                override fun onFailure(call: Call<UploadImageResponse>, t: Throwable) {
                    Log.e(TAG, "Upload network error: ${t.message}")
                    activity?.runOnUiThread {
                        saveBtn?.isEnabled = true; saveBtn?.text = "Save Result"
                        contributeBtn?.isEnabled = true; contributeBtn?.text = "🐾 Contribute to Dataset"
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
        allBreeds: String = "",
        shareForTraining: Boolean = false
    ) {
        val saveBtn       = view.findViewById<Button>(R.id.save_button)
        val contributeBtn = view.findViewById<Button>(R.id.contribute_button)
        val predictions   = parseAllBreeds(allBreeds, className, title, accuracy, breedId)
        val request       = SaveScanRequest(image_url = imageUrl, predictions = predictions, scan_type = scanType, share_for_training = shareForTraining)

        RetrofitClient.instance.saveScan(token, request)
            .enqueue(object : Callback<SaveScanResponse> {
                override fun onResponse(call: Call<SaveScanResponse>, response: Response<SaveScanResponse>) {
                    if (!isAdded) return
                    activity?.runOnUiThread {
                        if (response.isSuccessful && (response.body()?.success == true || response.body()?.scan_id != null)) {
                            savedScanId = response.body()?.scan_id
                            Log.d(TAG, "Scan saved with ID: $savedScanId")

                            if (shareForTraining) {
                                contributeBtn?.text = "✓ Contributed!"; contributeBtn?.isEnabled = false
                                saveBtn?.text = "Saved ✓"; saveBtn?.isEnabled = false
                                Toast.makeText(context, "Thank you! Your image has been submitted for review.", Toast.LENGTH_LONG).show()
                            } else {
                                saveBtn?.text = "Saved ✓"; saveBtn?.isEnabled = false
                                Toast.makeText(context, "Saved to history!", Toast.LENGTH_SHORT).show()
                            }
                            view.findViewById<Button>(R.id.view_history_button)?.visibility = View.VISIBLE
                        } else {
                            Log.e(TAG, "Save failed: ${response.code()} ${response.errorBody()?.string()}")
                            saveBtn?.isEnabled = true; saveBtn?.text = "Save Result"
                            contributeBtn?.isEnabled = true; contributeBtn?.text = "🐾 Contribute to Dataset"
                            Toast.makeText(context, "Save failed: ${response.code()}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                override fun onFailure(call: Call<SaveScanResponse>, t: Throwable) {
                    activity?.runOnUiThread {
                        saveBtn?.isEnabled = true; saveBtn?.text = "Save Result"
                        contributeBtn?.isEnabled = true; contributeBtn?.text = "🐾 Contribute to Dataset"
                        Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                }
            })
    }

    private fun contributeSavedScan(view: View, scanId: Int, token: String) {
        val saveBtn       = view.findViewById<Button>(R.id.save_button)
        val contributeBtn = view.findViewById<Button>(R.id.contribute_button)

        RetrofitClient.instance.contributeScan(token, scanId)
            .enqueue(object : Callback<SaveScanResponse> {
                override fun onResponse(call: Call<SaveScanResponse>, response: Response<SaveScanResponse>) {
                    if (!isAdded) return
                    activity?.runOnUiThread {
                        if (response.isSuccessful) {
                            contributeBtn?.text = "✓ Contributed!"; contributeBtn?.isEnabled = false
                            saveBtn?.text = "Saved ✓"; saveBtn?.isEnabled = false
                            Toast.makeText(context, "Thank you! Submitted for review.", Toast.LENGTH_LONG).show()
                        } else {
                            contributeBtn?.isEnabled = true; contributeBtn?.text = "🐾 Contribute to Dataset"
                            Toast.makeText(context, "Already contributed: ${response.code()}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                override fun onFailure(call: Call<SaveScanResponse>, t: Throwable) {
                    activity?.runOnUiThread {
                        contributeBtn?.isEnabled = true; contributeBtn?.text = "🐾 Contribute to Dataset"
                        Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                }
            })
    }
}