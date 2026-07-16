package edu.cit.becera.lrbms.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import edu.cit.becera.lrbms.mobile.data.local.SessionManager
import edu.cit.becera.lrbms.mobile.ui.auth.AuthNavHost
import edu.cit.becera.lrbms.mobile.ui.theme.BeceraLMSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BeceraLMSTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var ready by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        SessionManager.restore(context)
        ready = true
    }

    if (ready) {
        AuthNavHost(startLoggedIn = SessionManager.current != null)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
