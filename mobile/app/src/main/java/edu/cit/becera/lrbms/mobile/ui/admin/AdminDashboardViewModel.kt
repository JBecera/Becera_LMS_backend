package edu.cit.becera.lrbms.mobile.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.becera.lrbms.mobile.data.local.SessionManager
import edu.cit.becera.lrbms.mobile.data.model.Member
import edu.cit.becera.lrbms.mobile.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val firstName: String = "",
    val memberCount: Int = 0,
    val librarianCount: Int = 0,
    val totalAccounts: Int = 0,
    val recentAccounts: List<Member> = emptyList(),
    val errorMessage: String? = null
)

class AdminDashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState

    fun load() {
        val session = SessionManager.current
        _uiState.value = _uiState.value.copy(isLoading = true, firstName = session?.firstName ?: "")
        viewModelScope.launch {
            try {
                val allMembers = RetrofitClient.api.getAllMembers().filter { it.role?.uppercase() != "ADMIN" }
                val librarians = RetrofitClient.api.getMembersByRole("LIBRARIAN")
                val memberCount = allMembers.count { it.role?.uppercase() == "MEMBER" }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    memberCount = memberCount,
                    librarianCount = librarians.size,
                    totalAccounts = allMembers.size + 1,
                    recentAccounts = allMembers.takeLast(5).reversed()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Unable to load management data.")
            }
        }
    }
}
