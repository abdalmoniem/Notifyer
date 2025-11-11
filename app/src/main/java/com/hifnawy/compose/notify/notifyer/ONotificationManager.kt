package com.hifnawy.compose.notify.notifyer

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.hifnawy.compose.notify.notifyer.model.Notification
import kotlin.random.Random

object ONotificationManager {

    private val notificationIdsSent = mutableListOf<Int>()

    /**
     * Checks if the app has permission to post notifications.
     * If the app has permission, does nothing.
     * If the app does not have permission, requests permission.
     *
     * @param context [Context] The context in which the permission is checked.
     * @param launcher [ActivityResultLauncher<String>][ActivityResultLauncher] The launcher to be used to request permission.
     * @param onShowRationale [() -> Unit][onShowRationale] The callback function to be used when the rationale dialog should be shown.
     */
    fun checkNotificationPermission(
            activity: Activity,
            launcher: ActivityResultLauncher<String>,
            onShowRationale: () -> Unit = {}
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(this@ONotificationManager::class.simpleName, "Notification permission granted")
                    // proceed to send notification
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.POST_NOTIFICATIONS
                )                                            -> {
                    Log.d(this@ONotificationManager::class.simpleName, "Show rationale")
                    onShowRationale()
                }

                else                                         -> {
                    Log.d(this@ONotificationManager::class.simpleName, "Requesting notification permission")
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // pre-Android 13: permission is always granted
        }
    }

    /**
     * Sends a notification with the given title and message.
     *
     * @param context [Context] The Context in which the notification is sent.
     * @param notification [Notification] The Notification to be sent.
     * @param onNotificationSent [(notificationId: Int) -> Unit = {}][onNotificationSent] A callback function to be called when the notification is sent.
     */
    fun sendNotification(
            context: Context,
            notification: Notification,
            onNotificationSent: (notificationId: Int) -> Unit = {}
    ) {
        val notificationManager = context.getSystemService(NotificationManager::class.java) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(context, "NOTIFICATIONS_CHANNEL_ID").apply {
            val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    // TODO: add the notification model id to the intent so that it can be used to scroll to the notification in the list
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setContentIntent(pendingIntent)
            setSubText(notification.id.toString().uppercase())
            setContentTitle(notification.title)
            setContentText(notification.message)
            setAllowSystemGeneratedContextualActions(true)
            setGroup(context.getString(R.string.app_name))
            setPriority(NotificationManager.IMPORTANCE_MAX)
            setCategory(NotificationCompat.CATEGORY_REMINDER)
            setContentInfo(context.getString(R.string.app_name))
            setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
            setSmallIcon(R.drawable.notifications_24px)
            setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
        }

        val summaryNotificationBuilder: NotificationCompat.Builder? = when {
            notificationIdsSent.isNotEmpty() -> NotificationCompat.Builder(context, context.getString(R.string.app_name))
                .setSubText("${notificationIdsSent.size} New Notifications")
                .setContentText("${notificationIdsSent.size} New Notifications")
                .setContentTitle("${notificationIdsSent.size} New Notifications")
                .setGroup(context.getString(R.string.app_name))
                .setPriority(NotificationManager.IMPORTANCE_MAX)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentInfo(context.getString(R.string.app_name))
                .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                .setSmallIcon(R.drawable.notifications_24px)
                .setGroupSummary(true)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)

            else                             -> null
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("NOTIFICATIONS_CHANNEL_ID", "Notifications", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications Channel"
            }

            notificationManager.createNotificationChannel(channel)
            notificationBuilder.setChannelId(channel.id)
            summaryNotificationBuilder?.setChannelId(channel.id)
        }

        val notificationId = Random.nextInt()

        notificationManager.notify(notificationId, notificationBuilder.build())
        summaryNotificationBuilder?.let { notificationManager.notify(0, it.build()) }

        onNotificationSent(notificationId)
        notificationIdsSent.add(notificationId)
    }

    /**
     * Cancel all previously shown notifications.
     *
     * @param context [Context] The Context in which the notifications are canceled.
     */
    fun cancelNotifications(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.cancelAll()
        notificationIdsSent.clear()
    }
}