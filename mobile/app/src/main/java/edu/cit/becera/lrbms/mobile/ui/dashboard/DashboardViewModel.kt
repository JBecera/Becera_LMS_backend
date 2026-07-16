package edu.cit.becera.lrbms.mobile.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.becera.lrbms.mobile.data.local.SessionManager
import edu.cit.becera.lrbms.mobile.data.model.Fine
import edu.cit.becera.lrbms.mobile.data.model.Reservation
import edu.cit.becera.lrbms.mobile.data.model.Transaction
import edu.cit.becera.lrbms.mobile.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    val firstName: String = "",
    val activeLoans: List<Transaction> = emptyList(),
    val overdueCount: Int = 0,
    val pendingReservations: Int = 0,
    val totalOwed: Double = 0.0,
    val errorMessage: String? = null
)

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    fun load() {
        val session = SessionManager.current ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, firstName = session.firstName, errorMessage = null)
        viewModelScope.launch {
            try {
                val transactions = RetrofitClient.transactionApi.getMemberTransactions(session.id)
                val reservations = RetrofitClient.reservationApi.getMemberReservations(session.id)
                val fines = RetrofitClient.fineApi.getMemberFines(session.id)

                val active = transactions.filter { it.status != "RETURNED" }
                val overdue = active.count { isOverdue(it) }
                val pending = reservations.count { it.status == "PENDING" }
                val owed = fines.filter { it.paymentStatus != "PAID" }.sumOf { it.amount }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    activeLoans = active,
                    overdueCount = overdue,
                    pendingReservations = pending,
                    totalOwed = owed
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Unable to load dashboard")
            }
        }
    }
}

private fun isOverdue(transaction: Transaction): Boolean {
    val due = transaction.dueDate ?: return false
    return due < java.time.LocalDate.now().toString()
}

internal fun daysRemaining(dueDate: String?): Int? {
    if (dueDate == null) return null
    return try {
        java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), java.time.LocalDate.parse(dueDate)).toInt()
    } catch (e: Exception) {
        null
    }
}

internal fun Fine.isUnpaid() = paymentStatus != "PAID"

internal fun Reservation.isPending() = status == "PENDING"
