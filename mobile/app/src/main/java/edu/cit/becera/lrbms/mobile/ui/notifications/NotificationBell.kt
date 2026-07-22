package edu.cit.becera.lrbms.mobile.ui.notifications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NotificationBell(viewModel: NotificationViewModel = viewModel()) {
    val notifications by viewModel.notifications.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val unreadCount = notifications.count { !it.read }

    LaunchedEffect(Unit) { viewModel.startPolling() }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color(0xFF0F172A))
            if (unreadCount > 0) {
                Surface(shape = CircleShape, color = Color(0xFFDC2626), modifier = Modifier.size(16.dp)) {
                    Text(
                        unreadCount.toString(),
                        fontSize = 9.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Column(modifier = Modifier.width(280.dp).padding(horizontal = 12.dp, vertical = 4.dp)) {
                Box(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0F172A))
                }
                if (unreadCount > 0) {
                    TextButton(onClick = { viewModel.markAllRead() }) { Text("Mark all read", fontSize = 12.sp) }
                }
            }
            HorizontalDivider()
            if (notifications.isEmpty()) {
                DropdownMenuItem(text = { Text("You're all caught up.", fontSize = 13.sp, color = Color(0xFF64748B)) }, onClick = {})
            } else {
                notifications.take(10).forEach { n ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    n.message,
                                    fontSize = 13.sp,
                                    fontWeight = if (n.read) FontWeight.Normal else FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        },
                        onClick = { viewModel.markRead(n.id) }
                    )
                }
            }
        }
    }
}
