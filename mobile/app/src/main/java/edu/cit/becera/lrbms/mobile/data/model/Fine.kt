package edu.cit.becera.lrbms.mobile.data.model

import com.google.gson.annotations.SerializedName

data class Fine(
    @SerializedName("id") val id: Long,
    @SerializedName("memberId") val memberId: Long,
    @SerializedName("memberName") val memberName: String? = null,
    @SerializedName("amount") val amount: Double,
    @SerializedName("reason") val reason: String? = null,
    @SerializedName("paymentStatus") val paymentStatus: String,
    @SerializedName("dateIssued") val dateIssued: String? = null
)

data class UpdateFineRequest(
    @SerializedName("paymentStatus") val paymentStatus: String
)
