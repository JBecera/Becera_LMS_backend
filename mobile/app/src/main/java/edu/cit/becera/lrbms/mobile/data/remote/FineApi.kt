package edu.cit.becera.lrbms.mobile.data.remote

import edu.cit.becera.lrbms.mobile.data.model.Fine
import edu.cit.becera.lrbms.mobile.data.model.UpdateFineRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface FineApi {
    @GET("fines")
    suspend fun getAllFines(): List<Fine>

    @GET("fines/member/{memberId}")
    suspend fun getMemberFines(@Path("memberId") memberId: Long): List<Fine>

    @PUT("fines/{id}")
    suspend fun settleFine(@Path("id") id: Long, @Body request: UpdateFineRequest): Fine
}
