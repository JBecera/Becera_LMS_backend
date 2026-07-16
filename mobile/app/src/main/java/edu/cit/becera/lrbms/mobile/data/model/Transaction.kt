package edu.cit.becera.lrbms.mobile.data.model

import com.google.gson.annotations.SerializedName

data class Transaction(
    @SerializedName("id") val id: Long,
    @SerializedName("memberId") val memberId: Long,
    @SerializedName("memberName") val memberName: String? = null,
    @SerializedName("resourceId") val resourceId: Long,
    @SerializedName("resourceTitle") val resourceTitle: String? = null,
    @SerializedName("resourceCategory") val resourceCategory: String? = null,
    @SerializedName("checkOutDate") val checkOutDate: String? = null,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("checkInDate") val checkInDate: String? = null,
    @SerializedName("status") val status: String
)
