package com.hifnawy.compose.notify.notifyer.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.SquareIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.hifnawy.compose.notify.notifyer.MainActivity
import com.hifnawy.compose.notify.notifyer.ONotificationManager
import com.hifnawy.compose.notify.notifyer.R
import com.hifnawy.compose.notify.notifyer.dataStore.DataStoreInstance
import com.hifnawy.compose.notify.notifyer.model.Notification
import com.hifnawy.compose.notify.notifyer.ui.components.AppWidget.Companion.KEY_INDEX
import com.hifnawy.compose.notify.notifyer.ui.components.SendNotificationActionParameters.NotificationId
import com.hifnawy.compose.notify.notifyer.ui.components.SendNotificationActionParameters.NotificationMessage
import com.hifnawy.compose.notify.notifyer.ui.components.SendNotificationActionParameters.NotificationTitle
import com.hifnawy.compose.notify.notifyer.ui.theme.WidgetTheme
import kotlinx.coroutines.flow.first
import java.util.UUID

/**
 * Parameters for the [SendNotificationAction]
 *
 * @param keyName [String] The name of the key
 * @property NotificationId [SendNotificationActionParameters] The id of the notification to send
 * @property NotificationTitle [SendNotificationActionParameters] The title of the notification to send
 * @property NotificationMessage [SendNotificationActionParameters] The message of the notification to send
 */
private enum class SendNotificationActionParameters(keyName: String) {

    NotificationId("notification-id"),
    NotificationTitle("notification-title"),
    NotificationMessage("notification-message");

    val key: ActionParameters.Key<String> = ActionParameters.Key(keyName)
}

/**
 * A receiver for the [AppWidget] that is responsible for handling the lifecycle of the widget.
 *
 * This receiver is responsible for updating the widget when the user sends a notification from the widget.
 *
 * @see [AppWidget]
 */
class AppWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = AppWidget()
}

/**
 * The main widget for the notifications app.
 *
 * This widget displays a list of notifications and provides controls to send a notification.
 *
 */
class AppWidget : GlanceAppWidget() {

    /**
     * A companion object containing constants used by the [AppWidget].
     *
     * This object contains constants used by the [AppWidget] to access the preferences store.
     *
     * @see [AppWidget]
     */
    internal companion object {

        /**
         * The key used to store the index of the current notification in the list.
         *
         * @return [Preferences.Key] The key used to store the index of the current notification in the list.
         */
        val KEY_INDEX = intPreferencesKey("current_notification_index")
    }

    /**
     * Provides the content of the widget.
     *
     * This function is called when the widget is displayed and is responsible for providing the content of the widget.
     *
     * @param context [Context] The context of the widget.
     * @param id [GlanceId] The id of the widget.
     *
     * @see [provideContent]
     * @see [Content]
     */
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val notifications by DataStoreInstance.getNotifications(context).collectAsState(initial = emptyList())

