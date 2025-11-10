package com.hifnawy.compose.notify.notifyer.ui.components

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
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
import com.hifnawy.compose.notify.notifyer.R
import com.hifnawy.compose.notify.notifyer.dataStore.DataStoreInstance
import com.hifnawy.compose.notify.notifyer.model.Notification
import com.hifnawy.compose.notify.notifyer.ui.components.AppWidget.Companion.KEY_INDEX
import com.hifnawy.compose.notify.notifyer.ui.theme.WidgetTheme
import kotlinx.coroutines.flow.first
import java.util.UUID
import kotlin.random.Random

class AppWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = AppWidget()
}

class AppWidget : GlanceAppWidget() {

    internal companion object {

        val KEY_INDEX = intPreferencesKey("current_notification_index")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val notifications by DataStoreInstance.getNotifications(context).collectAsState(initial = emptyList())

            WidgetTheme(context) {
                val prefs = currentState<Preferences>()
                val currentIndex = prefs[KEY_INDEX] ?: 0
                Content(currentIndex, notifications)
            }
        }
    }

    @Composable
    private fun Content(currentIndex: Int = 0, notifications: List<Notification>) {
        val isEmpty = notifications.isEmpty()
        val notification = notifications.getOrNull(currentIndex)

        if (isEmpty || notification == null) {
            Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .clickable(actionStartActivity<MainActivity>())
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
                        .clickable(actionStartActivity<MainActivity>())
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
                            onClick = actionRunCallback<SendNotificationAction>()
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

internal class NextNotificationAction : ActionCallback {

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

internal class PreviousNotificationAction : ActionCallback {

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

internal class SendNotificationAction : ActionCallback {

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        updateAppWidgetState(context = context, glanceId = glanceId) { prefs ->
            val notifications = DataStoreInstance.getNotifications(context).first()
            val index = prefs[KEY_INDEX] ?: 0
            val notification = notifications.getOrNull(index) ?: return@updateAppWidgetState // Return early if no notification found

            sendNotification(context, notificationManager, notification)
        }
    }

    private fun sendNotification(
            context: Context,
            notificationManager: NotificationManager,
            notification: Notification
    ) {
        val channelId = "NOTIFICATIONS_CHANNEL_ID"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notifications_24px)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(Random.nextInt(), builder.build())
    }
}

internal class RandomNotificationAction : ActionCallback {

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
