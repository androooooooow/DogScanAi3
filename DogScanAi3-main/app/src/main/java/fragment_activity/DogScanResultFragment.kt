package fragment_activity

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.firstapp.dogscanai.R
import java.io.File

class DogScanResultFragment : Fragment() {

    private val TAG = "DogScanResultFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dog_scan_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Get arguments
            val breed = arguments?.getString("BREED") ?: "Unknown"
            val accuracy = arguments?.getDouble("ACCURACY") ?: 0.0
            val path = arguments?.getString("PATH")
            val details = arguments?.getString("DETAILS") ?: "No details available"

            Log.d(TAG, "Showing result - Breed: $breed, Accuracy: $accuracy, Path: $path")

            // UI Binding
            view.findViewById<TextView>(R.id.top_breed_name)?.text = breed
            view.findViewById<TextView>(R.id.top_breed_accuracy)?.text = String.format("%.1f%% Accuracy", accuracy)
            view.findViewById<ProgressBar>(R.id.accuracy_progress_bar)?.progress = accuracy.toInt()
            view.findViewById<TextView>(R.id.other_breeds_placeholder)?.text = details

            // Load Image
            if (!path.isNullOrEmpty()) {
                val imgFile = File(path)
                if (imgFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    view.findViewById<ImageView>(R.id.scanned_dog_image)?.setImageBitmap(bitmap)
                    Log.d(TAG, "Image loaded from: $path")
                } else {
                    Log.e(TAG, "Image file not found: $path")
                    view.findViewById<ImageView>(R.id.scanned_dog_image)?.setImageResource(R.drawable.aspin)
                }
            } else {
                view.findViewById<ImageView>(R.id.scanned_dog_image)?.setImageResource(R.drawable.aspin)
            }

            // Add retry button functionality
            view.findViewById<Button>(R.id.retry_button)?.setOnClickListener {
                activity?.supportFragmentManager?.popBackStack()
            }

            // Add save button functionality
            view.findViewById<Button>(R.id.save_button)?.setOnClickListener {
                Toast.makeText(context, "Result saved", Toast.LENGTH_SHORT).show()
                // Implement save logic here
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            Toast.makeText(context, "Error displaying result", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(breed: String, accuracy: Double, path: String, details: String): DogScanResultFragment {
            return DogScanResultFragment().apply {
                arguments = Bundle().apply {
                    putString("BREED", breed)
                    putDouble("ACCURACY", accuracy)
                    putString("PATH", path)
                    putString("DETAILS", details)
                }
            }
        }
    }
}