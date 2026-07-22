package edu.cit.becera.lrbms.mobile.ui.librarian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.becera.lrbms.mobile.data.model.CheckoutRequest
import edu.cit.becera.lrbms.mobile.data.model.Fine
import edu.cit.becera.lrbms.mobile.data.model.Reservation
import edu.cit.becera.lrbms.mobile.data.model.Transaction
import edu.cit.becera.lrbms.mobile.data.model.UpdateReservationRequest
import edu.cit.becera.lrbms.mobile.data.remote.RetrofitClient
import edu.cit.becera.lrbms.mobile.data.remote.errorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDate

data class ManageBookingsUiState(
    val isLoading: Boolean = true,
    val reservations: List<Reservation> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val fines: List<Fine> = emptyList(),
    val rejectDrafts: Map<Long, String> = emptyMap(),
    val checkoutDrafts: Map<Long, String> = emptyMap(),
    val message: String? = null
) {
    val pending get() = reservations.filter { it.status == "PENDING" }
    val approved get() = reservations.filter { it.status == "APPROVED" }
    val activeLoans get() = transactions.filter { it.status == "ACTIVE" }
    val history get() = reservations.filter { it.status == "REJECTED" || it.status == "COMPLETED" || it.status == "EXPIRED" }

    fun activeLoanCount(memberId: Long) = transactions.count { it.memberId == memberId && it.status == "ACTIVE" }
    fun unpaidFineTotal(memberId: Long) = fines.filter { it.memberId == memberId && it.paymentStatus != "PAID" }.sumOf { it.amount }
}

class ManageBookingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ManageBookingsUiState())
    val uiState: StateFlow<ManageBookingsUiState> = _uiState

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val reservations = runCatching { RetrofitClient.reservationApi.getAllReservations() }.getOrDefault(emptyList())
                val transactions = runCatching { RetrofitClient.transactionApi.getAllTransactions() }.getOrDefault(emptyList())
                val fines = runCatching { RetrofitClient.fineApi.getAllFines() }.getOrDefault(emptyList())
                _uiState.value = _uiState.value.copy(isLoading = false, reservations = reservations, transactions = transactions, fines = fines)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, message = "Unable to load booking data right now.")
            }
        }
    }

    fun approve(id: Long) {
        viewModelScope.launch {
            try {
                RetrofitClient.reservationApi.updateReservationStatus(id, UpdateReservationRequest("APPROVED"))
                _uiState.value = _uiState.value.copy(message = "Booking approved.")
                load()
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(message = e.errorMessage() ?: "Unable to approve this booking.")
            }
        }
    }

    fun setRejectDraft(id: Long, reason: String?) {
        val drafts = _uiState.value.rejectDrafts.toMutableMap()
        if (reason == null) drafts.remove(id) else drafts[id] = reason
        _uiState.value = _uiState.value.copy(rejectDrafts = drafts)
    }

    fun confirmReject(id: Long) {
        val reason = _uiState.value.rejectDrafts[id]?.trim().orEmpty()
        if (reason.isBlank()) {
            _uiState.value = _uiState.value.copy(message = "A rejection reason is required.")
            return
        }
        viewModelScope.launch {
            try {
                RetrofitClient.reservationApi.updateReservationStatus(id, UpdateReservationRequest("REJECTED", reason))
                setRejectDraft(id, null)
                _uiState.value = _uiState.value.copy(message = "Booking rejected.")
                load()
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(message = e.errorMessage() ?: "Unable to reject this booking.")
            }
        }
    }

    fun setCheckoutDraft(reservationId: Long, dueDate: String) {
        val drafts = _uiState.value.checkoutDrafts.toMutableMap()
        drafts[reservationId] = dueDate
        _uiState.value = _uiState.value.copy(checkoutDrafts = drafts)
    }

    fun defaultDueDate(): String = LocalDate.now().plusDays(7).toString()

    fun markPickedUp(reservation: Reservation) {
        val dueDate = _uiState.value.checkoutDrafts[reservation.id] ?: defaultDueDate()
        viewModelScope.launch {
            try {
                RetrofitClient.transactionApi.checkout(CheckoutRequest(reservation.memberId, reservation.resourceId, dueDate))
                _uiState.value = _uiState.value.copy(message = "Marked as picked up.")
                load()
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(message = e.errorMessage() ?: "Unable to check this member out.")
            }
        }
    }

    fun cancel(id: Long) {
        viewModelScope.launch {
            try {
                RetrofitClient.reservationApi.cancelReservation(id)
                _uiState.value = _uiState.value.copy(message = "Booking cancelled.")
                load()
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(message = e.errorMessage() ?: "Unable to cancel this booking.")
            }
        }
    }

    fun markReturned(transactionId: Long) {
        viewModelScope.launch {
            try {
                RetrofitClient.transactionApi.checkIn(transactionId)
                _uiState.value = _uiState.value.copy(message = "Marked as returned.")
                load()
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(message = e.errorMessage() ?: "Unable to record this return.")
            }
        }
    }
}
