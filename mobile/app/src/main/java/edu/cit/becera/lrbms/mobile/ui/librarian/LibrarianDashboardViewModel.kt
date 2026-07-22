package edu.cit.becera.lrbms.mobile.ui.librarian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.becera.lrbms.mobile.data.local.SessionManager
import edu.cit.becera.lrbms.mobile.data.model.Reservation
import edu.cit.becera.lrbms.mobile.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class LibrarianDashboardUiState(
    val isLoading: Boolean = true,
    val firstName: String = "",
    val catalogTitles: Int = 0,
    val activeLoans: Int = 0,
    val overdue: Int = 0,
    val pendingReservations: List<Reservation> = emptyList(),
    val unpaidFineMembers: Int = 0,
    val errorMessage: String? = null
)

class LibrarianDashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LibrarianDashboardUiState())
    val uiState: StateFlow<LibrarianDashboardUiState> = _uiState

    fun load() {
        val session = SessionManager.current
        _uiState.value = _uiState.value.copy(isLoading = true, firstName = session?.firstName ?: "")
        viewModelScope.launch {
            try {
                val books = runCatching { RetrofitClient.bookApi.getBooks() }.getOrDefault(emptyList())
                val transactions = runCatching { RetrofitClient.transactionApi.getAllTransactions() }.getOrDefault(emptyList())
                val reservations = runCatching { RetrofitClient.reservationApi.getAllReservations() }.getOrDefault(emptyList())
                val fines = runCatching { RetrofitClient.fineApi.getAllFines() }.getOrDefault(emptyList())

                val activeLoans = transactions.filter { it.status != "RETURNED" }
                val overdue = activeLoans.count { (it.dueDate ?: "") < LocalDate.now().toString() }
                val pending = reservations.filter { it.status == "PENDING" }
                val unpaidMembers = fines.filter { it.paymentStatus != "PAID" }.map { it.memberId }.distinct().size

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    catalogTitles = books.size,
                    activeLoans = activeLoans.size,
                    overdue = overdue,
                    pendingReservations = pending.take(6),
                    unpaidFineMembers = unpaidMembers
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Some dashboard data could not be loaded right now.")
            }
        }
    }
}
