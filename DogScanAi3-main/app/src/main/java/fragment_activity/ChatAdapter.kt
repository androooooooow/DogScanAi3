package com.firstapp.dogscanai.fragment_activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.text.method.LinkMovementMethod
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.linkify.LinkifyPlugin
import com.firstapp.dogscanai.R

enum class MessageRole { USER, ASSISTANT, ERROR }

data class ChatMessage(
    val id: Int? = null,
    val content: String,
    val role: MessageRole,
    val createdAt: String? = null
)

class ChatAdapter(
    private val messages: MutableList<ChatMessage>,
    private val onCopy: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_USER = 0
        private const val VIEW_ASSISTANT = 1
        private const val VIEW_ERROR = 2
    }

    override fun getItemViewType(position: Int): Int = when (messages[position].role) {
        MessageRole.USER -> VIEW_USER
        MessageRole.ASSISTANT -> VIEW_ASSISTANT
        MessageRole.ERROR -> VIEW_ERROR
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_USER -> UserViewHolder(inflater.inflate(R.layout.item_message_user, parent, false))
            VIEW_ERROR -> ErrorViewHolder(inflater.inflate(R.layout.item_message_error, parent, false))
            else -> AssistantViewHolder(inflater.inflate(R.layout.item_message_assistant, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        when (holder) {
            is UserViewHolder -> holder.bind(msg)
            is AssistantViewHolder -> holder.bind(msg, onCopy)
            is ErrorViewHolder -> holder.bind(msg)
        }
    }

    override fun getItemCount() = messages.size

    // ─── ViewHolders ──────────────────────────────────────────────────────────

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        fun bind(msg: ChatMessage) {
            tvMessage.text = msg.content
        }
    }

    class AssistantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val btnCopy: ImageButton = itemView.findViewById(R.id.btnCopy)

        private val markwon: Markwon = Markwon.builder(itemView.context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(itemView.context))
            .usePlugin(LinkifyPlugin.create())
            .build()

        fun bind(msg: ChatMessage, onCopy: (String) -> Unit) {
            markwon.setMarkdown(tvMessage, msg.content)
            tvMessage.movementMethod = LinkMovementMethod.getInstance()

            btnCopy.setOnClickListener {
                onCopy(msg.content)
                // Brief visual feedback
                btnCopy.setImageResource(R.drawable.ic_check)
                btnCopy.postDelayed({
                    btnCopy.setImageResource(R.drawable.ic_copy)
                }, 1500)
            }
        }
    }

    class ErrorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        fun bind(msg: ChatMessage) {
            tvMessage.text = "⚠️ ${msg.content}"
        }
    }
}