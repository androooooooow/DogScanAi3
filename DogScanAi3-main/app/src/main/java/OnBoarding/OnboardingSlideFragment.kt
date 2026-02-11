package OnBoarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.firstapp.dogscanai.R

class OnboardingSlideFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Just inflate the layout.
        return inflater.inflate(R.layout.fragment_onboarding_slide, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The fragment's only job is to display its content.
        val title = arguments?.getString("title")
        val description = arguments?.getString("description")
        val imageResId = arguments?.getInt("imageResId")

        view.findViewById<TextView>(R.id.slide_title).text = title
        view.findViewById<TextView>(R.id.slide_description).text = description
        imageResId?.let { view.findViewById<ImageView>(R.id.slide_image).setImageResource(it) }

        // The button is referenced in the XML but not used, which is fine.
        // Or you can remove it from the XML file entirely.
    }
}