package edu.cit.becera.lrbms.mobile.ui.librarian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.becera.lrbms.mobile.data.model.Book
import edu.cit.becera.lrbms.mobile.data.model.BookRequest
import edu.cit.becera.lrbms.mobile.data.remote.RetrofitClient
import edu.cit.becera.lrbms.mobile.data.remote.errorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class BookFormState(
    val id: Long? = null,
    val title: String = "",
    val author: String = "",
    val isbn: String = "",
    val category: String = "",
    val description: String = "",
    val coverImage: String = "",
    val totalCopies: String = "1",
    val availableCopies: String = "1"
)

data class ManageCatalogUiState(
    val isLoading: Boolean = true,
    val books: List<Book> = emptyList(),
    val form: BookFormState = BookFormState(),
    val message: String? = null
)

class ManageCatalogViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ManageCatalogUiState())
    val uiState: StateFlow<ManageCatalogUiState> = _uiState

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val books = RetrofitClient.bookApi.getBooks()
                _uiState.value = _uiState.value.copy(isLoading = false, books = books)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, message = "Unable to load the book catalog.")
            }
        }
    }

    fun updateForm(update: BookFormState.() -> BookFormState) {
        _uiState.value = _uiState.value.copy(form = _uiState.value.form.update())
    }

    fun editBook(book: Book) {
        _uiState.value = _uiState.value.copy(
            form = BookFormState(
                id = book.id,
                title = book.title,
                author = book.author,
                isbn = book.isbn ?: "",
                category = book.category ?: "",
                description = book.description ?: "",
                coverImage = book.coverImage ?: "",
                totalCopies = (book.totalCopies ?: book.availableCopies).toString(),
                availableCopies = book.availableCopies.toString()
            )
        )
    }

    fun cancelEdit() {
        _uiState.value = _uiState.value.copy(form = BookFormState())
    }

    fun submit() {
        val form = _uiState.value.form
        val request = BookRequest(
            title = form.title,
            author = form.author,
            isbn = form.isbn,
            category = form.category,
            description = form.description.ifBlank { null },
            coverImage = form.coverImage.ifBlank { null },
            totalCopies = form.totalCopies.toIntOrNull(),
            availableCopies = if (form.id != null) form.availableCopies.toIntOrNull() else null
        )
        viewModelScope.launch {
            try {
                if (form.id != null) {
                    RetrofitClient.bookApi.updateBook(form.id, request)
                    _uiState.value = _uiState.value.copy(message = "Book updated successfully.")
                } else {
                    RetrofitClient.bookApi.createBook(request)
                    _uiState.value = _uiState.value.copy(message = "Book added to the catalog.")
                }
                _uiState.value = _uiState.value.copy(form = BookFormState())
                load()
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(message = e.errorMessage() ?: "Unable to save book.")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Unable to save book.")
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            try {
                RetrofitClient.bookApi.deleteBook(id)
                _uiState.value = _uiState.value.copy(message = "Book removed from the catalog.")
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Unable to remove this book.")
            }
        }
    }
}
