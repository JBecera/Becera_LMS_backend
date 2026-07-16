package edu.cit.becera.lrbms.mobile.ui.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.becera.lrbms.mobile.data.local.SessionManager
import edu.cit.becera.lrbms.mobile.data.model.Reservation
import edu.cit.becera.lrbms.mobile.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ReservationsUiState(
    val isLoading: Boolean = true,
    val reservations: List<Reservation> = emptyList(),
    val message: String? = null
)

class ReservationsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ReservationsUiState())
    val uiState: StateFlow<ReservationsUiState> = _uiState

    fun load() {
        val session = SessionManager.current ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val reservations = RetrofitClient.reservationApi.getMemberReservations(session.id)
                _uiState.value = _uiState.value.copy(isLoading = false, reservations = reservations)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, message = "Unable to load reservations right now.")
            }
        }
    }

    fun cancel(id: Long) {
        viewModelScope.launch {
            try {
                RetrofitClient.reservationApi.cancelReservation(id)
                _uiState.value = _uiState.value.copy(message = "Reservation cancelled.")
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Unable to cancel this reservation.")
            }
        }
    }
}
