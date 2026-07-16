package edu.cit.becera.lrbms.mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "session")

data class UserSession(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
    val token: String
)

/**
 * Persists the logged-in member across app restarts (DataStore) while also keeping an
 * in-memory copy so the Retrofit auth interceptor can attach the bearer token synchronously.
 */
object SessionManager {

    private object Keys {
        val ID = longPreferencesKey("id")
        val FIRST_NAME = stringPreferencesKey("firstName")
        val LAST_NAME = stringPreferencesKey("lastName")
        val EMAIL = stringPreferencesKey("email")
        val ROLE = stringPreferencesKey("role")
        val TOKEN = stringPreferencesKey("token")
    }

    @Volatile
    var current: UserSession? = null
        private set

    suspend fun restore(context: Context) {
        val prefs = context.applicationContext.dataStore.data.first()
        val token = prefs[Keys.TOKEN]
        val id = prefs[Keys.ID]
        current = if (token != null && id != null) {
            UserSession(
                id = id,
                firstName = prefs[Keys.FIRST_NAME] ?: "",
                lastName = prefs[Keys.LAST_NAME] ?: "",
                email = prefs[Keys.EMAIL] ?: "",
                role = prefs[Keys.ROLE] ?: "MEMBER",
                token = token
            )
        } else {
            null
        }
    }

    suspend fun save(context: Context, session: UserSession) {
        current = session
        context.applicationContext.dataStore.edit { prefs ->
            prefs[Keys.ID] = session.id
            prefs[Keys.FIRST_NAME] = session.firstName
            prefs[Keys.LAST_NAME] = session.lastName
            prefs[Keys.EMAIL] = session.email
            prefs[Keys.ROLE] = session.role
            prefs[Keys.TOKEN] = session.token
        }
    }

    suspend fun clear(context: Context) {
        current = null
        context.applicationContext.dataStore.edit { it.clear() }
    }
}
