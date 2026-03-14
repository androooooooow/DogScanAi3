package fragment_activity

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firstapp.dogscanai.R
import network.model.LeaderboardEntry

class LeaderboardAdapter(
    private val entries: List<LeaderboardEntry>,
    private val currentUsername: String? = null
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    // Distinct avatar colors per position
    private val avatarColors = listOf(
        "#3D5AF1", "#E53935", "#43A047", "#FB8C00",
        "#8E24AA", "#00897B", "#F4511E", "#1E88E5"
    )

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRank    : TextView    = itemView.findViewById(R.id.tv_rank)
        val tvUsername: TextView    = itemView.findViewById(R.id.tv_username)
        val tvCount   : TextView    = itemView.findViewById(R.id.tv_count)
        val tvAvatar  : TextView    = itemView.findViewById(R.id.tv_avatar)
        val ivMedal   : ImageView   = itemView.findViewById(R.id.iv_medal)
        val rowRoot   : LinearLayout = itemView.findViewById(R.id.row_root)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard_entry, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount() = entries.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        val isMe  = entry.username == currentUsername

        // ── Crown icon for top 3, rank text for rest ──────
        when (entry.rank) {
            1 -> {
                holder.ivMedal.visibility = View.VISIBLE
                holder.tvRank.visibility  = View.GONE
                holder.ivMedal.setImageResource(R.drawable.ic_crown_gold)
            }
            2 -> {
                holder.ivMedal.visibility = View.VISIBLE
                holder.tvRank.visibility  = View.GONE
                holder.ivMedal.setImageResource(R.drawable.ic_crown_silver)
            }
            3 -> {
                holder.ivMedal.visibility = View.VISIBLE
                holder.tvRank.visibility  = View.GONE
                holder.ivMedal.setImageResource(R.drawable.ic_crown_bronze)
            }
            else -> {
                holder.ivMedal.visibility = View.GONE
                holder.tvRank.visibility  = View.VISIBLE
                holder.tvRank.text        = "#${entry.rank}"
            }
        }

        // ── Avatar initial + color ────────────────────────
        val initial = entry.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        holder.tvAvatar.text = initial
        val avatarColor = avatarColors[position % avatarColors.size]
        val drawable = holder.tvAvatar.background?.mutate() as? GradientDrawable
        drawable?.setColor(Color.parseColor(avatarColor))

        // ── Username ──────────────────────────────────────
        holder.tvUsername.text = if (isMe) "${entry.username} (you)" else entry.username

        // ── Count ─────────────────────────────────────────
        holder.tvCount.text = "${entry.approved_count}"

        // ── Highlight current user ────────────────────────
        if (isMe) {
            holder.rowRoot.setBackgroundColor(Color.parseColor("#EEF2FF"))
            holder.tvUsername.setTextColor(Color.parseColor("#3D5AF1"))
            holder.tvCount.setTextColor(Color.parseColor("#3D5AF1"))
        } else {
            holder.rowRoot.setBackgroundColor(Color.TRANSPARENT)
            holder.tvUsername.setTextColor(Color.parseColor("#111827"))
            holder.tvCount.setTextColor(Color.parseColor("#374151"))
        }
    }
}