package edu.cit.becera.lrbms.mobile.data.remote

import edu.cit.becera.lrbms.mobile.data.model.CreateReservationRequest
import edu.cit.becera.lrbms.mobile.data.model.Reservation
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReservationApi {
    @GET("reservations/member/{memberId}")
    suspend fun getMemberReservations(@Path("memberId") memberId: Long): List<Reservation>

    @POST("reservations")
    suspend fun createReservation(@Body request: CreateReservationRequest): Reservation

    @DELETE("reservations/{id}")
    suspend fun cancelReservation(@Path("id") id: Long)
}
