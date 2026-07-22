package edu.cit.becera.lrbms.mobile.ui.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.becera.lrbms.mobile.data.local.SessionManager
import edu.cit.becera.lrbms.mobile.data.model.ChangePasswordRequest
import edu.cit.becera.lrbms.mobile.data.model.Member
import edu.cit.becera.lrbms.mobile.data.remote.RetrofitClient
import edu.cit.becera.lrbms.mobile.data.remote.errorMessage
import edu.cit.becera.lrbms.mobile.ui.common.passwordStrengthError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AccountUiState(
    val isLoading: Boolean = true,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val memberId: String? = null,
    val dateRegistered: String? = null,
    val profileSaving: Boolean = false,
    val profileMessage: String? = null,

    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val passwordSaving: Boolean = false,
    val passwordMessage: String? = null
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
                _uiState.value = _uiState.value.copy(isLoading = false, profileMessage = "Unable to load your account details.")
            }
        }
    }

    fun updateField(update: AccountUiState.() -> AccountUiState) {
        _uiState.value = _uiState.value.update()
    }

    fun saveProfile() {
        val session = SessionManager.current ?: return
        val state = _uiState.value
        _uiState.value = state.copy(profileSaving = true, profileMessage = null)
        viewModelScope.launch {
            try {
                val payload = Member(
                    firstName = state.firstName,
                    lastName = state.lastName,
                    email = state.email,
                    phoneNumber = state.phoneNumber,
                    address = state.address
                )
                val updated = RetrofitClient.api.updateMember(session.id, payload)
                SessionManager.save(
                    getApplication(),
                    session.copy(firstName = updated.firstName ?: session.firstName, lastName = updated.lastName ?: session.lastName, email = updated.email)
                )
                _uiState.value = _uiState.value.copy(profileSaving = false, profileMessage = "Profile updated successfully.")
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(profileSaving = false, profileMessage = e.errorMessage() ?: "Unable to update your profile.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(profileSaving = false, profileMessage = "Unable to update your profile.")
            }
        }
    }

    fun changePassword() {
        val session = SessionManager.current ?: return
        val state = _uiState.value

        var currentError: String? = null
        var newError: String? = null
        var confirmError: String? = null

        if (state.currentPassword.isBlank()) currentError = "Enter your current password."
        if (state.newPassword.isBlank()) {
            newError = "Enter a new password."
        } else {
            val strengthError = passwordStrengthError(state.newPassword)
            newError = when {
                strengthError != null -> strengthError
                state.currentPassword.isNotBlank() && state.newPassword == state.currentPassword ->
                    "New password must be different from your current password."
                else -> null
            }
        }
        if (state.confirmPassword.isBlank()) {
            confirmError = "Confirm your new password."
        } else if (state.newPassword.isNotBlank() && state.newPassword != state.confirmPassword) {
            confirmError = "Passwords do not match."
        }

        if (currentError != null || newError != null || confirmError != null) {
            _uiState.value = state.copy(
                currentPasswordError = currentError,
                newPasswordError = newError,
                confirmPasswordError = confirmError
            )
            return
        }

        _uiState.value = state.copy(passwordSaving = true, passwordMessage = null, currentPasswordError = null, newPasswordError = null, confirmPasswordError = null)
        viewModelScope.launch {
            try {
                RetrofitClient.api.changePassword(
                    session.id,
                    ChangePasswordRequest(state.currentPassword, state.newPassword, state.confirmPassword)
                )
                _uiState.value = _uiState.value.copy(
                    passwordSaving = false,
                    currentPassword = "",
                    newPassword = "",
                    confirmPassword = "",
                    passwordMessage = "Password changed successfully."
                )
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(passwordSaving = false, passwordMessage = e.errorMessage() ?: "Unable to change your password.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(passwordSaving = false, passwordMessage = "Unable to change your password.")
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
