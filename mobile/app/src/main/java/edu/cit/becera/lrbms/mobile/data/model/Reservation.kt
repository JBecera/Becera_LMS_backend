package edu.cit.becera.lrbms.mobile.data.model

import com.google.gson.annotations.SerializedName

data class Reservation(
    @SerializedName("id") val id: Long,
    @SerializedName("memberId") val memberId: Long,
    @SerializedName("memberName") val memberName: String? = null,
    @SerializedName("resourceId") val resourceId: Long,
    @SerializedName("resourceTitle") val resourceTitle: String? = null,
    @SerializedName("reservationDate") val reservationDate: String? = null,
    @SerializedName("status") val status: String
)

data class CreateReservationRequest(
    @SerializedName("resourceId") val resourceId: Long
)
