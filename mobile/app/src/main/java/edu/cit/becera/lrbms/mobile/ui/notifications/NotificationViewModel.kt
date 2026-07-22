package edu.cit.becera.lrbms.mobile.ui.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.becera.lrbms.mobile.data.local.BookingNotification
import edu.cit.becera.lrbms.mobile.data.local.NotificationStore
import edu.cit.becera.lrbms.mobile.data.local.SessionManager
import edu.cit.becera.lrbms.mobile.data.remote.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val POLL_INTERVAL_MS = 20_000L

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val _notifications = MutableStateFlow<List<BookingNotification>>(emptyList())
    val notifications: StateFlow<List<BookingNotification>> = _notifications

    private var polling = false

    fun startPolling() {
        val session = SessionManager.current ?: return
        if (polling) return
        polling = true
        viewModelScope.launch {
            _notifications.value = NotificationStore.getNotifications(getApplication(), session.id)
            while (true) {
                sync(session.id)
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun sync(userId: Long) {
        try {
            val reservations = RetrofitClient.reservationApi.getMemberReservations(userId)
            _notifications.value = NotificationStore.syncBookingNotifications(getApplication(), userId, reservations)
        } catch (e: Exception) {
            // Notifications are a best-effort convenience; a failed poll just retries next tick.
        }
    }

    fun markRead(id: String) {
        val session = SessionManager.current ?: return
        viewModelScope.launch {
            _notifications.value = NotificationStore.markRead(getApplication(), session.id, id)
        }
    }

    fun markAllRead() {
        val session = SessionManager.current ?: return
        viewModelScope.launch {
            _notifications.value = NotificationStore.markAllRead(getApplication(), session.id)
        }
    }
}
