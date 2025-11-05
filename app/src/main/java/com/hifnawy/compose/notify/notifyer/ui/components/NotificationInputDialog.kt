package com.hifnawy.compose.notify.notifyer.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.hifnawy.compose.notify.notifyer.model.Notification
import com.hifnawy.compose.notify.notifyer.ui.theme.NotifyerTheme
import java.util.UUID

/**
 * A composable function that displays a dialog to add or update a notification.
 *
 * @param title [String] The title of the dialog.
 * @param notification [Notification] The notification to be updated, or null if it is a new notification.
 * @param onConfirm [(notification: Notification, isUpdate: Boolean) -> Unit][onConfirm] Called when the confirm button is clicked. The notification and a boolean indicating whether the notification is being updated or not are passed as parameters.
 * @param onDismissRequest [() -> Unit][onDismissRequest] Called when the dismiss button is clicked.
 */
@Composable
fun NotificationInputDialog(
        title: String,
        notification: Notification? = null,
        onConfirm: (notification: Notification, isUpdate: Boolean) -> Unit = { notification, isUpdate -> },
        onDismissRequest: () -> Unit = {}
) {
    var notificationTitle by remember { mutableStateOf(notification?.title ?: "") }
    var notificationMessage by remember { mutableStateOf(notification?.message ?: "") }
    var notificationId by remember { mutableStateOf(notification?.id ?: UUID.randomUUID()) }

    AlertDialog(
            properties = DialogProperties(dismissOnClickOutside = false),
            onDismissRequest = onDismissRequest,
            title = { Text(title) },
            text = {
                val focusRequesterTitle = remember { FocusRequester() }
                val focusRequesterMessage = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current
                val keyboardController = LocalSoftwareKeyboardController.current

                Column {
                    Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            fontSize = 15.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Light,
                            text = notificationId.toString().uppercase()
                    )
                    TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .focusRequester(focusRequesterTitle),
                            shape = RoundedCornerShape(10.dp),
                            colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            value = notificationTitle,
                            onValueChange = { notificationTitle = it },
                            placeholder = { Text("Notification Title") },
                            label = { Text("Notification Title") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusRequesterMessage.requestFocus() })

                    )
                    TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .focusRequester(focusRequesterMessage),
                            shape = RoundedCornerShape(10.dp),
                            colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            value = notificationMessage,
                            onValueChange = { notificationMessage = it },
                            placeholder = { Text("Notification Message") },
                            label = { Text("Notification Message") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            })
                    )
                }

                LaunchedEffect(Unit) {
                    focusRequesterTitle.requestFocus()
                }
            },
            confirmButton = {
                TextButton(onClick = { onConfirm(Notification(notification?.id ?: UUID.randomUUID(), notificationTitle, notificationMessage), notification != null) }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel")
                }
            }
    )
}

/**
 * A preview of the notification input dialog composable function.
 * The notification input dialog is displayed with the title "Add new Notification" and
 * two text fields for the title and message of the notification.
 * The notification input dialog is padded by the inner padding of the scaffold.
 * The preview is displayed with the system UI and background.
 */
@Composable
@Preview(
        device = Devices.PHONE,
        name = "Notification Input Dialog Preview",
        showSystemUi = true,
        showBackground = true,
        uiMode = UI_MODE_NIGHT_YES,
        wallpaper = Wallpapers.YELLOW_DOMINATED_EXAMPLE
)
private fun NotificationListTabletPreview() {
    NotifyerTheme {
        Scaffold { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NotificationInputDialog(
                        title = "Add new Notification",
                        onConfirm = { notification, isUpdate -> },
                        onDismissRequest = { }
                )
            }
        }
    }
}