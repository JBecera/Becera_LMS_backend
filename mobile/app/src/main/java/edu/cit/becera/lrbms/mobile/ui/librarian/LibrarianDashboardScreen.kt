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
import edu.cit.becera.lrbms.mobile.ui.dashboard.InfoCard

@Composable
fun LibrarianDashboardScreen(viewModel: LibrarianDashboardViewModel = viewModel()) {
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
        Column {
            Text("Librarian Workspace", color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(
                "Hello, ${state.firstName.ifBlank { "Librarian" }}.",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )
        }

        if (state.isLoading) {
            CircularProgressIndicator(color = Color(0xFF4F46E5))
        }
        state.errorMessage?.let { Text(it, color = Color(0xFFDC2626), fontSize = 14.sp) }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(Modifier.weight(1f), "Catalog titles", state.catalogTitles.toString())
            StatTile(Modifier.weight(1f), "Active loans", state.activeLoans.toString())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(Modifier.weight(1f), "Overdue", state.overdue.toString(), danger = state.overdue > 0)
            StatTile(Modifier.weight(1f), "Pending reservations", state.pendingReservations.size.toString(), danger = state.pendingReservations.isNotEmpty())
        }

        Text("Reservation queue", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF0F172A))
        if (state.pendingReservations.isEmpty()) {
            InfoCard(title = "Queue is clear", subtitle = "No reservations are waiting on approval right now.")
        } else {
            state.pendingReservations.forEach { r ->
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(r.resourceTitle ?: "Resource #${r.resourceId}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                        Text("${r.memberName ?: "Member #${r.memberId}"} · requested ${r.reservationDate ?: "—"}", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }

        if (state.unpaidFineMembers > 0) {
            InfoCard(title = "Unpaid fines", subtitle = "${state.unpaidFineMembers} member${if (state.unpaidFineMembers == 1) "" else "s"} with an outstanding balance.")
        }
    }
}

@Composable
private fun StatTile(modifier: Modifier = Modifier, label: String, value: String, danger: Boolean = false) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp, modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, fontSize = 12.sp, color = Color(0xFF64748B))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = if (danger) Color(0xFFDC2626) else Color(0xFF0F172A))
        }
    }
}
