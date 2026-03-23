package fragment_activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.firstapp.dogscanai.R
import com.firstapp.dogscanai.models.DogScannerResponse
import com.firstapp.dogscanai.views.ScanFrameView
import network.api.FlaskClient
import network.model.DiseaseResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

class CameraActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null
    private val TAG = "CameraActivity"
    private val CAMERA_REQUEST_CODE = 101
    private val GALLERY_REQUEST_CODE = 102
    private var isBreedMode = true

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

        setupButtons()
        updateToggleUI()
    }

    private fun setupButtons() {
        // Mode toggle
        findViewById<Button>(R.id.btn_breed).setOnClickListener {
            isBreedMode = true
            updateToggleUI()
        }
        findViewById<Button>(R.id.btn_disease).setOnClickListener {
            isBreedMode = false
            updateToggleUI()
        }

        // Capture
        findViewById<ImageButton>(R.id.capture_button).setOnClickListener {
            takePhoto()
        }

        // Upload from gallery
        findViewById<LinearLayout>(R.id.upload_button).setOnClickListener {
            openGallery()
        }

        // History — ✅ new button
        findViewById<LinearLayout>(R.id.history_button).setOnClickListener {
            startActivity(Intent(this, ScanHistoryActivity::class.java))
        }

    }

    private fun updateToggleUI() {
        val btnBreed      = findViewById<Button>(R.id.btn_breed)
        val btnDisease    = findViewById<Button>(R.id.btn_disease)
        val scanHint      = findViewById<TextView>(R.id.scan_hint)
        val captureLabel  = findViewById<TextView>(R.id.capture_label)
        val tvTipTitle    = findViewById<TextView>(R.id.tv_tip_title)
        val tvTipSubtitle = findViewById<TextView>(R.id.tv_tip_subtitle)

        if (isBreedMode) {
            // Breed active — blue
            btnBreed.setBackgroundResource(R.drawable.bg_toggle_active_breed)
            btnBreed.setTextColor(android.graphics.Color.WHITE)

            // Disease inactive
            btnDisease.setBackgroundResource(android.R.color.transparent)
            btnDisease.setTextColor(android.graphics.Color.parseColor("#88888888"))

            scanHint.text      = "Position dog here"
            captureLabel.text  = "SCAN BREED"
            tvTipTitle.text    = "Breed scan"
            tvTipSubtitle.text = "Full body visible works best"

        } else {
            // Disease active — red
            btnDisease.setBackgroundResource(R.drawable.bg_toggle_active_disease)
            btnDisease.setTextColor(android.graphics.Color.WHITE)

            // Breed inactive
            btnBreed.setBackgroundResource(android.R.color.transparent)
            btnBreed.setTextColor(android.graphics.Color.parseColor("#88888888"))

            scanHint.text      = "Focus on skin/coat area"
            captureLabel.text  = "SCAN DISEASE"
            tvTipTitle.text    = "Disease scan"
            tvTipSubtitle.text = "Close-up of affected area"
        }
    }

    // ─── Image helpers ───────────────────────────────────────────────────────

    private fun compressAndEncode(file: File): String {
        val original = BitmapFactory.decodeFile(file.absolutePath)

        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90       -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180      -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270      -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL   -> matrix.preScale(1f, -1f)
        }
        val rotated = Bitmap.createBitmap(
            original, 0, 0, original.width, original.height, matrix, true
        )

        val maxSize = 512
        val scale = minOf(
            maxSize.toFloat() / rotated.width,
            maxSize.toFloat() / rotated.height,
            1f
        )
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                rotated,
                (rotated.width  * scale).toInt(),
                (rotated.height * scale).toInt(),
                true
            )
        } else rotated

        val stream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        Log.d(TAG, "Compressed: ${stream.size()} bytes (scale: $scale)")

        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    private fun compressAndEncodeFromBytes(bytes: ByteArray, tempFile: File): String {
        tempFile.writeBytes(bytes)
        return compressAndEncode(tempFile)
    }

    // ─── Gallery ─────────────────────────────────────────────────────────────

    private fun openGallery() {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(
                Intent.createChooser(intent, "Select Image"),
                GALLERY_REQUEST_CODE
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open gallery", e)
            Toast.makeText(this, "No gallery app found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (allPermissionsGranted()) startCamera()
            else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            val imageUri: Uri? = data?.data
            if (imageUri != null) processImageFromUri(imageUri)
            else Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processImageFromUri(uri: Uri) {
        try {
            Toast.makeText(this, "Loading image...", Toast.LENGTH_SHORT).show()

            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null && bytes.isNotEmpty()) {
                val tempFile = File(externalCacheDir, "upload_${System.currentTimeMillis()}.jpg")
                val base64 = compressAndEncodeFromBytes(bytes, tempFile)

                if (isBreedMode) uploadBreedImage(base64, tempFile.absolutePath)
                else             uploadDiseaseImage(base64, tempFile.absolutePath)
            } else {
                Toast.makeText(this, "Could not read image, please try another", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading gallery image", e)
            Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ─── Camera ──────────────────────────────────────────────────────────────

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(
                    findViewById<PreviewView>(R.id.previewView).surfaceProvider
                )
            }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera bind error", e)
                Toast.makeText(this, "Failed to start camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: run {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show()
            return
        }

        val photoFile     = File(externalCacheDir, "scan_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val base64 = compressAndEncode(photoFile)
                    if (isBreedMode) uploadBreedImage(base64, photoFile.absolutePath)
                    else             uploadDiseaseImage(base64, photoFile.absolutePath)
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo failed: ${exc.message}", exc)
                    Toast.makeText(this@CameraActivity, "Failed to take photo", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // ─── API calls ───────────────────────────────────────────────────────────

    private fun uploadBreedImage(base64: String, imagePath: String) {
        Toast.makeText(this, "Identifying breed...", Toast.LENGTH_SHORT).show()

        FlaskClient.instance.scanDog(mapOf("image" to base64))
            .enqueue(object : Callback<DogScannerResponse> {
                override fun onResponse(
                    call: Call<DogScannerResponse>,
                    response: Response<DogScannerResponse>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        if (data != null) showBreedResult(data, imagePath)
                        else Toast.makeText(this@CameraActivity, "Empty response", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "Breed error: ${response.code()} - ${response.errorBody()?.string()}")
                        Toast.makeText(this@CameraActivity, "Please Upload or Scan a Real Dog (${response.code()})", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<DogScannerResponse>, t: Throwable) {
                    Log.e(TAG, "Network failure", t)
                    Toast.makeText(this@CameraActivity, "Please Check Your FlaskAPI: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun uploadDiseaseImage(base64: String, imagePath: String) {
        Toast.makeText(this, "Scanning for disease...", Toast.LENGTH_SHORT).show()

        FlaskClient.instance.scanDisease(mapOf("image" to base64))
            .enqueue(object : Callback<DiseaseResponse> {
                override fun onResponse(
                    call: Call<DiseaseResponse>,
                    response: Response<DiseaseResponse>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        if (data != null) showDiseaseResult(data, imagePath)
                        else Toast.makeText(this@CameraActivity, "Empty response", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "Disease error: ${response.code()} - ${response.errorBody()?.string()}")
                        Toast.makeText(this@CameraActivity, "Server error (${response.code()})", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<DiseaseResponse>, t: Throwable) {
                    Log.e(TAG, "Network failure", t)
                    Toast.makeText(this@CameraActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    // ─── Show results ────────────────────────────────────────────────────────

    private fun showBreedResult(data: DogScannerResponse, path: String) {
        try {
            hideCamera()

            val top       = data.top_breeds.firstOrNull()
            val breed     = top?.display_name ?: top?.class_name ?: "Unknown"
            val confidence = top?.confidence ?: 0.0
            val className  = top?.class_name ?: ""
            val breedId    = top?.breed_id?.toIntOrNull() ?: -1

            Log.d(TAG, ">>> top_breeds[0] = ${data.top_breeds[0]}")
            Log.d(TAG, ">>> breed_id raw = ${top?.breed_id}")

            val allBreeds = data.top_breeds.mapIndexed { i, b ->
                "${i + 1}|${b.class_name ?: ""}|${b.display_name ?: b.class_name ?: "Unknown"}|${b.confidence ?: 0.0}|${b.breed_id ?: ""}"
            }.joinToString(";;")

            val details = buildString {
                append("Result Type: ${data.result_type}\n")
                if (data.reasons?.isNotEmpty() == true)
                    append("Reasons: ${data.reasons.joinToString()}\n")
                data.top_breeds.forEachIndexed { i, b ->
                    append("\n${i + 1}. ${b.display_name ?: b.class_name} (${b.confidence}%)")
                }
                append("\n\nEmotion: ${data.emotion?.display_name ?: "Unknown"}")
                append("\nAge: ${data.age?.display_name ?: "Unknown"}")
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DogScanResultFragment.newInstance(
                    breed     = breed,
                    accuracy  = confidence,
                    path      = path,
                    details   = details,
                    scanType  = "breed",
                    className = className,
                    breedId   = breedId,
                    allBreeds = allBreeds
                ))
                .addToBackStack("result")
                .commit()

        } catch (e: Exception) {
            Log.e(TAG, "Error showing breed result", e)
            Toast.makeText(this, "Error displaying result", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDiseaseResult(data: DiseaseResponse, path: String) {
        try {
            hideCamera()

            val top = data.top_diseases.firstOrNull()

            val allDiseases = data.top_diseases.mapIndexed { i, d ->
                "${i + 1}|${d.class_name ?: ""}|${d.display_name ?: d.class_name ?: "Unknown"}|${d.confidence ?: 0.0}|"
            }.joinToString(";;")

            val details = buildString {
                append("Description: ${top?.description ?: "N/A"}\n\n")
                append("Treatment: ${top?.treatment ?: "Consult a veterinarian."}\n\n")
                append("Severity: ${top?.severity ?: "Unknown"}\n\n")
                if (data.top_diseases.size > 1) {
                    append("Other Possibilities:\n")
                    data.top_diseases.drop(1).forEachIndexed { i, d ->
                        append("${i + 2}. ${d.display_name ?: d.class_name} (${d.confidence}%)\n")
                    }
                }
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DogScanResultFragment.newInstance(
                    breed     = top?.display_name ?: top?.class_name ?: "Unknown",
                    accuracy  = top?.confidence   ?: 0.0,
                    path      = path,
                    details   = details,
                    scanType  = "disease",
                    className = top?.class_name   ?: "",
                    allBreeds = allDiseases
                ))
                .addToBackStack("result")
                .commit()

        } catch (e: Exception) {
            Log.e(TAG, "Error showing disease result", e)
            Toast.makeText(this, "Error displaying result", Toast.LENGTH_SHORT).show()
        }
    }

    // ─── Hide camera UI when showing results ─────────────────────────────────

    private fun hideCamera() {
        findViewById<PreviewView>(R.id.previewView).visibility     = View.GONE
        findViewById<View>(R.id.bottom_actions).visibility         = View.GONE
        findViewById<View>(R.id.filter_toggle).visibility          = View.GONE
        findViewById<ScanFrameView>(R.id.scan_frame).visibility    = View.GONE
        findViewById<TextView>(R.id.scan_hint).visibility          = View.GONE
        findViewById<View>(R.id.tip_card).visibility               = View.GONE
        findViewById<View>(R.id.gradient_top).visibility           = View.GONE
        findViewById<View>(R.id.gradient_bottom).visibility        = View.GONE
        findViewById<View>(R.id.tv_app_name).visibility            = View.GONE

    }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
}