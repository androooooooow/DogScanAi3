package fragment_activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.firstapp.dogscanai.R
import kotlinx.coroutines.launch
import network.api.RetrofitClient
import network.model.SaveBreedRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class CameraActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null
    private val TAG = "CameraActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // Check Permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        }

        findViewById<Button>(R.id.capture_button).setOnClickListener {
            takePhoto()
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Gawa ng temporary file para sa picture
        val photoFile = File(externalCacheDir, "scan_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // STEP 1: I-upload at i-process ang breed
                    uploadAndProcess(photoFile)
                }
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Capture failed: ${exc.message}")
                }
            })
    }

    private fun uploadAndProcess(file: File) {
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
        val email = "user@example.com".toRequestBody("text/plain".toMediaTypeOrNull())

        lifecycleScope.launch {
            try {
                Toast.makeText(this@CameraActivity, "Processing breed...", Toast.LENGTH_SHORT).show()

                // Tawagin ang Flask API
                val response = RetrofitClient.instance.predictBreed(body, email, null)

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!

                    // STEP 2: I-save sa Database (Background)
                    val saveReq = SaveBreedRequest(
                        user_email = "user@example.com",
                        user_id = null,
                        image_name = file.name,
                        predictions = result.predictions ?: emptyList()
                    )
                    RetrofitClient.instance.saveBreedToDb(saveReq)

                    // STEP 3: LILIPAT NA SA RESULT FRAGMENT
                    navigateToResult(
                        result.topBreed ?: "Unknown",
                        result.topConfidence ?: 0.0,
                        file.absolutePath,
                        result.predictions?.drop(1)?.joinToString("\n") { "${it.breed} - ${it.confidence.toInt()}%" } ?: ""
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
                Toast.makeText(this@CameraActivity, "Server Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToResult(breed: String, conf: Double, path: String, others: String) {
        // Itago ang Camera UI elements
        findViewById<PreviewView>(R.id.previewView).visibility = View.GONE
        findViewById<Button>(R.id.capture_button).visibility = View.GONE

        // I-load ang Fragment sa container
        val fragment = DogScanResultFragment.newInstance(breed, conf, path, others)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // Para pag nag-back, babalik sa camera
            .commit()
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
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch (e: Exception) { Log.e(TAG, "Camera failed", e) }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}