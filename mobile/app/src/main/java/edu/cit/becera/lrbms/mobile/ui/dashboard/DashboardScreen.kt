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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFEFF2FF))))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Library Resource Booking", color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Text("Welcome back!", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
            }
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(4.dp))
                Text("Logout", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Surface(
            shape = RoundedCornerShape(24.dp), 
            color = Color.White, 
            shadowElevation = 2.dp, 
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Your activity overview", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF0F172A))
                Text("Reservations, borrowings, and library updates in one view.", color = Color(0xFF64748B), fontSize = 15.sp)
            }
        }

        Text("Required Resources", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF0F172A), modifier = Modifier.padding(top = 8.dp))
        
        val requiredFiles = listOf(
            "Library Guidelines.pdf",
            "Borrowing Agreement.docx",
            "Resource Catalog 2024.pdf"
        )

        requiredFiles.forEach { fileName ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(24.dp))
                    Column {
                        Text(fileName, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 15.sp)
                        Text("Required for all members", fontSize = 13.sp, color = Color(0xFF64748B))
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(24.dp), 
            color = Color.White, 
            shadowElevation = 2.dp, 
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Upcoming reservations", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F172A))
                Text("Study room • 3:30 PM", color = Color(0xFF475569), fontSize = 15.sp)
                Text("Audiobook • 7:00 PM", color = Color(0xFF475569), fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun DashboardPreview() {
    DashboardScreen(onLogout = {})
}