            WidgetTheme {
                val prefs = currentState<Preferences>()
                val currentIndex = prefs[KEY_INDEX] ?: 0

                Content(currentIndex, notifications)
            }
        }
    }

    /**
     * The content of the widget.
     *
     * This function is called when the widget is displayed and is responsible for providing the content of the widget.
     *
     * @param currentIndex [Int] The index of the current notification in the list.
     * @param notifications [List<Notification>] The list of notifications to display.
     *
     */
    @Composable
    private fun Content(currentIndex: Int = 0, notifications: List<Notification>) {
        val context = LocalContext.current
        val isEmpty = notifications.isEmpty()
        val notification = notifications.getOrNull(currentIndex)

        if (isEmpty || notification == null) {
            Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .clickable(
                                onClick = actionStartActivity(
                                        Intent(context, MainActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                        }
                                )
                        )
                        .background(GlanceTheme.colors.surface)
                        .padding(16.dp)
                        .cornerRadius(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                        text = "No notifications",
                        style = TextStyle(
                                fontSize = 25.sp,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Normal,
                                color = GlanceTheme.colors.onSurface
                        )
                )
            }
        } else {
            Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .clickable(
                                onClick = actionStartActivity(
                                        Intent(context, MainActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                        }
                                )
                        )
                        .background(GlanceTheme.colors.surface)
                        .padding(16.dp)
                        .cornerRadius(16.dp),
            ) {
                Text(
                        text = notification.id.toString().uppercase(),
                        maxLines = 1,
                        style = TextStyle(
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Normal,
                                color = GlanceTheme.colors.onSurface
                        ),
                )

                Text(
                        text = notification.title,
                        maxLines = 1,
                        style = TextStyle(
                                fontSize = 20.sp,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Bold,
                                color = GlanceTheme.colors.onSurface
                        )
                )

                Text(
                        text = notification.message,
                        maxLines = 1,
                        style = TextStyle(
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Medium,
                                color = GlanceTheme.colors.onSurface
                        )
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

                Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    val buttonSize = 48.dp
                    SquareIconButton(
                            modifier = GlanceModifier.size(buttonSize),
                            imageProvider = ImageProvider(R.drawable.skip_previous_48px),
                            contentDescription = "Skip Previous",
                            onClick = actionRunCallback<PreviousNotificationAction>()
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    SquareIconButton(
                            modifier = GlanceModifier.size(buttonSize),
                            imageProvider = ImageProvider(R.drawable.notifications_24px),
                            contentDescription = "Notify",
                            onClick = actionRunCallback<SendNotificationAction>(
                                    parameters = actionParametersOf(
                                            NotificationId.key to notification.id.toString(),
                                            NotificationTitle.key to notification.title,
                                            NotificationMessage.key to notification.message
                                    )
                            )
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    SquareIconButton(
                            modifier = GlanceModifier.size(buttonSize),
                            imageProvider = ImageProvider(R.drawable.shuffle_24px),
                            contentDescription = "Get Random Notification",
                            onClick = actionRunCallback<RandomNotificationAction>()
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    SquareIconButton(
                            modifier = GlanceModifier.size(buttonSize),
                            imageProvider = ImageProvider(R.drawable.skip_next_48px),
                            contentDescription = "Skip Next",
                            onClick = actionRunCallback<NextNotificationAction>()
                    )
                }
            }
        }
    }

    /**
     * A preview of the content of the app widget.
     *
     * This function is called when the app widget is displayed and is responsible for providing the content of the app widget.
     *
     * @see [Content]
     */
    @Composable
    @Suppress("unused")
    @Preview(widthDp = 300, heightDp = 150)
    @OptIn(ExperimentalGlancePreviewApi::class)
    private fun ContentPreview() {
        val notifications = remember { mutableStateListOf<Notification>() }
        for (index in 1..30) {
            notifications.add(
                    Notification(
                            id = UUID.randomUUID(),
                            title = "Notification #${index.toString().padStart(2, '0')} Title",
                            message = "Notification #${index.toString().padStart(2, '0')} Message"
                    )
            )
        }

        Content(9, notifications)
    }
}

/**
 * An action that is triggered when the user clicks the next notification button.
 *
 * This action is responsible for updating the app widget state and updating the app widget.
 *
 * @see [AppWidget]
 */
internal class NextNotificationAction : ActionCallback {

    /**
     * Handles the action when the user clicks the next notification button.
     *
     * This function is called when the user clicks the next notification button and is responsible for updating the app widget state and updating the app widget.
     *
     * @param context [Context] The context of the app widget.
     * @param glanceId [GlanceId] The id of the app widget.
     * @param parameters [ActionParameters] The parameters of the action.
     *
     * @see [AppWidget]
     */
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val notifications = DataStoreInstance.getNotifications(context).first()
            val currentIndex = prefs[KEY_INDEX] ?: 0
            val nextIndex = when {
                notifications.isNotEmpty() -> (currentIndex + 1) % notifications.size
                else                       -> 0
            }
            prefs.toMutablePreferences().apply {
                set(KEY_INDEX, nextIndex)
            }
        }
        AppWidget().update(context, glanceId)
    }
}

