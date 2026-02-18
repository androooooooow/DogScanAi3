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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class CameraActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null
    private val TAG = "CameraActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        }

        findViewById<Button>(R.id.capture_button).setOnClickListener {
            takePhoto()
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
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch (e: Exception) { Log.e(TAG, "Camera Error", e) }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(externalCacheDir, "scan.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    uploadImage(photoFile)
                }
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo failed: ${exc.message}")
                }
            })
    }

    private fun uploadImage(file: File) {
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        // Dito natin makikita kung papasok sa Python
        Log.d(TAG, "Sending request to: ${RetrofitClient.instance}")

        RetrofitClient.instance.scanDog(body).enqueue(object : Callback<DogScannerResponse> {
            override fun onResponse(call: Call<DogScannerResponse>, response: Response<DogScannerResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    showResult(response.body()!!, file.absolutePath)
                } else {
                    Toast.makeText(this@CameraActivity, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DogScannerResponse>, t: Throwable) {
                Log.e(TAG, "FAILED TO CONNECT: ${t.message}")
                Toast.makeText(this@CameraActivity, "Check Laptop IP & Firewall!", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showResult(data: DogScannerResponse, path: String) {
        // 1. Itago ang Camera Layout
        findViewById<PreviewView>(R.id.previewView).visibility = View.GONE
        findViewById<Button>(R.id.capture_button).visibility = View.GONE

        // 2. I-show ang Result Fragment sa fragment_container
        val fragment = DogScanResultFragment.newInstance(
            data.breed ?: "Unknown",
            (data.confidence ?: 0.0) * 100,
            path,
            "Age: ${data.age?.age ?: "N/A"}"
        )

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
}