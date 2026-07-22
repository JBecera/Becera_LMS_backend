package edu.cit.becera.lrbms.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    secondary = Color(0xFF7C94FF),
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    outline = Color(0xFFCBD5E1)
)

// Every screen is hand-styled with hardcoded light/white surfaces (matching the web app, which
// has no dark-mode support either), so the app always renders in light mode regardless of the
// system theme. Without this, a device in dark mode gets Material's default dark on-surface
// (white) text color rendered on these hardcoded white cards, making typed input invisible.
@Composable
fun BeceraLMSTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
