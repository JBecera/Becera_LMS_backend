package edu.cit.becera.lrbms.mobile.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.becera.lrbms.mobile.data.model.ChangePasswordRequest
import edu.cit.becera.lrbms.mobile.data.model.Member
import edu.cit.becera.lrbms.mobile.data.remote.RetrofitClient
import edu.cit.becera.lrbms.mobile.data.remote.errorMessage
import edu.cit.becera.lrbms.mobile.ui.common.emailFormatError
import edu.cit.becera.lrbms.mobile.ui.common.passwordStrengthError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AccountFormState(
    val id: Long? = null,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = "LIBRARIAN"
)

data class ManageAccountsUiState(
    val isLoading: Boolean = true,
    val members: List<Member> = emptyList(),
    val librarians: List<Member> = emptyList(),
    val form: AccountFormState = AccountFormState(),
    val message: String? = null
)

class ManageAccountsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ManageAccountsUiState())
    val uiState: StateFlow<ManageAccountsUiState> = _uiState

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val allMembers = RetrofitClient.api.getAllMembers().filter { it.role?.uppercase() != "ADMIN" }
                val librarians = RetrofitClient.api.getMembersByRole("LIBRARIAN")
                _uiState.value = _uiState.value.copy(isLoading = false, members = allMembers, librarians = librarians)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, message = "Unable to load management data.")
            }
        }
    }

    fun updateForm(update: AccountFormState.() -> AccountFormState) {
        _uiState.value = _uiState.value.copy(form = _uiState.value.form.update())
    }

    fun edit(member: Member) {
        _uiState.value = _uiState.value.copy(
            form = AccountFormState(
                id = member.id,
                firstName = member.firstName ?: "",
                lastName = member.lastName ?: "",
                email = member.email,
                password = "",
                role = member.role ?: "LIBRARIAN"
            )
        )
    }

    fun cancelEdit() {
        _uiState.value = _uiState.value.copy(form = AccountFormState())
    }

    fun submit() {
        val form = _uiState.value.form

        emailFormatError(form.email)?.let {
            _uiState.value = _uiState.value.copy(message = it)
            return
        }
        if (form.id == null || form.password.isNotBlank()) {
            passwordStrengthError(form.password)?.let {
                _uiState.value = _uiState.value.copy(message = it)
                return
            }
        }

        viewModelScope.launch {
            try {
                if (form.id != null) {
                    RetrofitClient.api.updateMember(
                        form.id,
                        Member(firstName = form.firstName, lastName = form.lastName, email = form.email, role = form.role)
                    )
                    if (form.password.isNotBlank()) {
                        RetrofitClient.api.changePassword(form.id, ChangePasswordRequest("", form.password, form.password))
                    }
                    _uiState.value = _uiState.value.copy(message = "Account updated successfully.")
                } else {
                    RetrofitClient.api.register(
                        Member(firstName = form.firstName, lastName = form.lastName, email = form.email, password = form.password, role = "LIBRARIAN")
                    )
                    _uiState.value = _uiState.value.copy(message = "Librarian account created successfully.")
                }
                _uiState.value = _uiState.value.copy(form = AccountFormState())
                load()
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(message = e.errorMessage() ?: "Unable to save account.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Unable to save account.")
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.deleteMember(id)
                _uiState.value = _uiState.value.copy(message = "Account removed successfully.")
                load()
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(message = e.errorMessage() ?: "Unable to remove account.")
            }
        }
    }
}
