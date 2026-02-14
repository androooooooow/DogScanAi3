package fragment_activity

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
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

        val breedName = arguments?.getString("BREED_NAME") ?: "Unknown"
        val confidence = arguments?.getDouble("CONFIDENCE") ?: 0.0
        val imagePath = arguments?.getString("IMAGE_PATH")
        val others = arguments?.getString("OTHER_BREEDS")

        view.findViewById<TextView>(R.id.top_breed_name).text = breedName
        view.findViewById<TextView>(R.id.top_breed_accuracy).text = "${confidence.toInt()}%"
        view.findViewById<ProgressBar>(R.id.accuracy_progress_bar).progress = confidence.toInt()
        view.findViewById<TextView>(R.id.other_breeds_placeholder).text = others

        imagePath?.let {
            val bitmap = BitmapFactory.decodeFile(it)
            view.findViewById<ImageView>(R.id.scanned_dog_image).setImageBitmap(bitmap)
        }
    }

    companion object {
        fun newInstance(breed: String, conf: Double, path: String, others: String) = DogScanResultFragment().apply {
            arguments = Bundle().apply {
                putString("BREED_NAME", breed)
                putDouble("CONFIDENCE", conf)
                putString("IMAGE_PATH", path)
                putString("OTHER_BREEDS", others)
            }
        }
    }
}