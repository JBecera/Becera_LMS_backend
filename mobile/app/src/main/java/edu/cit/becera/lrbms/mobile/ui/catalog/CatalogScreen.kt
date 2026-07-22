package edu.cit.becera.lrbms.mobile.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.cit.becera.lrbms.mobile.data.model.Book
import edu.cit.becera.lrbms.mobile.ui.common.DateField
import edu.cit.becera.lrbms.mobile.ui.dashboard.InfoCard
import java.time.LocalDate

@Composable
fun CatalogScreen(viewModel: CatalogViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFEFF2FF))))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Search the catalog", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
        Text("Browse by title or author, pick a date to see what's available then, and book for pickup.", fontSize = 13.sp, color = Color(0xFF64748B))

        OutlinedTextField(
            value = state.query,
            onValueChange = { viewModel.search(it) },
            label = { Text("Title or author") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4F46E5))
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateField(
                label = "Pickup date",
                value = state.pickupDate,
                onValueChange = { viewModel.setPickupDate(it) },
                minDate = LocalDate.now(),
                maxDate = LocalDate.now().plusDays(30),
                modifier = Modifier.weight(1f)
            )
            if (state.pickupDate.isNotBlank()) {
                TextButton(onClick = { viewModel.clearPickupDate() }) { Text("Clear") }
            }
        }
        Text(
            if (state.pickupDate.isNotBlank()) "Showing availability for ${state.pickupDate}. A librarian still approves each booking."
            else "Showing copies available now. Pick a date to book in advance.",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )

        state.message?.let {
            Text(it, fontSize = 13.sp, color = Color(0xFF4F46E5))
        }

        if (state.isLoading) {
            CircularProgressIndicator(color = Color(0xFF4F46E5))
        } else if (state.visibleBooks.isEmpty()) {
            InfoCard(title = "No matching titles", subtitle = "Try a different title or author, or clear your search.")
        } else {
            state.visibleBooks.forEach { book ->
                BookRow(book = book, count = viewModel.availabilityFor(book), datedView = state.pickupDate.isNotBlank(), onReserve = { viewModel.reserve(book.id) })
            }
        }
    }
}

@Composable
private fun BookRow(book: Book, count: Int, datedView: Boolean, onReserve: () -> Unit) {
    val canBook = count > 0
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(book.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
            Text(book.author, fontSize = 13.sp, color = Color(0xFF64748B))
            book.category?.let { Text(it, fontSize = 12.sp, color = Color(0xFF94A3B8)) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (canBook) "$count available${if (datedView) " that day" else ""}" else if (datedView) "None free that day" else "Out of stock",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (canBook) Color(0xFF16A34A) else Color(0xFFDC2626)
                )
                Button(
                    onClick = onReserve,
                    enabled = canBook,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text("Book for pickup", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
