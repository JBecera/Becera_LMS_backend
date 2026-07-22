package edu.cit.becera.lrbms.mobile.ui.fines

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import edu.cit.becera.lrbms.mobile.data.model.Fine
import edu.cit.becera.lrbms.mobile.ui.dashboard.InfoCard

@Composable
fun FinesScreen(viewModel: FinesViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    val totalOwed = state.fines.filter { it.paymentStatus != "PAID" }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFEFF2FF))))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(if (state.isStaff) "Fines & Penalties" else "My Fines", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
        Text(
            if (state.isStaff) "Overdue penalties across all members. Mark a fine settled once it's paid at the desk."
            else "Outstanding penalties on your account. Unpaid fines restrict new borrowing and reservations.",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )

        state.errorMessage?.let { Text(it, fontSize = 13.sp, color = Color(0xFF4F46E5)) }

        if (!state.isStaff) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total owed", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text(
                        "₱${"%.2f".format(totalOwed)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (totalOwed > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                    )
                }
            }
        }

        if (state.isLoading) {
            CircularProgressIndicator(color = Color(0xFF4F46E5))
        } else if (state.fines.isEmpty()) {
            InfoCard(title = "No fines on record", subtitle = "Overdue penalties will be listed here as soon as they're issued.")
        } else {
            state.fines.forEach { fine -> FineRow(fine, isStaff = state.isStaff, onSettle = { viewModel.settle(fine.id) }) }
        }
    }
}

@Composable
private fun FineRow(fine: Fine, isStaff: Boolean, onSettle: () -> Unit) {
    val paid = fine.paymentStatus == "PAID"
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    if (isStaff) {
                        Text(fine.memberName ?: "Member #${fine.memberId}", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 14.sp)
                    }
                    Text(fine.reason ?: "Fine", fontWeight = if (isStaff) FontWeight.Normal else FontWeight.Bold, color = Color(0xFF1E293B), fontSize = if (isStaff) 12.sp else 14.sp)
                    Text("Issued ${fine.dateIssued ?: "—"}", fontSize = 12.sp, color = Color(0xFF64748B))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₱${"%.2f".format(fine.amount)}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    Text(if (paid) "Settled" else "Unpaid", fontSize = 12.sp, color = if (paid) Color(0xFF16A34A) else Color(0xFFDC2626))
                }
            }
            if (isStaff && !paid) {
                Button(
                    onClick = onSettle,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Text("Mark settled", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
