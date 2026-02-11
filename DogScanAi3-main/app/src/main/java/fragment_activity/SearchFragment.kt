package fragment_activity



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.firstapp.dogscanai.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        setupSearch()

        return binding.root
    }

    private fun setupSearch() {
        binding.searchButton.setOnClickListener {
            val query = binding.searchEditText.text.toString().trim()

            if (query.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter search term", Toast.LENGTH_SHORT).show()
            } else {
                performSearch(query)
            }
        }
    }

    private fun performSearch(query: String) {
        // Simple search logic
        binding.resultText.text = "Searching for: $query"
        Toast.makeText(requireContext(), "Search: $query", Toast.LENGTH_SHORT).show()

        // TODO: Add your actual search logic here
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}