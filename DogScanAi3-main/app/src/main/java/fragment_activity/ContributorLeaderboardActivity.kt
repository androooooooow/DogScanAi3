package fragment_activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firstapp.dogscanai.R

class ContributorLeaderboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contributor_leaderboard)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ContributorLeaderboardFragment.newInstance())
                .commit()
        }
    }
}