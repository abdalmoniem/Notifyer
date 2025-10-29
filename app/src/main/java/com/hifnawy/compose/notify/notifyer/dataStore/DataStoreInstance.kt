package com.hifnawy.compose.notify.notifyer.dataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hifnawy.compose.notify.notifyer.model.Notification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * A class that provides a data store instance that stores the saved notifications.
 *
 * @constructor Creates a DataStoreInstance with the given context.
 */
object DataStoreInstance {

    /**
     * The key used to store the notifications in the data store.
     *
     * @return [Preferences.Key<String>][Preferences.Key] The key used to store the notifications.
     * @see DataStoreInstance.saveNotifications
     * @see DataStoreInstance.getNotifications
     */
    private val NOTIFICATIONS_KEY = stringPreferencesKey("notifications_key")

    /**
     * Returns a DataStore instance that stores the saved notifications.
     * The DataStore instance is created using the preferences data store builder.
     * The name of the data store is "saved_notifications".
     *
     * @return [DataStore<Preferences>] The DataStore instance.
     */
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "saved_notifications")

    /**
     * Saves a list of notifications to the data store.
     * The notifications are stored as a JSON string in the data store.
     * The key used to store the notifications is "notifications_key".
     *
     * @param objects [List<Notification>][Notification] The list of notifications to be saved.
     */
    suspend fun saveNotifications(context: Context, objects: List<Notification>) {
        context.dataStore.edit { preferences ->
            val jsonString = Json.encodeToString(objects)
            preferences[NOTIFICATIONS_KEY] = jsonString
        }
    }

    /**
     * Retrieves a flow of a list of notifications from the data store.
     * The list of notifications is retrieved from the data store using the key "notifications_key".
     * If no value is found in the data store, an empty list is returned.
     *
     * @return [Flow<List<Notification>>][Notification] A flow of a list of notifications.
     */
    fun getNotifications(context: Context): Flow<List<Notification>> {
        return context.dataStore.data.map { preferences ->
            val jsonString = preferences[NOTIFICATIONS_KEY] ?: "[]"
            Json.decodeFromString<List<Notification>>(jsonString)
        }
    }

    /**
     * Adds a notification to the list of notifications stored in the data store.
     * If the list of notifications does not exist in the data store, a new list is created.
     * The notification is added to the list and the list is then stored in the data store.
     *
     * @param notification [Notification] The notification to be added.
     */
    suspend fun addNotification(context: Context, notification: Notification) {
        context.dataStore.edit { preferences ->
            val jsonString = preferences[NOTIFICATIONS_KEY] ?: "[]"
            val notifications = Json.decodeFromString<MutableList<Notification>>(jsonString)
            notifications.add(notification)
            preferences[NOTIFICATIONS_KEY] = Json.encodeToString(notifications)
        }
    }
}