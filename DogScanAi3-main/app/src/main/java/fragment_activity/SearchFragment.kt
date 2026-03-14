package fragment_activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.res.ColorStateList
import android.graphics.Color
import com.bumptech.glide.Glide
import com.firstapp.dogscanai.utils.SessionManager
import com.firstapp.dogscanai.R
import com.firstapp.dogscanai.databinding.FragmentSearchBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import network.model.BreedResponse
import network.model.RetrofitClient

data class SearchItem(
    val id: Int,
    val title: String,
    val description: String,
    val imageRes: Int = 0,
    val imageUrl: String? = null,
    val treatment: String? = null
)

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SearchAdapter
    private lateinit var sessionManager: SessionManager
    private var isViewingBreeds = true

    private var breedList = listOf<SearchItem>()

    private val diseaseList = listOf(
        SearchItem(201, "Demodicosis", "Skin disease caused by Demodex mites.", R.drawable.demodicosis,
            treatment = "• Use medicated shampoos (Benzoyl Peroxide).\n• Topical or oral medications as prescribed by a vet.\n• Boost immune system with proper nutrition."),
        SearchItem(202, "Dermatitis", "Inflammation of the skin due to allergies.", R.drawable.dermatitis,
            treatment = "• Identify and avoid allergens.\n• Use hypoallergenic soaps.\n• Antihistamines or steroids may be required."),
        SearchItem(203, "Fungal Infections", "Issues caused by fungi like Malassezia.", R.drawable.fungal,
            treatment = "• Antifungal creams or shampoos.\n• Keep affected areas dry.\n• Oral antifungal drugs for severe cases."),
        SearchItem(204, "Hypersensitivity", "Severe allergic reactions.", R.drawable.hypersensitivity,
            treatment = "• Immediate vet consultation.\n• Elimination diet if food-related.\n• Anti-inflammatory medications."),
        SearchItem(205, "Ringworm", "Contagious fungal infection forming circles.", R.drawable.ringworm,
            treatment = "• Topical antifungal therapy.\n• Disinfect all bedding and brushes.\n• Quarantine the pet to prevent spreading."),
        SearchItem(206, "Healthy Skin", "Normal healthy dog skin.", R.drawable.aspin,
            treatment = "• Maintain regular grooming.\n• Balanced diet with Omega-3 fatty acids.\n• Monthly flea and tick prevention.")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())

        adapter = SearchAdapter(emptyList<SearchItem>()) { selectedItem ->
            showInfoPopup(selectedItem)
        }

        binding.searchRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.searchRecyclerView.adapter = adapter

        setupCategoryButtons()
        setupSearch()
        fetchBreeds()

        return binding.root
    }

    private fun fetchBreeds() {
        val token = sessionManager.getBearerToken() ?: return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getBreeds(token)
                if (response.isSuccessful) {
                    val breeds = response.body() ?: emptyList<BreedResponse>()

                    breedList = breeds.map { breed ->
                        val imageUrl = "http://192.168.137.1:5000${breed.image_url}"  // ✅ fixed IP
                        android.util.Log.d("BreedImage", "Loading: $imageUrl")
                        SearchItem(
                            id = breed.breed_id,
                            title = breed.display_name,
                            description = breed.description,
                            imageUrl = imageUrl
                        )
                    }

                    if (isViewingBreeds) {
                        adapter.filterList(breedList)
                    }
                } else {
                    Toast.makeText(context, "Failed to load breeds", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showInfoPopup(item: SearchItem) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_info_popup, null)

        val img = dialogView.findViewById<ImageView>(R.id.popupImage)
        val title = dialogView.findViewById<TextView>(R.id.popupTitle)
        val desc = dialogView.findViewById<TextView>(R.id.popupDescription)
        val treat = dialogView.findViewById<TextView>(R.id.popupTreatment)

        if (item.imageUrl != null) {
            Glide.with(requireContext())
                .load(item.imageUrl)
                .placeholder(R.drawable.aspin)
                .error(R.drawable.aspin)
                .into(img)
        } else {
            img.setImageResource(item.imageRes)
        }

        title.text = item.title
        desc.text = item.description

        if (item.treatment != null) {
            treat.visibility = View.VISIBLE
            treat.text = "Recommended Treatment:\n${item.treatment}"
        } else {
            treat.visibility = View.GONE
        }

        builder.setView(dialogView)
        builder.setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun setupCategoryButtons() {
        binding.btnCategoryBreeds.setOnClickListener {
            isViewingBreeds = true
            updateCategoryUI(true)
            adapter.filterList(breedList)
            binding.searchEditText.hint = "Search dog breeds..."
            binding.searchEditText.text.clear()
        }

        binding.btnCategoryDiseases.setOnClickListener {
            isViewingBreeds = false
            updateCategoryUI(false)
            adapter.filterList(diseaseList)
            binding.searchEditText.hint = "Search skin diseases..."
            binding.searchEditText.text.clear()
        }
    }

    private fun updateCategoryUI(breedsSelected: Boolean) {
        val activeColor = Color.parseColor("#4A69FF")
        val inactiveColor = Color.parseColor("#E0E0E0")

        if (breedsSelected) {
            binding.btnCategoryBreeds.backgroundTintList = ColorStateList.valueOf(activeColor)
            binding.btnCategoryBreeds.setTextColor(Color.WHITE)
            binding.btnCategoryDiseases.backgroundTintList = ColorStateList.valueOf(inactiveColor)
            binding.btnCategoryDiseases.setTextColor(Color.BLACK)
        } else {
            binding.btnCategoryDiseases.backgroundTintList = ColorStateList.valueOf(activeColor)
            binding.btnCategoryDiseases.setTextColor(Color.WHITE)
            binding.btnCategoryBreeds.backgroundTintList = ColorStateList.valueOf(inactiveColor)
            binding.btnCategoryBreeds.setTextColor(Color.BLACK)
        }
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val currentFullList = if (isViewingBreeds) breedList else diseaseList
                val filteredList = currentFullList.filter {
                    it.title.contains(s.toString(), ignoreCase = true)
                }
                adapter.filterList(filteredList)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}