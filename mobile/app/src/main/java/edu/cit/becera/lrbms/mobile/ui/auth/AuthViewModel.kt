package edu.cit.becera.lrbms.mobile.ui.auth

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

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun register(firstName: String, lastName: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)
            try {
                val member = Member(firstName = firstName, lastName = lastName, email = email, password = password)
                RetrofitClient.api.register(member)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true, successMessage = "Registration successful!")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Registration failed")
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
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Login failed")
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
