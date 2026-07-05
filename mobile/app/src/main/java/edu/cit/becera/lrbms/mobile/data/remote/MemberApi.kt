package edu.cit.becera.lrbms.mobile.data.remote

import edu.cit.becera.lrbms.mobile.data.model.Member
import retrofit2.http.Body
import retrofit2.http.POST

interface MemberApi {
    @POST("members")
    suspend fun register(@Body member: Member): Member

    @POST("members/login")
    suspend fun login(@Body member: Member): Member
}
