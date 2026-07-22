package edu.cit.becera.lrbms.mobile.ui.librarian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.becera.lrbms.mobile.data.model.Member
import edu.cit.becera.lrbms.mobile.data.remote.RetrofitClient
import edu.cit.becera.lrbms.mobile.data.remote.errorMessage
import edu.cit.becera.lrbms.mobile.ui.common.passwordStrengthError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class RegisterFormState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = ""
)

data class MembersUiState(
    val isLoading: Boolean = true,
    val members: List<Member> = emptyList(),
    val search: String = "",
    val form: RegisterFormState = RegisterFormState(),
    val message: String? = null
) {
    val filtered: List<Member>
        get() {
            val q = search.trim().lowercase()
            if (q.isBlank()) return members
            return members.filter { "${it.firstName} ${it.lastName} ${it.email}".lowercase().contains(q) }
        }
}

class MembersViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MembersUiState())
    val uiState: StateFlow<MembersUiState> = _uiState

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val members = RetrofitClient.api.getAllMembers().filter { it.role?.uppercase() == "MEMBER" }
                _uiState.value = _uiState.value.copy(isLoading = false, members = members)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, message = "Unable to load member accounts.")
            }
        }
    }

    fun setSearch(query: String) {
        _uiState.value = _uiState.value.copy(search = query)
    }

    fun updateForm(update: RegisterFormState.() -> RegisterFormState) {
        _uiState.value = _uiState.value.copy(form = _uiState.value.form.update())
    }

    fun register() {
        val form = _uiState.value.form
        val passwordError = passwordStrengthError(form.password)
        if (passwordError != null) {
            _uiState.value = _uiState.value.copy(message = passwordError)
            return
        }
        viewModelScope.launch {
            try {
                RetrofitClient.api.register(Member(firstName = form.firstName, lastName = form.lastName, email = form.email, password = form.password, role = "MEMBER"))
                _uiState.value = _uiState.value.copy(message = "Member account created for ${form.firstName} ${form.lastName}.", form = RegisterFormState())
                load()
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(message = e.errorMessage() ?: "Unable to register this member.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Unable to register this member.")
            }
        }
    }
}
