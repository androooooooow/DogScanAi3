package fragment_activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dogscanai.utils.SessionManager
import com.firstapp.dogscanai.databinding.ActivityEditProfileBinding
import network.api.RetrofitClient
import network.model.User
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

        // I-set ang current data sa fields
        binding.etEditName.setText(currentUser?.username)
        binding.etEditEmail.setText(currentUser?.email)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etEditName.text.toString().trim()
            val email = binding.etEditEmail.text.toString().trim()
            val password = binding.etEditPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Name and Email are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. KUNIN ANG TOKEN (Kailangan ito dahil sa 'protect' middleware sa Node.js)
            val token = "Bearer ${sessionManager.getToken()}"

            // 2. I-PREPARE ANG REQUEST
            val request = UpdateProfileRequest(
                username = name,
                email = email,
                password = if (password.isEmpty()) null else password
            )

            // 3. TAWAGIN ANG API NA MAY TOKEN
            RetrofitClient.instance.updateProfile(token, request).enqueue(object : Callback<UpdateProfileResponse> {
                override fun onResponse(call: Call<UpdateProfileResponse>, response: Response<UpdateProfileResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {

                        val updatedUser = response.body()?.user
                        if (updatedUser != null) {
                            // I-update ang session para magbago ang name sa Profile Fragment
                            sessionManager.saveUser(updatedUser)
                            Toast.makeText(this@EditProfileActivity, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        // Kung 401 ang error, ibig sabihin expired o kulang ang Token
                        val errorMsg = if (response.code() == 401) "Session expired. Please login again." else "Update Failed!"
                        Toast.makeText(this@EditProfileActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) {
                    Toast.makeText(this@EditProfileActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}