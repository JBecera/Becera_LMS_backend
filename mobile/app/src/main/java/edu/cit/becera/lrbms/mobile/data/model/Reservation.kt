package edu.cit.becera.lrbms.mobile.data.model

import com.google.gson.annotations.SerializedName

data class Reservation(
    @SerializedName("id") val id: Long,
    @SerializedName("memberId") val memberId: Long,
    @SerializedName("memberName") val memberName: String? = null,
    @SerializedName("resourceId") val resourceId: Long,
    @SerializedName("resourceTitle") val resourceTitle: String? = null,
    @SerializedName("reservationDate") val reservationDate: String? = null,
    @SerializedName("pickupDate") val pickupDate: String? = null,
    @SerializedName("status") val status: String,
    @SerializedName("reason") val reason: String? = null
)

data class CreateReservationRequest(
    @SerializedName("resourceId") val resourceId: Long,
    @SerializedName("pickupDate") val pickupDate: String? = null
)

data class UpdateReservationRequest(
    @SerializedName("status") val status: String,
    @SerializedName("reason") val reason: String? = null
)
