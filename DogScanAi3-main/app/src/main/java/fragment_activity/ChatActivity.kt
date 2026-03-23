package fragment_activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
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
import network.api.ApiService
import network.model.SendMessageRequest
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: MaterialButton
    private lateinit var typingIndicator: LinearLayout
    private lateinit var adapter: ChatAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService

    private val messages = mutableListOf<ChatMessage>()
    private var threadId: Int? = null
    private var isLoading = false

    companion object {
        private const val TAG = "ChatActivity"
        private const val BASE_URL = "http://192.168.137.1:5000/"
    }

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

        // ← OkHttpClient with generous timeouts for Ollama
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

        setupRecyclerView()
        setupInput()

        btnSend.isEnabled = false

        lifecycleScope.launch { initThread() }
    }

    // ─── Thread & History Init ────────────────────────────────────────────────

    private suspend fun initThread() {
        Log.d(TAG, "initThread() started")

        val token = sessionManager.getBearerToken()
        if (token == null) {
            Log.e(TAG, "Token is null — user not logged in")
            showErrorMessage("Not logged in. Please restart the app.")
            return
        }

        Log.d(TAG, "Token OK: $token")

        try {
            Log.d(TAG, "Calling createGeneralThread...")
            val thread = apiService.createGeneralThread(token)
            threadId = thread.id
            Log.d(TAG, "Thread ready — id=${thread.id}, type=${thread.thread_type}")

            Log.d(TAG, "Loading messages for thread ${thread.id}...")
            val response = apiService.getMessages(token, thread.id, limit = 50)
            Log.d(TAG, "Messages loaded: ${response.messages.size} message(s)")

            if (response.messages.isEmpty()) {
                addAssistantMessage(
                    "Hello! I'm Casper, your DogScan AI assistant 🐾 How can I help you today?"
                )
            } else {
                response.messages.forEach { msg ->
                    messages.add(
                        ChatMessage(
                            id        = msg.id,
                            content   = msg.content,
                            role      = if (msg.role == "assistant") MessageRole.ASSISTANT
                            else MessageRole.USER,
                            createdAt = msg.created_at
                        )
                    )
                }
                adapter.notifyDataSetChanged()
                scrollToBottom()
            }

            btnSend.isEnabled = true
            Log.d(TAG, "initThread() complete — send button enabled")

        } catch (e: retrofit2.HttpException) {
            val code = e.code()
            val errorBody = e.response()?.errorBody()?.string()
            Log.e(TAG, "HTTP $code — $errorBody")
            showErrorMessage("Server error $code. Check your Node.js server.")

        } catch (e: java.net.ConnectException) {
            Log.e(TAG, "ConnectException — can't reach server: ${e.message}")
            showErrorMessage("Can't connect to server. Check your IP and that Node.js is running.")

        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Timeout — server took too long: ${e.message}")
            showErrorMessage("Connection timed out. Check your network.")

        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.javaClass.simpleName}: ${e.message}")
            showErrorMessage("Could not load chat. (${e.javaClass.simpleName}: ${e.message})")
        }
    }

    // ─── RecyclerView ─────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages) { message -> copyToClipboard(message) }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = this@ChatActivity.adapter
        }
    }

    // ─── Input ────────────────────────────────────────────────────────────────

    private fun setupInput() {
        btnSend.setOnClickListener { sendMessage() }
        etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }
    }

    // ─── Send Message ─────────────────────────────────────────────────────────

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        if (text.isEmpty() || isLoading) return

        val tid = threadId
        if (tid == null) {
            Log.e(TAG, "sendMessage() called but threadId is null")
            showErrorMessage("Chat not ready yet. Please wait a moment and try again.")
            return
        }

        Log.d(TAG, "Sending message to thread $tid: \"$text\"")

        etMessage.setText("")
        addUserMessage(text)
        showTypingIndicator(true)
        isLoading = true
        btnSend.isEnabled = false

        lifecycleScope.launch {
            val token = sessionManager.getBearerToken()
            if (token == null) {
                Log.e(TAG, "Token gone during sendMessage")
                showTypingIndicator(false)
                showErrorMessage("Session expired. Please log in again.")
                isLoading = false
                btnSend.isEnabled = true
                return@launch
            }

            try {
                val result = apiService.sendMessage(
                    token,
                    tid,
                    SendMessageRequest(text)
                )

                Log.d(TAG, "Reply received: ${result.assistant_message.content.take(80)}...")
                showTypingIndicator(false)

                // Update optimistic user bubble with DB-persisted record
                val lastUserIndex = messages.indexOfLast { it.role == MessageRole.USER }
                if (lastUserIndex != -1) {
                    messages[lastUserIndex] = ChatMessage(
                        id        = result.user_message.id,
                        content   = result.user_message.content,
                        role      = MessageRole.USER,
                        createdAt = result.user_message.created_at
                    )
                    adapter.notifyItemChanged(lastUserIndex)
                }

                addAssistantMessage(result.assistant_message.content)

            } catch (e: retrofit2.HttpException) {
                val code = e.code()
                val errorBody = e.response()?.errorBody()?.string()
                Log.e(TAG, "HTTP $code on sendMessage — $errorBody")
                showTypingIndicator(false)
                showErrorMessage("Server error $code. Try again.")

            } catch (e: java.net.ConnectException) {
                Log.e(TAG, "ConnectException on sendMessage: ${e.message}")
                showTypingIndicator(false)
                showErrorMessage("Can't connect to server. Check your network.")

            } catch (e: java.net.SocketTimeoutException) {
                Log.e(TAG, "Timeout on sendMessage: ${e.message}")
                showTypingIndicator(false)
                showErrorMessage("Request timed out. Try again.")

            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error on sendMessage: ${e.javaClass.simpleName}: ${e.message}")
                showTypingIndicator(false)
                showErrorMessage("Could not send message. (${e.javaClass.simpleName})")

            } finally {
                isLoading = false
                btnSend.isEnabled = true
            }
        }
    }

    // ─── UI Helpers ───────────────────────────────────────────────────────────

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

    private fun showErrorMessage(msg: String) {
        runOnUiThread {
            messages.add(ChatMessage(content = msg, role = MessageRole.ERROR))
            adapter.notifyItemInserted(messages.size - 1)
            scrollToBottom()
        }
    }

    private fun showTypingIndicator(show: Boolean) {
        runOnUiThread {
            typingIndicator.visibility = if (show) View.VISIBLE else View.GONE
        }
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