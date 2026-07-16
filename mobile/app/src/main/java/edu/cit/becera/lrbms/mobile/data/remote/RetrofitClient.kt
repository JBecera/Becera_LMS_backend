package edu.cit.becera.lrbms.mobile.data.remote

import edu.cit.becera.lrbms.mobile.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/api/"

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

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
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
