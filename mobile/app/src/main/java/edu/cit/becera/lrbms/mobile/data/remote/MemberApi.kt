package edu.cit.becera.lrbms.mobile.data.remote

import edu.cit.becera.lrbms.mobile.data.model.AuthResponse
import edu.cit.becera.lrbms.mobile.data.model.Member
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path

interface MemberApi {
    @POST("members")
    suspend fun register(@Body member: Member): Member

    @POST("auth/login")
    suspend fun login(@Body member: Member): AuthResponse

    @GET("members/{id}")
    suspend fun getMember(@Path("id") id: Long): Member

    @PUT("members/{id}")
    suspend fun updateMember(@Path("id") id: Long, @Body member: Member): Member
}
