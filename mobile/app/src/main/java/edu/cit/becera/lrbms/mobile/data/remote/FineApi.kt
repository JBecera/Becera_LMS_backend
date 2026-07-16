package edu.cit.becera.lrbms.mobile.data.remote

import edu.cit.becera.lrbms.mobile.data.model.Fine
import retrofit2.http.GET
import retrofit2.http.Path

interface FineApi {
    @GET("fines/member/{memberId}")
    suspend fun getMemberFines(@Path("memberId") memberId: Long): List<Fine>
}
