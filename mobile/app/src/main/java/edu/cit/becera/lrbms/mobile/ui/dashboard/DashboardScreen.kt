package edu.cit.becera.lrbms.mobile.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFEFF2FF))))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Library Resource Booking", color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Welcome to your dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            }
            Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF0F172A))) {
                Text("Logout")
            }
        }

        Surface(shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Your activity overview", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F172A))
                Text("Reservations, borrowings, and library updates in one view.", color = Color(0xFF64748B))
            }
        }

        Surface(shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Upcoming reservations", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text("Study room • 3:30 PM", color = Color(0xFF475569))
                Text("Audiobook • 7:00 PM", color = Color(0xFF475569))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
