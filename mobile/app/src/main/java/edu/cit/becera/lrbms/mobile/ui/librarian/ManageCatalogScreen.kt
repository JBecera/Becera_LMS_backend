package edu.cit.becera.lrbms.mobile.ui.librarian

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.cit.becera.lrbms.mobile.data.model.Book
import edu.cit.becera.lrbms.mobile.ui.dashboard.InfoCard

@Composable
fun ManageCatalogScreen(viewModel: ManageCatalogViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val form = state.form

    LaunchedEffect(Unit) { viewModel.load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFEFF2FF))))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Manage the catalog", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
            if (form.id != null) TextButton(onClick = { viewModel.cancelEdit() }) { Text("Cancel edit") }
        }
        Text("Add new titles, update details, and keep availability accurate.", fontSize = 13.sp, color = Color(0xFF64748B))

        state.message?.let { Text(it, fontSize = 13.sp, color = Color(0xFF4F46E5)) }

        Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (form.id != null) "Edit book" else "Add a new book", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))

                Field("Title", form.title) { viewModel.updateForm { copy(title = it) } }
                Field("Author", form.author) { viewModel.updateForm { copy(author = it) } }
                Field("ISBN", form.isbn) { viewModel.updateForm { copy(isbn = it) } }
                Field("Category", form.category) { viewModel.updateForm { copy(category = it) } }
                Field("Cover image URL", form.coverImage) { viewModel.updateForm { copy(coverImage = it) } }
                Field("Total copies", form.totalCopies, keyboardType = KeyboardType.Number) { viewModel.updateForm { copy(totalCopies = it) } }
                if (form.id != null) {
                    Field("Available copies", form.availableCopies, keyboardType = KeyboardType.Number) { viewModel.updateForm { copy(availableCopies = it) } }
                }
                Field("Description", form.description, singleLine = false) { viewModel.updateForm { copy(description = it) } }

                Button(
                    onClick = { viewModel.submit() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text(if (form.id != null) "Save book" else "Create book", fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("Catalog overview", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF0F172A))
        if (state.isLoading) {
            CircularProgressIndicator(color = Color(0xFF4F46E5))
        } else if (state.books.isEmpty()) {
            InfoCard(title = "Catalog is empty", subtitle = "Add your first title above to make it searchable.")
        } else {
            state.books.forEach { book ->
                CatalogRow(book, onEdit = { viewModel.editBook(book) }, onDelete = { viewModel.delete(book.id) })
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, keyboardType: KeyboardType = KeyboardType.Text, singleLine: Boolean = true, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4F46E5))
    )
}

@Composable
private fun CatalogRow(book: Book, onEdit: () -> Unit, onDelete: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(book.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
            Text("${book.author} · ${book.isbn ?: "—"}", fontSize = 12.sp, color = Color(0xFF64748B))
            Text(
                "${book.availableCopies}/${book.totalCopies ?: book.availableCopies} available",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (book.availableCopies > 0) Color(0xFF16A34A) else Color(0xFFDC2626)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onDelete) { Text("Delete", color = Color(0xFFDC2626)) }
            }
        }
    }
}
