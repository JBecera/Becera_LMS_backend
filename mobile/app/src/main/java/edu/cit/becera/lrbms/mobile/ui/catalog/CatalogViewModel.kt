package edu.cit.becera.lrbms.mobile.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.becera.lrbms.mobile.data.model.Book
import edu.cit.becera.lrbms.mobile.data.model.CreateReservationRequest
import edu.cit.becera.lrbms.mobile.data.remote.RetrofitClient
import edu.cit.becera.lrbms.mobile.data.remote.errorMessage
import edu.cit.becera.lrbms.mobile.ui.common.todayIso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class CatalogUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val pickupDate: String = "",
    val books: List<Book> = emptyList(),
    val message: String? = null
) {
    // Web always applies this client-side title/author filter on top of whatever the server
    // returned, so a dated view can still be narrowed by typing without a fresh network call.
    val visibleBooks: List<Book>
        get() {
            val q = query.trim().lowercase()
            if (q.isBlank()) return books
            return books.filter { "${it.title} ${it.author}".lowercase().contains(q) }
        }
}

class CatalogViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState

    fun load() {
        search(_uiState.value.query)
    }

    fun search(query: String) {
        val hasPickupDate = _uiState.value.pickupDate.isNotBlank()
        _uiState.value = _uiState.value.copy(query = query, isLoading = if (hasPickupDate) _uiState.value.isLoading else true)
        // With a pickup date active, the fetched list is already narrowed to that date; typing
        // only needs to re-filter it locally (see visibleBooks) rather than refetch.
        if (!hasPickupDate) fetch()
    }

    fun setPickupDate(date: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, pickupDate = date)
        fetch()
    }

    fun clearPickupDate() {
        _uiState.value = _uiState.value.copy(isLoading = true, pickupDate = "")
        fetch()
    }

    private fun fetch() {
        val query = _uiState.value.query
        val pickupDate = _uiState.value.pickupDate
        viewModelScope.launch {
            try {
                val books = when {
                    pickupDate.isNotBlank() -> RetrofitClient.bookApi.getBooksAvailableOn(pickupDate)
                    query.isBlank() -> RetrofitClient.bookApi.getBooks()
                    else -> RetrofitClient.bookApi.searchBooks(query)
                }
                _uiState.value = _uiState.value.copy(isLoading = false, books = books)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, message = "Unable to load the catalog right now.")
            }
        }
    }

    fun availabilityFor(book: Book): Int =
        if (_uiState.value.pickupDate.isNotBlank()) book.availableOnDate ?: 0 else book.availableCopies

    fun reserve(bookId: Long) {
        val pickupDate = _uiState.value.pickupDate.ifBlank { todayIso() }
        viewModelScope.launch {
            try {
                RetrofitClient.reservationApi.createReservation(CreateReservationRequest(bookId, pickupDate))
                _uiState.value = _uiState.value.copy(message = "Booked for pickup on $pickupDate. A librarian will approve it soon.")
                fetch()
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(message = e.errorMessage() ?: "Unable to book this title right now.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Unable to book this title right now.")
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
