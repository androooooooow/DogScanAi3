package fragment_activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.dogscanai.utils.SessionManager
import com.firstapp.dogscanai.accounts.LoginActivity
import com.firstapp.dogscanai.databinding.ActivityProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // --- DYNAMIC DATA DISPLAY ---
        val user = sessionManager.getUser()
        if (user != null) {
            binding.userName.text = user.name
            binding.userEmail.text = user.email
        }

        // --- BUTTON LISTENERS ---
        binding.btnLogout.setOnClickListener {
            performLogout()
        }

        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogout() {
        sessionManager.clearSession()

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}