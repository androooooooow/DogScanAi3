package fragment_activity

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.firstapp.dogscanai.R
import java.io.File

class DogScanResultFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dog_scan_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val breed = arguments?.getString("BREED") ?: "Unknown"
        val accuracy = arguments?.getDouble("ACCURACY") ?: 0.0
        val path = arguments?.getString("PATH")
        val details = arguments?.getString("DETAILS")

        // UI Binding
        view.findViewById<TextView>(R.id.top_breed_name).text = breed
        view.findViewById<TextView>(R.id.top_breed_accuracy).text = "${accuracy.toInt()}% Accuracy"
        view.findViewById<ProgressBar>(R.id.accuracy_progress_bar).progress = accuracy.toInt()
        view.findViewById<TextView>(R.id.other_breeds_placeholder).text = details

        // Safe Image Loading
        path?.let {
            val imgFile = File(it)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                view.findViewById<ImageView>(R.id.scanned_dog_image).setImageBitmap(bitmap)
            }
        }
    }

    companion object {
        fun newInstance(breed: String, accuracy: Double, path: String, details: String) = DogScanResultFragment().apply {
            arguments = Bundle().apply {
                putString("BREED", breed)
                putDouble("ACCURACY", accuracy)
                putString("PATH", path)
                putString("DETAILS", details)
            }
        }
    }
}