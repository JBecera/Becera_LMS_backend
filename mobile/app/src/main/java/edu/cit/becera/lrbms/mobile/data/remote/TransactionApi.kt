package edu.cit.becera.lrbms.mobile.data.remote

import edu.cit.becera.lrbms.mobile.data.model.CheckoutRequest
import edu.cit.becera.lrbms.mobile.data.model.SelfCheckoutRequest
import edu.cit.becera.lrbms.mobile.data.model.Transaction
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TransactionApi {
    @GET("transactions")
    suspend fun getAllTransactions(): List<Transaction>

    @GET("transactions/member/{memberId}")
    suspend fun getMemberTransactions(@Path("memberId") memberId: Long): List<Transaction>

    @POST("transactions/checkout")
    suspend fun checkout(@Body request: CheckoutRequest): Transaction

    @POST("transactions/self-checkout")
    suspend fun selfCheckout(@Body request: SelfCheckoutRequest): Transaction

    @POST("transactions/{id}/checkin")
    suspend fun checkIn(@Path("id") id: Long): Transaction
}
