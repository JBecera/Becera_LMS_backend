package edu.cit.becera.lrbms.mobile.ui.borrowing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.becera.lrbms.mobile.data.local.SessionManager
import edu.cit.becera.lrbms.mobile.data.model.Transaction
import edu.cit.becera.lrbms.mobile.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MyBorrowingUiState(
    val isLoading: Boolean = true,
    val activeLoans: List<Transaction> = emptyList(),
    val history: List<Transaction> = emptyList(),
    val errorMessage: String? = null
)

class MyBorrowingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MyBorrowingUiState())
    val uiState: StateFlow<MyBorrowingUiState> = _uiState

    fun load() {
        val session = SessionManager.current ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val transactions = RetrofitClient.transactionApi.getMemberTransactions(session.id)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    activeLoans = transactions.filter { it.status != "RETURNED" },
                    history = transactions.filter { it.status == "RETURNED" }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Unable to load your borrowed items right now.")
            }
        }
    }
}