/**
 * An action that is triggered when the user clicks the previous notification button.
 *
 * This action is responsible for updating the app widget state and updating the app widget.
 *
 * @see [AppWidget]
 */
internal class PreviousNotificationAction : ActionCallback {

    /**
     * Handles the action when the user clicks the previous notification button.
     *
     * This function is called when the user clicks the previous notification button and is responsible for updating the app widget state and updating the app widget.
     *
     * @param context [Context] The context of the app widget.
     * @param glanceId [GlanceId] The id of the app widget.
     * @param parameters [ActionParameters] The parameters of the action.
     *
     * @see [AppWidget]
     */
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val notifications = DataStoreInstance.getNotifications(context).first()
            val currentIndex = prefs[KEY_INDEX] ?: 0
            val prevIndex = when {
                notifications.isNotEmpty() -> (currentIndex - 1 + notifications.size) % notifications.size
                else                       -> 0
            }
            prefs.toMutablePreferences().apply {
                set(KEY_INDEX, prevIndex)
            }
        }
        AppWidget().update(context, glanceId)
    }
}

/**
 * An action that is triggered when the user clicks the send notification button.
 *
 * This action is responsible for updating the app widget state and updating the app widget.
 *
 * @see [AppWidget]
 */
internal class SendNotificationAction : ActionCallback {

    /**
     * Handles the action when the user clicks the send notification button.
     *
     * This function is called when the user clicks the send notification button and is responsible for updating the app widget state and updating the app widget.
     *
     * @param context [Context] The context of the app widget.
     * @param glanceId [GlanceId] The id of the app widget.
     * @param parameters [ActionParameters] The parameters of the action.
     *
     * @see [AppWidget]
     */
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val notificationId = parameters[NotificationId.key] ?: return
        val notificationTitle = parameters[NotificationTitle.key] ?: return
        val notificationMessage = parameters[NotificationMessage.key] ?: return

        val notification = Notification(
                id = UUID.fromString(notificationId),
                title = notificationTitle,
                message = notificationMessage
        )

        Log.d(this@SendNotificationAction::class.simpleName, "Sending notification: $notification")

        updateAppWidgetState(context = context, glanceId = glanceId) { prefs ->
            val permissionGranted = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> when {
                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> true
                    else                                                                                                     -> false
                }
                // pre-Android 13: permission is always granted
                else                                                  -> true
            }

            when {
                permissionGranted -> {
                    Log.d(this@SendNotificationAction::class.simpleName, "Notification permission granted")
                    Log.d(this@SendNotificationAction::class.simpleName, "Sending notification: $notification")
                    ONotificationManager.sendNotification(context, notification)
                }

                else              -> {
                    Log.d(this@SendNotificationAction::class.simpleName, "Notification permission not granted")
                    Log.d(this@SendNotificationAction::class.simpleName, "Requesting notification permission")
                    Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        context.startActivity(this)
                    }
                }
            }
        }
    }
}

/**
 * An action that is triggered when the user clicks the random notification button.
 *
 * This action is responsible for updating the app widget state and updating the app widget.
 *
 * @see [AppWidget]
 */
internal class RandomNotificationAction : ActionCallback {

    /**
     * Handles the action when the user clicks the random notification button.
     *
     * This function is called when the user clicks the random notification button and is responsible for updating the app widget state and updating the app widget.
     *
     * @param context [Context] The context of the app widget.
     * @param glanceId [GlanceId] The id of the app widget.
     * @param parameters [ActionParameters] The parameters of the action.
     *
     * @see [AppWidget]
     */
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val notifications = DataStoreInstance.getNotifications(context).first()
            val randomIndex = notifications.indexOf(notifications.random())
            prefs.toMutablePreferences().apply {
                set(KEY_INDEX, randomIndex)
            }
        }
        AppWidget().update(context, glanceId)
    }
}
