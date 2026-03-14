package fragment_activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firstapp.dogscanai.R
import com.firstapp.dogscanai.fragment_activity.ChatAdapter
import com.firstapp.dogscanai.fragment_activity.ChatMessage
import com.firstapp.dogscanai.fragment_activity.MessageRole
import com.firstapp.dogscanai.utils.SessionManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: MaterialButton
    private lateinit var typingIndicator: LinearLayout
    private lateinit var adapter: ChatAdapter
    private lateinit var sessionManager: SessionManager

    private val messages = mutableListOf<ChatMessage>()
    private val history = mutableListOf<JSONObject>()
    private var isLoading = false

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val BASE_URL = "http://192.168.137.1:5001"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView    = findViewById(R.id.recyclerView)
        etMessage       = findViewById(R.id.etMessage)
        btnSend         = findViewById(R.id.btnSend)
        typingIndicator = findViewById(R.id.typingIndicator)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        sessionManager = SessionManager(this)

        setupRecyclerView()
        setupInput()

        // Welcome message
        addAssistantMessage("Hello! I'm Casper, your DogScan AI assistant 🐾 How can I help you today?")
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages) { message -> copyToClipboard(message) }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = this@ChatActivity.adapter
        }
    }

    private fun setupInput() {
        btnSend.setOnClickListener { sendMessage() }
        etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }
    }

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        if (text.isEmpty() || isLoading) return

        etMessage.setText("")
        addUserMessage(text)
        showTypingIndicator(true)
        isLoading = true
        btnSend.isEnabled = false

        // Add to history
        history.add(JSONObject().apply {
            put("role", "user")
            put("content", text)
        })

        lifecycleScope.launch {
            try {
                val reply = callAssistantApi(text)
                showTypingIndicator(false)
                addAssistantMessage(reply)

                // Add assistant reply to history
                history.add(JSONObject().apply {
                    put("role", "assistant")
                    put("content", reply)
                })

                // Keep history to last 6 turns
                while (history.size > 12) history.removeAt(0)

            } catch (e: Exception) {
                showTypingIndicator(false)
                showError("Could not connect to assistant. Please try again.")
            } finally {
                isLoading = false
                btnSend.isEnabled = true
            }
        }
    }

    private suspend fun callAssistantApi(message: String): String {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val historyArray = JSONArray()
            for (item in history.dropLast(1)) { // exclude last user msg already added
                historyArray.put(item)
            }

            val body = JSONObject().apply {
                put("message", message)
                put("thread_type", "general")
                put("history", historyArray)
            }

            val request = Request.Builder()
                .url("$BASE_URL/assistant/chat")
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("Empty response")

            if (!response.isSuccessful) {
                throw Exception("Server error: ${response.code}")
            }

            JSONObject(responseBody).getString("reply")
        }
    }

    private fun addUserMessage(text: String) {
        messages.add(ChatMessage(content = text, role = MessageRole.USER))
        adapter.notifyItemInserted(messages.size - 1)
        scrollToBottom()
    }

    private fun addAssistantMessage(text: String) {
        messages.add(ChatMessage(content = text, role = MessageRole.ASSISTANT))
        adapter.notifyItemInserted(messages.size - 1)
        scrollToBottom()
    }

    private fun showTypingIndicator(show: Boolean) {
        typingIndicator.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        messages.add(ChatMessage(content = message, role = MessageRole.ERROR))
        adapter.notifyItemInserted(messages.size - 1)
        scrollToBottom()
    }

    private fun scrollToBottom() {
        recyclerView.postDelayed({
            if (messages.isNotEmpty()) {
                recyclerView.smoothScrollToPosition(messages.size - 1)
            }
        }, 100)
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("message", text))
        Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show()
    }
}