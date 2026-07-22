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
import edu.cit.becera.lrbms.mobile.data.model.Reservation
import edu.cit.becera.lrbms.mobile.data.model.Transaction
import edu.cit.becera.lrbms.mobile.ui.common.DateField
import edu.cit.becera.lrbms.mobile.ui.common.pickupCountdown
import edu.cit.becera.lrbms.mobile.ui.dashboard.InfoCard
import java.time.LocalDate

@Composable
fun ManageBookingsScreen(viewModel: ManageBookingsViewModel = viewModel()) {
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
        Text("Booking approvals", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
        Text("Approve or reject requests, hand titles over at pickup, and record returns.", fontSize = 13.sp, color = Color(0xFF64748B))

        state.message?.let { Text(it, fontSize = 13.sp, color = Color(0xFF4F46E5)) }

        if (state.isLoading) {
            CircularProgressIndicator(color = Color(0xFF4F46E5))
            return@Column
        }

        SectionTitle("Pending approvals")
        if (state.pending.isEmpty()) {
            InfoCard(title = "Nothing pending", subtitle = "New booking requests from members will appear here.")
        } else {
            state.pending.forEach { reservation -> PendingCard(reservation, state, viewModel) }
        }

        SectionTitle("Awaiting pickup")
        if (state.approved.isEmpty()) {
            InfoCard(title = "Nothing awaiting pickup", subtitle = "Approved bookings appear here until the member collects them.")
        } else {
            state.approved.forEach { reservation -> ApprovedCard(reservation, state, viewModel) }
        }

        SectionTitle("On loan / Returns")
        if (state.activeLoans.isEmpty()) {
            InfoCard(title = "Nothing on loan", subtitle = "Checked-out titles appear here until they're returned.")
        } else {
            state.activeLoans.forEach { transaction -> LoanCard(transaction, onReturn = { viewModel.markReturned(transaction.id) }) }
        }

        SectionTitle("History")
        if (state.history.isEmpty()) {
            InfoCard(title = "No history yet", subtitle = "Rejected and completed bookings will appear here.")
        } else {
            state.history.forEach { reservation -> HistoryRow(reservation) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF0F172A))
}

@Composable
private fun PendingCard(reservation: Reservation, state: ManageBookingsUiState, viewModel: ManageBookingsViewModel) {
    val isRejecting = state.rejectDrafts.containsKey(reservation.id)
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(reservation.resourceTitle ?: "Resource #${reservation.resourceId}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
            Text(reservation.memberName ?: "Member #${reservation.memberId}", fontSize = 12.sp, color = Color(0xFF64748B))
            Text("Pickup date: ${reservation.pickupDate ?: "—"} · Booked ${reservation.reservationDate ?: "—"}", fontSize = 12.sp, color = Color(0xFF94A3B8))
            Text(
                "Active loans: ${state.activeLoanCount(reservation.memberId)} · Unpaid fines: ₱${"%.2f".format(state.unpaidFineTotal(reservation.memberId))}",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )

            if (isRejecting) {
                OutlinedTextField(
                    value = state.rejectDrafts[reservation.id] ?: "",
                    onValueChange = { viewModel.setRejectDraft(reservation.id, it) },
                    label = { Text("Rejection reason") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4F46E5))
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.confirmReject(reservation.id) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))) {
                        Text("Confirm reject")
                    }
                    TextButton(onClick = { viewModel.setRejectDraft(reservation.id, null) }) { Text("Cancel") }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.approve(reservation.id) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))) {
                        Text("Approve")
                    }
                    Button(
                        onClick = { viewModel.setRejectDraft(reservation.id, "") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF0F172A))
                    ) {
                        Text("Reject")
                    }
                }
            }
        }
    }
}

@Composable
private fun ApprovedCard(reservation: Reservation, state: ManageBookingsUiState, viewModel: ManageBookingsViewModel) {
    val countdown = pickupCountdown(reservation.pickupDate)
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(reservation.resourceTitle ?: "Resource #${reservation.resourceId}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
            Text(reservation.memberName ?: "Member #${reservation.memberId}", fontSize = 12.sp, color = Color(0xFF64748B))
            Text("Pickup date: ${reservation.pickupDate ?: "—"}", fontSize = 12.sp, color = Color(0xFF94A3B8))
            Text(countdown.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (countdown.expired) Color(0xFFDC2626) else Color(0xFF16A34A))

            if (countdown.expired) {
                Button(onClick = { viewModel.cancel(reservation.id) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))) {
                    Text("Cancel")
                }
            } else {
                DateField(
                    label = "Due date",
                    value = state.checkoutDrafts[reservation.id] ?: viewModel.defaultDueDate(),
                    onValueChange = { viewModel.setCheckoutDraft(reservation.id, it) },
                    minDate = LocalDate.now().plusDays(1),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = { viewModel.markPickedUp(reservation) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))) {
                    Text("Mark picked up")
                }
            }
        }
    }
}

@Composable
private fun LoanCard(transaction: Transaction, onReturn: () -> Unit) {
    val overdue = transaction.dueDate?.let { it < LocalDate.now().toString() } ?: false
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(transaction.resourceTitle ?: "Resource #${transaction.resourceId}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                Text(transaction.memberName ?: "Member #${transaction.memberId}", fontSize = 12.sp, color = Color(0xFF64748B))
                Text(
                    if (overdue) "Overdue · due ${transaction.dueDate}" else "Due ${transaction.dueDate ?: "—"}",
                    fontSize = 12.sp,
                    color = if (overdue) Color(0xFFDC2626) else Color(0xFF64748B)
                )
            }
            TextButton(onClick = onReturn) { Text("Mark returned") }
        }
    }
}

@Composable
private fun HistoryRow(reservation: Reservation) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(reservation.resourceTitle ?: "Resource #${reservation.resourceId}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
            Text("${reservation.memberName ?: "Member #${reservation.memberId}"} · ${reservation.status}", fontSize = 12.sp, color = Color(0xFF64748B))
            if (reservation.status == "REJECTED" && reservation.reason != null) {
                Text("Reason: ${reservation.reason}", fontSize = 12.sp, color = Color(0xFFDC2626))
            }
        }
    }
}
