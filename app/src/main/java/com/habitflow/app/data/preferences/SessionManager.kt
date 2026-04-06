package com.habitflow.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.habitflow.app.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID      = stringPreferencesKey("user_id")
        val USER_NAME    = stringPreferencesKey("user_name")
        val USER_EMAIL   = stringPreferencesKey("user_email")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_LOGGED_IN] ?: false
    }

    val currentUser: Flow<User?> = context.dataStore.data.map { prefs ->
        val id    = prefs[Keys.USER_ID] ?: return@map null
        val name  = prefs[Keys.USER_NAME] ?: return@map null
        val email = prefs[Keys.USER_EMAIL] ?: return@map null
        User(id = id, name = name, email = email)
    }

    suspend fun saveSession(user: User) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = true
            prefs[Keys.USER_ID]      = user.id
            prefs[Keys.USER_NAME]    = user.name
            prefs[Keys.USER_EMAIL]   = user.email
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs -> prefs.clear() }
    }

    suspend fun isLoggedInSync(): Boolean {
        return context.dataStore.data.first()[Keys.IS_LOGGED_IN] ?: false
    }
}
