package edu.cit.becera.lrbms.mobile.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.becera.lrbms.mobile.data.model.Book
import edu.cit.becera.lrbms.mobile.data.model.CreateReservationRequest
import edu.cit.becera.lrbms.mobile.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import org.json.JSONObject

data class CatalogUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val books: List<Book> = emptyList(),
    val message: String? = null
)

class CatalogViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState

    fun load() {
        search(_uiState.value.query)
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, query = query)
        viewModelScope.launch {
            try {
                val books = if (query.isBlank()) {
                    RetrofitClient.bookApi.getBooks()
                } else {
                    RetrofitClient.bookApi.searchBooks(query)
                }
                _uiState.value = _uiState.value.copy(isLoading = false, books = books)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, message = "Unable to load the catalog right now.")
            }
        }
    }

    fun reserve(bookId: Long) {
        viewModelScope.launch {
            try {
                RetrofitClient.reservationApi.createReservation(CreateReservationRequest(bookId))
                _uiState.value = _uiState.value.copy(message = "You're on the waitlist. A librarian will approve it once a copy is free.")
                search(_uiState.value.query)
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(message = extractError(e) ?: "Unable to reserve this title right now.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Unable to reserve this title right now.")
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    private fun extractError(e: HttpException): String? = try {
        val body = e.response()?.errorBody()?.string()
        body?.let { JSONObject(it).optString("error").ifBlank { null } }
    } catch (ex: Exception) {
        null
    }
}
