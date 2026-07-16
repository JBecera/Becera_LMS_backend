package edu.cit.becera.lrbms.mobile.data.remote

import edu.cit.becera.lrbms.mobile.data.model.Transaction
import retrofit2.http.GET
import retrofit2.http.Path

interface TransactionApi {
    @GET("transactions/member/{memberId}")
    suspend fun getMemberTransactions(@Path("memberId") memberId: Long): List<Transaction>
}
