package edu.cit.becera.lrbms.mobile.ui.dashboard

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
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
            Text("Library Resource Booking", color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(
                if (state.firstName.isNotBlank()) "Welcome back, ${state.firstName}." else "Welcome back!",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )
        }

        if (state.isLoading) {
            CircularProgressIndicator(color = Color(0xFF4F46E5))
        }

        state.errorMessage?.let {
            Text(it, color = Color(0xFFDC2626), fontSize = 14.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(modifier = Modifier.weight(1f), label = "On loan", value = state.activeLoans.size.toString(), hint = "Limit of 3")
            StatTile(modifier = Modifier.weight(1f), label = "Overdue", value = state.overdueCount.toString(), danger = state.overdueCount > 0)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(modifier = Modifier.weight(1f), label = "Pending reservations", value = state.pendingReservations.toString())
            StatTile(modifier = Modifier.weight(1f), label = "Fines due", value = "₱${"%.2f".format(state.totalOwed)}", danger = state.totalOwed > 0)
        }

        Text("Currently borrowed", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF0F172A))

        if (state.activeLoans.isEmpty() && !state.isLoading) {
            InfoCard(title = "No active loans", subtitle = "Reserve a title from the catalog to get started.")
        } else {
            state.activeLoans.forEach { loan ->
                val remaining = daysRemaining(loan.dueDate)
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(loan.resourceTitle ?: "Resource #${loan.resourceId}", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 15.sp)
                        Text(
                            if (remaining != null && remaining < 0) "${-remaining}d overdue" else "Due ${loan.dueDate ?: "—"}",
                            fontSize = 13.sp,
                            color = if (remaining != null && remaining < 0) Color(0xFFDC2626) else Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(modifier: Modifier = Modifier, label: String, value: String, hint: String? = null, danger: Boolean = false) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp, modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, fontSize = 12.sp, color = Color(0xFF64748B))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = if (danger) Color(0xFFDC2626) else Color(0xFF0F172A))
            hint?.let { Text(it, fontSize = 11.sp, color = Color(0xFF94A3B8)) }
        }
    }
}

@Composable
internal fun InfoCard(title: String, subtitle: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.Start) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
            Text(subtitle, fontSize = 13.sp, color = Color(0xFF64748B))
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun DashboardPreview() {
    DashboardScreen()
}
