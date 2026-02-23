package fragment_activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dogscanai.utils.SessionManager
import com.firstapp.dogscanai.databinding.ActivityEditProfileBinding
import network.model.RetrofitClient
import network.model.UpdateProfileRequest
import network.model.UpdateProfileResponse
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
            val name = binding.etEditName.text.toString().trim()
            val email = binding.etEditEmail.text.toString().trim()
            val password = binding.etEditPassword.text.toString().trim()

            // ✅ Validate fields
            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Name and Email are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ Get and validate token
            val rawToken = sessionManager.getToken()
            if (rawToken.isNullOrEmpty()) {
                Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ Match the same token format as your other API calls
            val token = "Bearer $rawToken"

            Log.d("EditProfile", "Token: $token")
            Log.d("EditProfile", "Sending: username=$name, email=$email")

            val request = UpdateProfileRequest(
                username = name,
                email = email,
                password = if (password.isEmpty()) null else password
            )

            RetrofitClient.instance.updateProfile(token, request)
                .enqueue(object : Callback<UpdateProfileResponse> {
                    override fun onResponse(
                        call: Call<UpdateProfileResponse>,
                        response: Response<UpdateProfileResponse>
                    ) {
                        // ✅ Log everything for debugging
                        val errorBody = response.errorBody()?.string()
                        Log.d("EditProfile", "Code: ${response.code()}")
                        Log.d("EditProfile", "Body: ${response.body()}")
                        Log.d("EditProfile", "ErrorBody: $errorBody")

                        if (response.isSuccessful && response.body()?.success == true) {
                            val updatedUser = response.body()?.user
                            if (updatedUser != null) {
                                sessionManager.saveUser(updatedUser)
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    "Profile Updated Successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            } else {
                                Toast.makeText(
                                    this@EditProfileActivity,
                                    "Updated but user data missing",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            val errorMsg = when (response.code()) {
                                401 -> "Session expired. Please login again."
                                400 -> "Invalid data: $errorBody"
                                404 -> "Endpoint not found. Check API route."
                                else -> "Update Failed! ${response.code()}: $errorBody"
                            }
                            Toast.makeText(
                                this@EditProfileActivity,
                                errorMsg,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) {
                        Log.e("EditProfile", "Network error: ${t.message}")
                        Toast.makeText(
                            this@EditProfileActivity,
                            "Network Error: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }
}