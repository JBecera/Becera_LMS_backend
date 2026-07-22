package edu.cit.becera.lrbms.mobile.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.becera.lrbms.mobile.data.local.SessionManager
import edu.cit.becera.lrbms.mobile.data.local.UserSession
import edu.cit.becera.lrbms.mobile.data.model.Member
import edu.cit.becera.lrbms.mobile.data.remote.RetrofitClient
import edu.cit.becera.lrbms.mobile.data.remote.errorMessage
import edu.cit.becera.lrbms.mobile.ui.common.emailFormatError
import edu.cit.becera.lrbms.mobile.ui.common.passwordStrengthError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun register(firstName: String, lastName: String, email: String, password: String) {
        emailFormatError(email)?.let {
            _uiState.value = _uiState.value.copy(errorMessage = it)
            return
        }
        passwordStrengthError(password)?.let {
            _uiState.value = _uiState.value.copy(errorMessage = it)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            try {
                val member = Member(firstName = firstName, lastName = lastName, email = email, password = password)
                RetrofitClient.api.register(member)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Registration successful!")
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.errorMessage() ?: "Registration failed")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Registration failed")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            try {
                val member = Member(email = email, password = password)
                val response = RetrofitClient.api.login(member)
                SessionManager.save(
                    getApplication(),
                    UserSession(
                        id = response.id,
                        firstName = response.firstName,
                        lastName = response.lastName,
                        email = response.email,
                        role = response.role,
                        token = response.token
                    )
                )
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Login successful!")
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.errorMessage() ?: "Invalid email or password.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Login failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
