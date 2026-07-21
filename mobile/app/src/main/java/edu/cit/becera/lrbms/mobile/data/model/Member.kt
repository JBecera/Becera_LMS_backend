package edu.cit.becera.lrbms.mobile.data.model

import com.google.gson.annotations.SerializedName

data class Member(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("memberId") val memberId: String? = null,
    @SerializedName("phoneNumber") val phoneNumber: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("dateRegistered") val dateRegistered: String? = null
)
