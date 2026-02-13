package fragment_activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firstapp.dogscanai.R
import com.firstapp.dogscanai.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SearchAdapter
    private var isViewingBreeds = true

    private val breedList = listOf(
        SearchItem(1, "Affenpinscher", "Monkey-faced toy terrier", R.drawable.aso_1),
        SearchItem(2, "Afghan Hound", "Aristocratic sighthound", R.drawable.aso2),
        SearchItem(3, "African Hunting Dog", "Painted wild dog", R.drawable.aso3),
        SearchItem(4, "Airedale Terrier", "King of terriers", R.drawable.aso4),
        SearchItem(5, "American Staffordshire Terrier", "Loyal terrier", R.drawable.aso5),
        SearchItem(6, "Appenzeller", "Swiss cattle herder", R.drawable.aso6),
        SearchItem(7, "Aspin", "Philippine street dog", R.drawable.aspin),
        SearchItem(8, "Australian Terrier", "Spirited small terrier", R.drawable.aso7),
        SearchItem(9, "Basenji", "Barkless African dog", R.drawable.aso8),
        SearchItem(10, "Basset Hound", "Low-slung scent hound", R.drawable.aso9),
        SearchItem(11, "Beagle", "Merry scent hound", R.drawable.aso10),
        SearchItem(12, "Bedlington Terrier", "Lamb-like terrier", R.drawable.aso11),
        SearchItem(13, "Bernese Mountain Dog", "Swiss mountain dog", R.drawable.aso12),
        SearchItem(14, "Black and Tan Coonhound", "American tracker", R.drawable.aso13),
        SearchItem(15, "Blenheim Spaniel", "Ruby and white toy spaniel", R.drawable.aso14),
        SearchItem(16, "Bloodhound", "Supreme tracking hound", R.drawable.aso15),
        SearchItem(17, "Bluetick Coonhound", "Speckled hunting dog", R.drawable.aso16),
        SearchItem(18, "Border Collie", "Supreme herding dog", R.drawable.aso17),
        SearchItem(19, "Border Terrier", "Working terrier", R.drawable.aso18),
        SearchItem(20, "Borzoi", "Russian sighthound", R.drawable.aso19),
        SearchItem(21, "Boston Bull Terrier", "American gentleman", R.drawable.aso20),
        SearchItem(22, "Bouvier des Flandres", "Cattle herder", R.drawable.aso21),
        SearchItem(23, "Boxer", "Playful guardian", R.drawable.aso22),
        SearchItem(24, "Brabancon Griffon", "Brussels toy griffon", R.drawable.aso23),
        SearchItem(25, "Briard", "French shepherd", R.drawable.aso24),
        SearchItem(26, "Brittany Spaniel", "Compact pointer", R.drawable.aso25),
        SearchItem(27, "Bull Mastiff", "British guardian", R.drawable.aso26),
        SearchItem(28, "Cairn Terrier", "Shaggy Scottish terrier", R.drawable.aso27),
        SearchItem(29, "Cardigan Welsh Corgi", "Tailed Welsh corgi", R.drawable.aso28),
        SearchItem(30, "Chesapeake Bay Retriever", "Waterproof retriever", R.drawable.aso29),
        SearchItem(31, "Chihuahua", "Tiny dog, big personality", R.drawable.aso30),
        SearchItem(32, "Chow Chow", "Blue-tongued Chinese dog", R.drawable.aso31),
        SearchItem(33, "Clumber Spaniel", "Heavy land spaniel", R.drawable.aso32),
        SearchItem(34, "Cocker Spaniel", "Merry American spaniel", R.drawable.aso33),
        SearchItem(35, "Collie", "Scottish herding beauty", R.drawable.aso34),
        SearchItem(36, "Curly-coated Retriever", "Curly hunter", R.drawable.aso35),
        SearchItem(37, "Dandie Dinmont Terrier", "Unique long terrier", R.drawable.aso36),
        SearchItem(38, "Dhole", "Asian wild dog", R.drawable.aso37),
        SearchItem(39, "Dingo", "Australian wild dog", R.drawable.aso38),
        SearchItem(40, "Doberman Pinscher", "Sleek German guardian", R.drawable.aso39),
        SearchItem(41, "English Foxhound", "Pack hunting dog", R.drawable.aso40),
        SearchItem(42, "English Setter", "Feathered setter", R.drawable.aso41),
        SearchItem(43, "English Springer Spaniel", "Energetic spaniel", R.drawable.aso42),
        SearchItem(44, "Entlebucher", "Swiss mountain dog", R.drawable.aso43),
        SearchItem(45, "Eskimo Dog", "Arctic sled dog", R.drawable.aso44),
        SearchItem(46, "Flat-coated Retriever", "Black hunting retriever", R.drawable.aso45),
        SearchItem(47, "French Bulldog", "Bat-eared companion", R.drawable.aso46),
        SearchItem(48, "German Shepherd", "Versatile working dog", R.drawable.aso47),
        SearchItem(49, "German Short-haired Pointer", "German hunter", R.drawable.aso48),
        SearchItem(50, "Giant Schnauzer", "Powerful schnauzer", R.drawable.aso49),
        SearchItem(51, "Golden Retriever", "Friendly family dog", R.drawable.aso50),
        SearchItem(52, "Gordon Setter", "Black and tan setter", R.drawable.aso51),
        SearchItem(53, "Great Dane", "Gentle German giant", R.drawable.aso52),
        SearchItem(54, "Great Pyrenees", "White mountain guardian", R.drawable.aso53),
        SearchItem(55, "Greater Swiss Mountain Dog", "Swiss draft dog", R.drawable.aso54),
        SearchItem(56, "Groenendael", "Belgian black shepherd", R.drawable.aso55),
        SearchItem(57, "Ibizan Hound", "Ancient Egyptian hound", R.drawable.aso56),
        SearchItem(58, "Irish Setter", "Mahogany red setter", R.drawable.aso57),
        SearchItem(59, "Irish Terrier", "Red daredevil terrier", R.drawable.aso58),
        SearchItem(60, "Irish Water Spaniel", "Curly water retriever", R.drawable.aso59),
        SearchItem(61, "Irish Wolfhound", "Gentle giant sighthound", R.drawable.aso60),
        SearchItem(62, "Italian Greyhound", "Miniature sighthound", R.drawable.aso61),
        SearchItem(63, "Japanese Spaniel", "Elegant toy companion", R.drawable.aso62),
        SearchItem(64, "Keeshond", "Dutch barge dog", R.drawable.aso63),
        SearchItem(65, "Kelpie", "Australian working dog", R.drawable.aso64),
        SearchItem(66, "Kerry Blue Terrier", "Irish blue terrier", R.drawable.aso65),
        SearchItem(67, "Komondor", "Hungarian corded dog", R.drawable.aso66),
        SearchItem(68, "Kuvasz", "Hungarian white guardian", R.drawable.aso67),
        SearchItem(69, "Labrador Retriever", "Favorite family dog", R.drawable.aso68),
        SearchItem(70, "Lakeland Terrier", "Narrow-bodied terrier", R.drawable.aso69),
        SearchItem(71, "Leonberg", "German lion-like dog", R.drawable.aso70),
        SearchItem(72, "Lhasa Apso", "Tibetan sentinel", R.drawable.aso71),
        SearchItem(73, "Malamute", "Alaskan freight hauler", R.drawable.aso72),
        SearchItem(74, "Malinois", "Belgian working shepherd", R.drawable.aso73),
        SearchItem(75, "Maltese Dog", "Gentle white toy breed", R.drawable.aso74),
        SearchItem(76, "Mexican Hairless", "Ancient hairless dog", R.drawable.aso75),
        SearchItem(77, "Miniature Pinscher", "King of toys", R.drawable.aso76),
        SearchItem(78, "Miniature Poodle", "Medium-sized poodle", R.drawable.aso77),
        SearchItem(79, "Miniature Schnauzer", "Bearded German terrier", R.drawable.aso78),
        SearchItem(80, "Newfoundland", "Canadian water dog", R.drawable.aso79),
        SearchItem(81, "Norfolk Terrier", "Small fearless terrier", R.drawable.aso80),
        SearchItem(82, "Norwegian Elkhound", "Viking companion", R.drawable.aso81),
        SearchItem(83, "Norwich Terrier", "Prick-eared terrier", R.drawable.aso82),
        SearchItem(84, "Old English Sheepdog", "Shaggy bobtail herder", R.drawable.aso83),
        SearchItem(85, "Otterhound", "Water hunting dog", R.drawable.aso84),
        SearchItem(86, "Papillon", "Butterfly-eared toy", R.drawable.aso85),
        SearchItem(87, "Pekinese", "Chinese imperial dog", R.drawable.aso86),
        SearchItem(88, "Pembroke Welsh Corgi", "Short-legged herder", R.drawable.aso87),
        SearchItem(89, "Pomeranian", "Fluffy toy spitz", R.drawable.aso88),
        SearchItem(90, "Pug", "Chinese wrinkled toy", R.drawable.aso89),
        SearchItem(91, "Redbone Coonhound", "Red hunting dog", R.drawable.aso90),
        SearchItem(92, "Rhodesian Ridgeback", "Lion hunting dog", R.drawable.aso91),
        SearchItem(93, "Rottweiler", "Powerful guardian", R.drawable.aso92),
        SearchItem(94, "Saint Bernard", "Swiss rescue giant", R.drawable.aso93),
        SearchItem(95, "Saluki", "Ancient desert sighthound", R.drawable.aso94),
        SearchItem(96, "Samoyed", "Smiling white sled dog", R.drawable.aso95),
        SearchItem(97, "Schipperke", "Belgian barge dog", R.drawable.aso96),
        SearchItem(98, "Scotch Terrier", "Independent terrier", R.drawable.aso97),
        SearchItem(99, "Scottish Deerhound", "Coursing sighthound", R.drawable.aso98),
        SearchItem(100, "Sealyham Terrier", "Welsh white terrier", R.drawable.aso99),
        SearchItem(101, "Shetland Sheepdog", "Miniature herder", R.drawable.aso100),
        SearchItem(102, "Shih-Tzu", "Chrysanthemum companion", R.drawable.aso101),
        SearchItem(103, "Siberian Husky", "Russian sled dog", R.drawable.aso102),
        SearchItem(104, "Silky Terrier", "Australian toy terrier", R.drawable.aso103),
        SearchItem(105, "Soft-coated Wheaten Terrier", "Wheat-colored terrier", R.drawable.aso104),
        SearchItem(106, "Staffordshire Bull Terrier", "Muscular terrier", R.drawable.aso105),
        SearchItem(107, "Standard Poodle", "French water dog", R.drawable.aso106),
        SearchItem(108, "Standard Schnauzer", "Original schnauzer", R.drawable.aso107),
        SearchItem(109, "Sussex Spaniel", "Golden liver spaniel", R.drawable.aso108),
        SearchItem(110, "Tibetan Mastiff", "Himalayan guardian", R.drawable.aso109),
        SearchItem(111, "Tibetan Terrier", "Tibetan companion", R.drawable.aso110),
        SearchItem(112, "Toy Poodle", "Elegant toy poodle", R.drawable.aso111),
        SearchItem(113, "Toy Terrier", "Spirited toy terrier", R.drawable.aso112),
        SearchItem(114, "Vizsla", "Hungarian golden pointer", R.drawable.aso113),
        SearchItem(115, "Walker Hound", "Fast American foxhound", R.drawable.aso114),
        SearchItem(116, "Weimaraner", "Gray ghost hunter", R.drawable.aso115),
        SearchItem(117, "Welsh Springer Spaniel", "Red and white spaniel", R.drawable.aso116),
        SearchItem(118, "West Highland White Terrier", "White Scottish terrier", R.drawable.aso117),
        SearchItem(119, "Whippet", "Racing sighthound", R.drawable.aso118),
        SearchItem(120, "Wire-haired Fox Terrier", "Wiry hunting terrier", R.drawable.aso119),
        SearchItem(121, "Yorkshire Terrier", "Glamorous toy terrier", R.drawable.aso120)
    )

    private val diseaseList = listOf(
        SearchItem(201, "Demodicosis", "Skin disease caused by Demodex mites.", R.drawable.demodicosis),
        SearchItem(202, "Dermatitis", "Inflammation of the skin.", R.drawable.dermatitis),
        SearchItem(203, "Fungal Infections", "Issues caused by fungi.", R.drawable.fungal),
        SearchItem(204, "Hypersensitivity", "Allergic reactions.", R.drawable.hypersensitivity),
        SearchItem(205, "Ringworm", "Contagious fungal infection.", R.drawable.ringworm),
        SearchItem(206, "Healthy Skin", "Normal healthy dog skin.", R.drawable.aspin)
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        adapter = SearchAdapter(breedList)
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.searchRecyclerView.adapter = adapter

        setupCategoryButtons()
        setupSearch()

        return binding.root
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