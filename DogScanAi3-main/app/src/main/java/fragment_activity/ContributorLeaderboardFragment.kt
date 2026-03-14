package fragment_activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firstapp.dogscanai.R
import androidx.recyclerview.widget.RecyclerView
import network.api.RetrofitClient
import network.model.ContributorStats
import network.model.LeaderboardEntry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContributorLeaderboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contributor_leaderboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs    = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val token    = prefs.getString("token", null)
        val username = prefs.getString("username", null)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_leaderboard)
        val tvYourRank   = view.findViewById<TextView>(R.id.tv_your_rank)
        val tvYourCount  = view.findViewById<TextView>(R.id.tv_your_count)
        val tvPending    = view.findViewById<TextView>(R.id.tv_pending_count)
        val progressBar  = view.findViewById<ProgressBar>(R.id.leaderboard_progress)
        val tvEmpty      = view.findViewById<TextView>(R.id.tv_leaderboard_empty)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        progressBar.visibility = View.VISIBLE

        // ── Fetch leaderboard ─────────────────────────────────
        RetrofitClient.instance.getLeaderboard()
            .enqueue(object : Callback<List<LeaderboardEntry>> {
                override fun onResponse(
                    call: Call<List<LeaderboardEntry>>,
                    response: Response<List<LeaderboardEntry>>
                ) {
                    if (!isAdded) return
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val entries = response.body() ?: emptyList()
                        if (entries.isEmpty()) {
                            tvEmpty.visibility = View.VISIBLE
                        } else {
                            recyclerView.adapter = LeaderboardAdapter(entries, username)
                        }
                    } else {
                        tvEmpty.text = "Failed to load leaderboard."
                        tvEmpty.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<List<LeaderboardEntry>>, t: Throwable) {
                    if (!isAdded) return
                    progressBar.visibility = View.GONE
                    tvEmpty.text = "Network error: ${t.message}"
                    tvEmpty.visibility = View.VISIBLE
                }
            })

        // ── Fetch user's own stats ────────────────────────────
        if (token != null) {
            RetrofitClient.instance.getMyContributionStats("Bearer $token")
                .enqueue(object : Callback<ContributorStats> {
                    override fun onResponse(
                        call: Call<ContributorStats>,
                        response: Response<ContributorStats>
                    ) {
                        if (!isAdded) return
                        val stats = response.body()
                        if (response.isSuccessful && stats != null) {
                            // ✅ Numbers only — labels are in XML already
                            tvYourCount.text = "${stats.approved_count}"
                            tvYourRank.text  = if (stats.rank != null) "#${stats.rank}" else "–"
                            tvPending.text   = "${stats.pending_count}"
                        }
                    }

                    override fun onFailure(call: Call<ContributorStats>, t: Throwable) {
                        // silently ignore — leaderboard still shows
                    }
                })
        }
    }

    companion object {
        fun newInstance() = ContributorLeaderboardFragment()
    }
}