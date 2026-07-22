package edu.cit.becera.lrbms.mobile.ui.fines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.becera.lrbms.mobile.data.local.SessionManager
import edu.cit.becera.lrbms.mobile.data.model.Fine
import edu.cit.becera.lrbms.mobile.data.model.UpdateFineRequest
import edu.cit.becera.lrbms.mobile.data.remote.RetrofitClient
import edu.cit.becera.lrbms.mobile.data.remote.errorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class FinesUiState(
    val isLoading: Boolean = true,
    val isStaff: Boolean = false,
    val fines: List<Fine> = emptyList(),
    val errorMessage: String? = null
)

class FinesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FinesUiState())
    val uiState: StateFlow<FinesUiState> = _uiState

    fun load() {
        val session = SessionManager.current ?: return
        val isStaff = session.role == "LIBRARIAN" || session.role == "ADMIN"
        _uiState.value = _uiState.value.copy(isLoading = true, isStaff = isStaff)
        viewModelScope.launch {
            try {
                val fines = if (isStaff) RetrofitClient.fineApi.getAllFines() else RetrofitClient.fineApi.getMemberFines(session.id)
                _uiState.value = _uiState.value.copy(isLoading = false, fines = fines)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Unable to load fines right now.")
            }
        }
    }

    fun settle(id: Long) {
        viewModelScope.launch {
            try {
                RetrofitClient.fineApi.settleFine(id, UpdateFineRequest("PAID"))
                _uiState.value = _uiState.value.copy(errorMessage = "Fine marked as settled.")
                load()
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(errorMessage = e.errorMessage() ?: "Unable to update this fine.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Unable to update this fine.")
            }
        }
    }
}
