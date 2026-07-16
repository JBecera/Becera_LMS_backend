package edu.cit.becera.lrbms.mobile.ui.reservations

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
import edu.cit.becera.lrbms.mobile.data.model.Reservation
import edu.cit.becera.lrbms.mobile.ui.dashboard.InfoCard

@Composable
fun ReservationsScreen(viewModel: ReservationsViewModel = viewModel()) {
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
        Text("My Reservations", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
        Text("Track the resources you've reserved and their approval status.", fontSize = 13.sp, color = Color(0xFF64748B))

        state.message?.let { Text(it, fontSize = 13.sp, color = Color(0xFF4F46E5)) }

        if (state.isLoading) {
            CircularProgressIndicator(color = Color(0xFF4F46E5))
        } else if (state.reservations.isEmpty()) {
            InfoCard(title = "No reservations", subtitle = "Reserve a title from the catalog to see it here.")
        } else {
            state.reservations.forEach { reservation ->
                ReservationRow(reservation, onCancel = { viewModel.cancel(reservation.id) })
            }
        }
    }
}

@Composable
private fun ReservationRow(reservation: Reservation, onCancel: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(reservation.resourceTitle ?: "Resource #${reservation.resourceId}", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 15.sp)
                Text("Reserved ${reservation.reservationDate ?: "—"} · ${reservation.status}", fontSize = 12.sp, color = Color(0xFF64748B))
            }
            if (reservation.status == "PENDING") {
                Button(
                    onClick = onCancel,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = Color(0xFFDC2626))
                ) {
                    Text("Cancel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
