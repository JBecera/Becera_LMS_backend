package edu.cit.becera.lrbms.mobile.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("token") val token: String
)
