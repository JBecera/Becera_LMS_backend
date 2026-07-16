package edu.cit.becera.lrbms.mobile.ui.borrowing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.cit.becera.lrbms.mobile.data.model.Transaction
import edu.cit.becera.lrbms.mobile.ui.dashboard.InfoCard
import edu.cit.becera.lrbms.mobile.ui.dashboard.daysRemaining

@Composable
fun MyBorrowingScreen(viewModel: MyBorrowingViewModel = viewModel()) {
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
        Text("My Borrowing", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
        Text("Everything currently checked out under your account.", fontSize = 13.sp, color = Color(0xFF64748B))

        if (state.isLoading) {
            CircularProgressIndicator(color = Color(0xFF4F46E5))
        } else if (state.activeLoans.isEmpty()) {
            InfoCard(title = "Nothing borrowed yet", subtitle = "Reserved titles move here once a librarian checks them out to you.")
        } else {
            state.activeLoans.forEach { loan -> LoanRow(loan) }
        }
    }
}

@Composable
private fun LoanRow(loan: Transaction) {
    val remaining = daysRemaining(loan.dueDate)
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(loan.resourceTitle ?: "Resource #${loan.resourceId}", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 15.sp)
            loan.resourceCategory?.let { Text(it, fontSize = 12.sp, color = Color(0xFF94A3B8)) }
            Text("Checked out ${loan.checkOutDate ?: "—"}", fontSize = 12.sp, color = Color(0xFF64748B))
            Text(
                if (remaining != null && remaining < 0) "${-remaining}d overdue" else "Due ${loan.dueDate ?: "—"}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (remaining != null && remaining < 0) Color(0xFFDC2626) else Color(0xFF4F46E5)
            )
        }
    }
}
