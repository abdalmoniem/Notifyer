package com.hifnawy.compose.notify.notifyer

import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hifnawy.compose.notify.notifyer.model.Notification
import com.hifnawy.compose.notify.notifyer.ui.components.AppWidget
import com.hifnawy.compose.notify.notifyer.ui.components.NotificationInputDialog
import com.hifnawy.compose.notify.notifyer.ui.components.NotificationList
import com.hifnawy.compose.notify.notifyer.ui.components.Scaffolding
import com.hifnawy.compose.notify.notifyer.viewModel.MainActivityViewModel
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {

    /**
     * Called when the activity is starting. Responsible for setting up the
     * UI that is used to display a list of notifications.
     *
     * @param savedInstanceState [Bundle] If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle).
     * Note: Otherwise it is null.
     */

    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateGlanceWidgets()

        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val viewModel = viewModel<MainActivityViewModel>()
            var updateNotification by remember { mutableStateOf<Notification?>(null) }
            var isEditDialogVisible by remember { mutableStateOf(false) }
            var isDeleteAllDialogVisible by remember { mutableStateOf(false) }
            var shouldShowRationale by remember { mutableStateOf(false) }

            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) shouldShowRationale = false
            }

            LaunchedEffect(Unit) {
                ONotificationManager.checkNotificationPermission(this@MainActivity, launcher) { shouldShowRationale = true }
            }

            MainScreen(
                    notifications = viewModel.notifications.asSnapshotStateList,
                    listState = viewModel.lazyListState,
                    updateNotification = updateNotification,
                    isEditDialogVisible = isEditDialogVisible,
                    isDeleteAllDialogVisible = isDeleteAllDialogVisible,
                    shouldShowRationale = shouldShowRationale,
                    onPermissionRationaleConfirm = {
                        when {
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                context.startActivity(this)
                            }

                            else                                           -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                                context.startActivity(this)
                            }
                        }

                        shouldShowRationale = false
                    },
                    onPermissionRationaleDismiss = { shouldShowRationale = false },
                    onAddClick = { isEditDialogVisible = true },
                    onShuffleClick = viewModel.notifications::shuffle,
                    onDeleteAllClick = { isDeleteAllDialogVisible = true },
                    onDeleteAllConfirm = {
                        viewModel.notifications.clear()
                        isDeleteAllDialogVisible = false

                    },
                    onDeleteAllDismiss = { isDeleteAllDialogVisible = false },
                    onClearAllClick = { ONotificationManager.cancelNotifications(context) },
                    onClearAndInsertClick = {
                        val newList = (1..30).map { index ->
                            Notification(
                                    id = UUID.randomUUID(),
                                    title = "Notification #${index.toString().padStart(2, '0')} Title",
                                    message = "Notification #${index.toString().padStart(2, '0')} Message"
                            )
                        }
                        viewModel.notifications.replaceAll(newList)
                    },
                    onItemCardClick = { notification ->
                        updateNotification = notification
                        isEditDialogVisible = true
                    },
                    onItemButtonClick = { notification ->
                        ONotificationManager.checkNotificationPermission(
                                activity = this,
                                launcher = launcher,
                                onShowRationale = { shouldShowRationale = true }
                        )

                        ONotificationManager.sendNotification(
                                context = context,
                                notification = notification,
                        ) { notificationId ->
                            Log.d(this@MainActivity::class.simpleName, "Notification sent with ID: $notificationId")
                        }
                    },
                    onEditConfirm = { notification, isUpdate ->
                        notificationEditConfirmed(notification, isUpdate)

                        isEditDialogVisible = false
                        updateNotification = null
                    },
                    onEditDismiss = {
                        isEditDialogVisible = false
                        updateNotification = null
                    }
            )
        }
    }

    /**
     * Update the glance widgets with the current list of notifications.
     *
     * @see [GlanceAppWidgetManager]
     * @see [updateAppWidgetState]
     */
    private fun updateGlanceWidgets() {
        // Use the lifecycleScope to run the suspend function
        lifecycleScope.launch {
            val manager = GlanceAppWidgetManager(this@MainActivity)

            // Get all active GlanceIds for your specific widget class
            val glanceIds = manager.getGlanceIds(AppWidget::class.java)

            // Iterate over all instances and call update()
            glanceIds.forEach { glanceId ->
                AppWidget().update(this@MainActivity, glanceId)
            }
        }
    }

    /**
     * A composable function that displays the main screen of the app.
     * The main screen contains a list of notifications, a floating action button to add new notifications,
     * and buttons to shuffle and delete all notifications.
     * The main screen also displays a dialog to add or update a notification when the floating action button is clicked,
     * and a dialog to confirm deleting all notifications when the delete all button is clicked.
     *
     * @param notifications [List<Notification>][List] The list of notifications to be displayed.
     * @param listState [LazyGridState] The state of the list of notifications.
     * @param updateNotification [Notification] The notification to be updated, or null if it is a new notification.
     * @param isEditDialogVisible [Boolean] Whether the dialog to add or update a notification is visible or not.
     * @param isDeleteAllDialogVisible [Boolean] Whether the dialog to confirm deleting all notifications is visible or not.
     * @param shouldShowRationale [Boolean] Whether the rationale dialog should be shown or not.
     * @param onPermissionRationaleConfirm [() -> Unit][onPermissionRationaleConfirm] The callback function to be used when the confirm button in the rationale dialog is clicked.
     * @param onPermissionRationaleDismiss [() -> Unit][onPermissionRationaleDismiss] The callback function to be used when the dismiss button in the rationale dialog is clicked.
     * @param onAddClick [() -> Unit][onAddClick] The callback function to be used when the floating action button is clicked.
     * @param onShuffleClick [() -> Unit][onShuffleClick] The callback function to be used when the shuffle button is clicked.
     * @param onClearAllClick [() -> Unit][onClearAllClick] The callback function to be used when the clear all button is clicked.
     * @param onClearAndInsertClick [() -> Unit][onClearAndInsertClick] The callback function to be used when the clear and insert button is clicked.
     * @param onDeleteAllClick [() -> Unit][onDeleteAllClick] The callback function to be used when the delete all button is clicked.
     * @param onDeleteAllConfirm [() -> Unit][onDeleteAllConfirm] The callback function to be used when the confirm button in the delete all dialog is clicked.
     * @param onDeleteAllDismiss [() -> Unit][onDeleteAllDismiss] The callback function to be used when the dismiss button in the delete all dialog is clicked.
     * @param onItemCardClick [(notification:  Notification) -> Unit][onItemCardClick] The callback function to be used when an item in the list of notifications is clicked.
     * @param onItemButtonClick [(notification:  Notification) -> Unit][onItemButtonClick] The callback function to be used when the button in an item in the list of notifications
     *                          is clicked.
     * @param onEditConfirm [(notification:  Notification, isUpdate: Boolean) -> Unit][onEditConfirm] The callback function to be used when the confirm button in the dialog is
     *                      clicked.
     * @param onEditDismiss [() -> Unit][onEditDismiss] The callback function to be used when the dismiss button in the edit dialog is clicked.
     */
    @Composable
    private fun MainScreen(
            notifications: SnapshotStateList<Notification> = mutableStateListOf(),
            listState: LazyGridState = LazyGridState(),
            updateNotification: Notification? = null,
            isEditDialogVisible: Boolean = false,
            isDeleteAllDialogVisible: Boolean = false,
            shouldShowRationale: Boolean = false,
            onPermissionRationaleConfirm: () -> Unit = {},
            onPermissionRationaleDismiss: () -> Unit = {},
            onAddClick: () -> Unit = {},
            onShuffleClick: () -> Unit = {},
            onClearAllClick: () -> Unit = {},
            onClearAndInsertClick: () -> Unit = {},
            onDeleteAllClick: () -> Unit = {},
            onDeleteAllConfirm: () -> Unit = {},
            onDeleteAllDismiss: () -> Unit = {},
            onItemCardClick: (notification: Notification) -> Unit = {},
            onItemButtonClick: (notification: Notification) -> Unit = {},
            onEditConfirm: (notification: Notification, isUpdate: Boolean) -> Unit = { _, _ -> },
            onEditDismiss: () -> Unit = {},
    ) {
        Scaffolding(
                floatingActionButton = {
                    FloatingActionButton(onClick = onAddClick) {
                        Icon(
                                painter = painterResource(id = R.drawable.add_24px),
                                contentDescription = "Add new Notification"
                        )
                    }
                },
                onShuffleClick = onShuffleClick,
                onDeleteAllClick = onDeleteAllClick,
                onClearAllClick = onClearAllClick,
                onClearAndInsertClick = onClearAndInsertClick
        ) { innerPadding ->
            NotificationList(
                    listState = listState,
                    notifications = notifications,
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(vertical = 10.dp),
                    onItemCardClick = onItemCardClick,
                    onItemButtonClick = onItemButtonClick
            )

            if (shouldShowRationale) {
                PermissionRationaleDialog(onConfirm = onPermissionRationaleConfirm, onDismissRequest = onPermissionRationaleDismiss)
            }

            if (isDeleteAllDialogVisible) {
                AlertDialog(
                        title = { Text("Delete All Notifications") },
                        text = { Text("Are you sure you want to delete all notifications?") },
                        onDismissRequest = onDeleteAllDismiss,
                        confirmButton = {
                            Button(onClick = onDeleteAllConfirm) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            Button(onClick = onDeleteAllDismiss) {
                                Text("Cancel")
                            }
                        }
                )
            }

            if (isEditDialogVisible) {
                NotificationInputDialog(
                        title = if (updateNotification == null) "Add Notification" else "Update Notification",
                        notification = updateNotification,
                        onConfirm = onEditConfirm,
                        onDismissRequest = onEditDismiss
                )
            }
        }
    }

    @Composable
    /**
     * A composable function that displays a dialog to show the rationale for requesting notification permission.
     *
     * @param onConfirm [() -> Unit][onConfirm] The callback function to be used when the confirm button is clicked.
     * @param onDismissRequest [() -> Unit][onDismissRequest] The callback function to be used when the dismiss button is clicked.
     */
    private fun PermissionRationaleDialog(onConfirm: () -> Unit = {}, onDismissRequest: () -> Unit = {}) {
        AlertDialog(
                title = { Text("Notification permission needed") },
                text = { Text("This app needs notification permission to alert you about updates.") },
                confirmButton = {
                    Button(onClick = onConfirm) {
                        Text("Open settings")
                    }
                },
                dismissButton = {
                    Button(onClick = onDismissRequest) {
                        Text("Cancel")
                    }
                },
                onDismissRequest = onDismissRequest
        )
    }

    /**
     * Updates or adds a new notification to the list of notifications.
     *
     * @param notification [Notification] The notification to be updated or added.
     * @param isUpdate [Boolean] Whether the notification is being updated or added.
     */
    private fun notificationEditConfirmed(
            notification: Notification,
            isUpdate: Boolean
    ) {
        val viewModel by viewModels<MainActivityViewModel>()
        when {
            isUpdate -> viewModel.apply {
                val updatedIndex = notifications.indexOfFirst { it.id == notification.id }
                if (updatedIndex != -1) notifications[updatedIndex] = notification
            }

            else     -> viewModel.apply {
                notifications.add(notification)
                viewModelScope.launch { viewModel.lazyListState.scrollToItem(viewModel.notifications.size - 1) }
            }
        }
    }

    /**
     * A preview of the main screen of the app.
     * The main screen contains a list of notifications, a floating action button to add new notifications,
     * and buttons to shuffle and delete all notifications.
     * The main screen also displays a dialog to add or update a notification when the floating action button is clicked,
     * and a dialog to confirm deleting all notifications when the delete all button is clicked.
     *
     * This preview is displayed with the system UI and background, and is shown on a phone device.
     */
    @Composable
    @Preview(
            device = Devices.PHONE,
            name = "Main Screen Preview",
            showSystemUi = true,
            showBackground = true,
            uiMode = UI_MODE_NIGHT_YES,
            wallpaper = Wallpapers.YELLOW_DOMINATED_EXAMPLE
    )
    private fun MainScreenPreview() {
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

        notifications.shuffle()

        MainScreen(notifications = notifications)
    }
}