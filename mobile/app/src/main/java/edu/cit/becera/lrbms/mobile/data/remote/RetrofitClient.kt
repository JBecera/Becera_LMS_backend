package edu.cit.becera.lrbms.mobile.data.remote

import edu.cit.becera.lrbms.mobile.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://library-system-app-sedg.onrender.com/api/"

    private const val COLD_START_RETRIES = 2
    private const val COLD_START_RETRY_DELAY_MS = 2000L

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val authInterceptor = Interceptor { chain ->
        val token = SessionManager.current?.token
        val request = if (token != null) {
            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    // Render's free tier spins the backend down when idle; the first request or two after a
    // spin-down can bounce with a network error or a bare 404 before the instance is back up.
    // Retry those a couple of times before giving up, mirroring the frontend's axios interceptor.
    private val coldStartInterceptor = Interceptor { chain ->
        val request = chain.request()
        var attempt = 0
        var lastResponse: Response? = null
        var lastError: IOException? = null

        while (attempt <= COLD_START_RETRIES) {
            if (attempt > 0) Thread.sleep(COLD_START_RETRY_DELAY_MS)
            lastResponse?.close()
            lastResponse = null
            try {
                val response = chain.proceed(request)
                if (response.code != 404 || attempt == COLD_START_RETRIES) {
                    return@Interceptor response
                }
                lastResponse = response
            } catch (e: IOException) {
                lastError = e
                if (attempt == COLD_START_RETRIES) throw e
            }
            attempt++
        }
        lastResponse ?: throw (lastError ?: IOException("Request failed after retries"))
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(coldStartInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: MemberApi by lazy { retrofit.create(MemberApi::class.java) }
    val bookApi: BookApi by lazy { retrofit.create(BookApi::class.java) }
    val transactionApi: TransactionApi by lazy { retrofit.create(TransactionApi::class.java) }
    val reservationApi: ReservationApi by lazy { retrofit.create(ReservationApi::class.java) }
    val fineApi: FineApi by lazy { retrofit.create(FineApi::class.java) }
}
