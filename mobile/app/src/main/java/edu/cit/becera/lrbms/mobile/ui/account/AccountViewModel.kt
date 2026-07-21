package edu.cit.becera.lrbms.mobile.ui.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.becera.lrbms.mobile.data.local.SessionManager
import edu.cit.becera.lrbms.mobile.data.local.UserSession
import edu.cit.becera.lrbms.mobile.data.model.Member
import edu.cit.becera.lrbms.mobile.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AccountUiState(
    val isLoading: Boolean = true,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val password: String = "",
    val memberId: String? = null,
    val dateRegistered: String? = null,
    val message: String? = null
)

class AccountViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState

    fun load() {
        val session = SessionManager.current ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val account = RetrofitClient.api.getMember(session.id)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    firstName = account.firstName ?: "",
                    lastName = account.lastName ?: "",
                    email = account.email,
                    phoneNumber = account.phoneNumber ?: "",
                    address = account.address ?: "",
                    memberId = account.memberId,
                    dateRegistered = account.dateRegistered
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, message = "Unable to load your account details.")
            }
        }
    }

    fun updateField(update: AccountUiState.() -> AccountUiState) {
        _uiState.value = _uiState.value.update()
    }

    fun save() {
        val session = SessionManager.current ?: return
        val state = _uiState.value
        viewModelScope.launch {
            try {
                val payload = Member(
                    firstName = state.firstName,
                    lastName = state.lastName,
                    email = state.email,
                    password = state.password.ifBlank { null },
                    phoneNumber = state.phoneNumber,
                    address = state.address
                )
                val updated = RetrofitClient.api.updateMember(session.id, payload)
                SessionManager.save(
                    getApplication(),
                    session.copy(firstName = updated.firstName ?: session.firstName, lastName = updated.lastName ?: session.lastName, email = updated.email)
                )
                _uiState.value = _uiState.value.copy(password = "", message = "Account updated successfully.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Unable to update your account.")
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            SessionManager.clear(getApplication())
            onSignedOut()
        }
    }
}
