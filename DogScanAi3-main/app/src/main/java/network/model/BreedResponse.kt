package network.model

data class BreedResponse(
    val breed_id: Int,
    val class_name: String,
    val display_name: String,
    val image_url: String,
    val size: String,
    val description: String
)