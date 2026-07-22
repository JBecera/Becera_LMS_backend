package edu.cit.becera.lrbms.mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.cit.becera.lrbms.mobile.data.model.Reservation
import kotlinx.coroutines.flow.first
import java.time.Instant

private val Context.notificationsDataStore by preferencesDataStore(name = "notifications")

data class BookingNotification(
    val id: String,
    val reservationId: Long,
    val status: String,
    val title: String,
    val message: String,
    val createdAt: String,
    val read: Boolean
)

/**
 * Client-side-only notification center for booking status changes, mirroring the web app's
 * localStorage diffing: no push/websocket backend, so each poll compares fetched reservations
 * against a per-member snapshot of last-seen statuses and turns PENDING -> APPROVED/REJECTED
 * transitions into notifications.
 */
object NotificationStore {
    private val gson = Gson()
    private val notificationListType = object : TypeToken<List<BookingNotification>>() {}.type
    private val snapshotMapType = object : TypeToken<Map<Long, String>>() {}.type

    private fun notificationsKey(userId: Long) = stringPreferencesKey("notifications_$userId")
    private fun snapshotKey(userId: Long) = stringPreferencesKey("snapshot_$userId")

    suspend fun getNotifications(context: Context, userId: Long): List<BookingNotification> {
        val raw = context.notificationsDataStore.data.first()[notificationsKey(userId)] ?: return emptyList()
        return runCatching { gson.fromJson<List<BookingNotification>>(raw, notificationListType) }.getOrDefault(emptyList())
    }

    suspend fun markRead(context: Context, userId: Long, id: String): List<BookingNotification> {
        val updated = getNotifications(context, userId).map { if (it.id == id) it.copy(read = true) else it }
        save(context, userId, updated)
        return updated
    }

    suspend fun markAllRead(context: Context, userId: Long): List<BookingNotification> {
        val updated = getNotifications(context, userId).map { it.copy(read = true) }
        save(context, userId, updated)
        return updated
    }

    suspend fun syncBookingNotifications(context: Context, userId: Long, reservations: List<Reservation>): List<BookingNotification> {
        val snapshotRaw = context.notificationsDataStore.data.first()[snapshotKey(userId)]
        val snapshot = (runCatching { gson.fromJson<Map<Long, String>>(snapshotRaw, snapshotMapType) }.getOrNull() ?: emptyMap()).toMutableMap()
        val notifications = getNotifications(context, userId).toMutableList()
        var changed = false

        reservations.forEach { r ->
            val previousStatus = snapshot[r.id]
            if (previousStatus != r.status && (r.status == "APPROVED" || r.status == "REJECTED")) {
                val alreadyNotified = notifications.any { it.reservationId == r.id && it.status == r.status }
                if (!alreadyNotified) {
                    val title = r.resourceTitle ?: "Your booking"
                    val message = if (r.status == "APPROVED") {
                        "$title was approved — pick it up within 3 days."
                    } else {
                        "$title was rejected" + (r.reason?.let { ": $it" } ?: ".")
                    }
                    notifications.add(
                        0,
                        BookingNotification(
                            id = "${r.id}-${r.status}-${System.currentTimeMillis()}",
                            reservationId = r.id,
                            status = r.status,
                            title = title,
                            message = message,
                            createdAt = Instant.now().toString(),
                            read = false
                        )
                    )
                    changed = true
                }
            }
            if (previousStatus != r.status) {
                snapshot[r.id] = r.status
                changed = true
            }
        }

        if (changed) {
            save(context, userId, notifications)
            context.notificationsDataStore.edit { it[snapshotKey(userId)] = gson.toJson(snapshot) }
        }

        return notifications
    }

    private suspend fun save(context: Context, userId: Long, notifications: List<BookingNotification>) {
        context.notificationsDataStore.edit { it[notificationsKey(userId)] = gson.toJson(notifications) }
    }
}
