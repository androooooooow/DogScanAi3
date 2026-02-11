package network.model

// PALITAN ANG MGA IMPORT NA ITO:
// import com.firstapp.dogscanai.ApiService  // REMOVE THIS
// import okhttp3.Logging.HttpLoggingInterceptor  // REMOVE THIS

// ITO ANG TAMANG IMPORTS:
import network.model.ApiService  // ✅ TAMA NA ITO
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor  // ✅ TAMA NA ITO
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ✅ PASTE ITO: Gamitin 'to kasi hotspot ang phone mo
    private const val BASE_URL = "http://192.168.137.1:5000/"

    private var retrofit: Retrofit? = null

    private val requestInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        // Add token if available
        val token = AuthManager.getToken()
        token?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }

        val request = requestBuilder.build()
        chain.proceed(request)
    }

    fun getClient(): ApiService {
        if (retrofit == null) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(requestInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val gson = GsonBuilder()
                .setLenient()
                .create()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)  // ✅ Dito gagamitin ang BASE_URL
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            println("📡 Using base URL: $BASE_URL")
        }

        return retrofit!!.create(ApiService::class.java)
    }
}