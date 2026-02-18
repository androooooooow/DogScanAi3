package network.api



import com.firstapp.dogscanai.models.DogScannerResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ScannerApiService {
    @Multipart
    @POST("predict/breed")
    fun uploadDogImage(
        @Part image: MultipartBody.Part
    ): Call<DogScannerResponse>
}