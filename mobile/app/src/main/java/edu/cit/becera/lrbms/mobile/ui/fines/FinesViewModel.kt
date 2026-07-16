package edu.cit.becera.lrbms.mobile.ui.fines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.becera.lrbms.mobile.data.local.SessionManager
import edu.cit.becera.lrbms.mobile.data.model.Fine
import edu.cit.becera.lrbms.mobile.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FinesUiState(
    val isLoading: Boolean = true,
    val fines: List<Fine> = emptyList(),
    val errorMessage: String? = null
)

class FinesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(FinesUiState())
    val uiState: StateFlow<FinesUiState> = _uiState

    fun load() {
        val session = SessionManager.current ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val fines = RetrofitClient.fineApi.getMemberFines(session.id)
                _uiState.value = _uiState.value.copy(isLoading = false, fines = fines)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Unable to load fines right now.")
            }
        }
    }
}
