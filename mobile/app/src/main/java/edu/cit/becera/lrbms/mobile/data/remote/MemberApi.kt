package edu.cit.becera.lrbms.mobile.data.remote

import edu.cit.becera.lrbms.mobile.data.model.AuthResponse
import edu.cit.becera.lrbms.mobile.data.model.ChangePasswordRequest
import edu.cit.becera.lrbms.mobile.data.model.Member
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path

interface MemberApi {
    @POST("members")
    suspend fun register(@Body member: Member): Member

    @POST("auth/login")
    suspend fun login(@Body member: Member): AuthResponse

    @GET("members")
    suspend fun getAllMembers(): List<Member>

    @GET("members/role/{role}")
    suspend fun getMembersByRole(@Path("role") role: String): List<Member>

    @GET("members/{id}")
    suspend fun getMember(@Path("id") id: Long): Member

    @PUT("members/{id}")
    suspend fun updateMember(@Path("id") id: Long, @Body member: Member): Member

    @PUT("members/{id}/password")
    suspend fun changePassword(@Path("id") id: Long, @Body request: ChangePasswordRequest): Member

    @DELETE("members/{id}")
    suspend fun deleteMember(@Path("id") id: Long)
}
