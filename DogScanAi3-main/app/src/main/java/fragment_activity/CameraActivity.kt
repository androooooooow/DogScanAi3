package fragment_activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.firstapp.dogscanai.R
import com.firstapp.dogscanai.models.DogScannerResponse
import network.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import android.util.Base64
import java.io.FileInputStream

class CameraActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null
    private val TAG = "CameraActivity"
    private val CAMERA_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        }

        findViewById<Button>(R.id.capture_button).setOnClickListener {
            takePhoto()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(findViewById<PreviewView>(R.id.previewView).surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera Error", e)
                Toast.makeText(this, "Failed to start camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: run {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show()
            return
        }

        // Create unique filename with timestamp
        val fileName = "scan_${System.currentTimeMillis()}.jpg"
        val photoFile = File(externalCacheDir, fileName)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Photo saved: ${photoFile.absolutePath}")
                    uploadImage(photoFile)
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo failed: ${exc.message}", exc)
                    Toast.makeText(this@CameraActivity, "Failed to take photo", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun uploadImage(file: File) {
        // Show loading indicator
        Toast.makeText(this, "Processing image...", Toast.LENGTH_SHORT).show()

        try {
            // Convert image to base64
            val bytes = file.readBytes()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

            Log.d(TAG, "Image size: ${bytes.size} bytes")
            Log.d(TAG, "Base64 length: ${base64.length}")

            // Create JSON body with base64 image
            val body = mapOf("image" to base64)

            // Make API call
            RetrofitClient.instance.scanDog(body).enqueue(object : Callback<DogScannerResponse> {
                override fun onResponse(
                    call: Call<DogScannerResponse>,
                    response: Response<DogScannerResponse>
                ) {
                    Log.d(TAG, "Response code: ${response.code()}")

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            Log.d(TAG, "Response successful: $body")
                            showResult(body, file.absolutePath)
                        } else {
                            Toast.makeText(
                                this@CameraActivity,
                                "Empty response from server",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Server error: ${response.code()} - $errorBody")
                        Toast.makeText(
                            this@CameraActivity,
                            "Server Error (${response.code()}): Please check connection",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DogScannerResponse>, t: Throwable) {
                    Log.e(TAG, "Network failure", t)
                    Toast.makeText(
                        this@CameraActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image", e)
            Toast.makeText(
                this@CameraActivity,
                "Error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showResult(data: DogScannerResponse, path: String) {
        try {
            // Hide Camera Layout
            findViewById<PreviewView>(R.id.previewView).visibility = View.GONE
            findViewById<Button>(R.id.capture_button).visibility = View.GONE

            // Extract data from response
            val breed = if (data.result_type == "pure_breed" && data.top_breeds.isNotEmpty()) {
                data.top_breeds[0].display_name ?: data.top_breeds[0].class_name ?: "Unknown"
            } else {
                "Mixed Breed"
            }

            val confidence = if (data.top_breeds.isNotEmpty()) {
                data.top_breeds[0].confidence ?: 0.0
            } else {
                0.0
            }

            // Create details string
            val details = buildString {
                append("Result Type: ${data.result_type}\n")
                if (data.reasons?.isNotEmpty() == true) {
                    append("Reasons: ${data.reasons.joinToString()}\n")
                }
                data.top_breeds.forEachIndexed { index, breed ->
                    append("\n${index + 1}. ${breed.display_name ?: breed.class_name}")
                    append(" (${breed.confidence}%)")
                }
                append("\n\nEmotion: ${data.emotion?.display_name ?: "Unknown"}")
                append("\nAge: ${data.age?.display_name ?: "Unknown"}")
            }

            // Show Result Fragment
            val fragment = DogScanResultFragment.newInstance(
                breed = breed,
                accuracy = confidence,
                path = path,
                details = details
            )

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("scan_result")
                .commit()

            Log.d(TAG, "Result fragment shown")

        } catch (e: Exception) {
            Log.e(TAG, "Error showing result", e)
            Toast.makeText(this, "Error displaying result", Toast.LENGTH_SHORT).show()
        }
    }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}