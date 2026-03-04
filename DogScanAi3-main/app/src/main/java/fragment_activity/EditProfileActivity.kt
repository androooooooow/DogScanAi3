package fragment_activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dogscanai.utils.SessionManager
import com.firstapp.dogscanai.databinding.ActivityEditProfileBinding
import network.api.RetrofitClient
import network.model.UpdateEmailRequest
import network.model.UpdatePasswordRequest
import network.model.UpdateUsernameRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val currentUser = sessionManager.getUser()

        // Pre-fill fields
        binding.etEditName.setText(currentUser?.username)
        binding.etEditEmail.setText(currentUser?.email)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnSaveProfile.setOnClickListener {
            val newUsername     = binding.etEditName.text.toString().trim()
            val newEmail        = binding.etEditEmail.text.toString().trim()
            val currentPassword = binding.etCurrentPassword.text.toString().trim()
            val newPassword     = binding.etEditPassword.text.toString().trim()

            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "Name and Email are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentPassword.isEmpty()) {
                Toast.makeText(this, "Current password is required to save changes", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val token = sessionManager.getBearerToken()
            if (token.isNullOrEmpty()) {
                Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val originalUsername = currentUser?.username ?: ""
            val originalEmail    = currentUser?.email ?: ""

            val usernameChanged = newUsername != originalUsername
            val emailChanged    = newEmail != originalEmail
            val passwordChanged = newPassword.isNotEmpty()

            if (!usernameChanged && !emailChanged && !passwordChanged) {
                Toast.makeText(this, "No changes detected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (usernameChanged) updateUsername(token, newUsername, currentPassword)
            if (emailChanged)    updateEmail(token, newEmail, currentPassword)
            if (passwordChanged) updatePassword(token, currentPassword, newPassword)
        }
    }

    private fun updateUsername(token: String, username: String, currentPassword: String) {
        RetrofitClient.instance.updateUsername(
            token,
            UpdateUsernameRequest(username = username, current_password = currentPassword)
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("EditProfile", "Username: ${response.code()}")
                if (response.isSuccessful) {
                    // ✅ Save updated username locally so ProfileFragment shows it immediately
                    val currentUser = sessionManager.getUser()
                    if (currentUser != null) {
                        sessionManager.saveUser(currentUser.copy(username = username))
                    }
                    Toast.makeText(this@EditProfileActivity, "Username updated!", Toast.LENGTH_SHORT).show()
                    finish() // ✅ onResume in ProfileFragment will pick up the new name
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@EditProfileActivity, "Username error: $err", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@EditProfileActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateEmail(token: String, email: String, currentPassword: String) {
        RetrofitClient.instance.updateEmail(
            token,
            UpdateEmailRequest(email = email, current_password = currentPassword)
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("EditProfile", "Email: ${response.code()}")
                if (response.isSuccessful) {
                    // ✅ Email change requires re-login (backend increments session_version)
                    Toast.makeText(this@EditProfileActivity, "Email updated! Please login again.", Toast.LENGTH_LONG).show()
                    sessionManager.clearSession()
                    finish()
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@EditProfileActivity, "Email error: $err", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@EditProfileActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updatePassword(token: String, currentPassword: String, newPassword: String) {
        RetrofitClient.instance.updatePassword(
            token,
            UpdatePasswordRequest(current_password = currentPassword, new_password = newPassword)
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("EditProfile", "Password: ${response.code()}")
                if (response.isSuccessful) {
                    // ✅ Password change requires re-login (backend increments session_version)
                    Toast.makeText(this@EditProfileActivity, "Password updated! Please login again.", Toast.LENGTH_LONG).show()
                    sessionManager.clearSession()
                    finish()
                } else {
                    val err = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@EditProfileActivity, "Password error: $err", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@EditProfileActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}