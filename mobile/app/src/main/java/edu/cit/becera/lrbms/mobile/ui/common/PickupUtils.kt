package edu.cit.becera.lrbms.mobile.ui.common

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

/** The library closes at 6 PM; an approved booking not collected by then expires. */
const val CLOSING_HOUR = 18
const val CLOSING_LABEL = "6:00 PM"

data class PickupCountdown(val expired: Boolean, val label: String)

fun pickupCountdown(pickupDate: String?): PickupCountdown {
    if (pickupDate == null) return PickupCountdown(expired = false, label = "Ready for pickup")
    val deadline = runCatching { LocalDate.parse(pickupDate).atTime(CLOSING_HOUR, 0) }.getOrNull()
        ?: return PickupCountdown(expired = false, label = "Ready for pickup")
    val remaining = Duration.between(LocalDateTime.now(), deadline)
    if (remaining.isNegative || remaining.isZero) return PickupCountdown(expired = true, label = "Pickup window expired")

    val days = remaining.toDays()
    val hours = remaining.toHours() % 24
    val minutes = remaining.toMinutes() % 60
    val label = if (days >= 1) "${days}d ${hours}h ${minutes}m left to pick up" else "${hours}h ${minutes}m left to pick up"
    return PickupCountdown(expired = false, label = label)
}

fun pickupByLabel(pickupDate: String?): String = "by $CLOSING_LABEL on ${pickupDate ?: "—"}"
