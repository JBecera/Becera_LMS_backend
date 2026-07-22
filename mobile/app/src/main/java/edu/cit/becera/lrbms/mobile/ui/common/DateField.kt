package edu.cit.becera.lrbms.mobile.ui.common

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

/** A read-only text field that opens the native Android date picker on tap. */
@Composable
fun DateField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null
) {
    val context = LocalContext.current
    val initial = runCatching { LocalDate.parse(value, isoFormatter) }.getOrDefault(minDate ?: LocalDate.now())

    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        enabled = false,
        readOnly = true,
        trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF4F46E5)) },
        modifier = modifier.clickable {
            val dialog = DatePickerDialog(
                context,
                { _, year, month, day -> onValueChange(LocalDate.of(year, month + 1, day).format(isoFormatter)) },
                initial.year, initial.monthValue - 1, initial.dayOfMonth
            )
            minDate?.let { dialog.datePicker.minDate = it.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }
            maxDate?.let { dialog.datePicker.maxDate = it.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }
            dialog.show()
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = Color(0xFF0F172A),
            disabledBorderColor = Color(0xFFCBD5E1),
            disabledLabelColor = Color(0xFF64748B),
            disabledTrailingIconColor = Color(0xFF4F46E5)
        )
    )
}

fun todayIso(): String = LocalDate.now().format(isoFormatter)
