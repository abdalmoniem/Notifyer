package com.hifnawy.compose.notify.notifyer.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.hifnawy.compose.notify.notifyer.model.Notification
import com.hifnawy.compose.notify.notifyer.ui.theme.NotifyerTheme
import java.util.UUID

/**
 * A composable function that displays a list of notifications.
 * Each notification is displayed with a title and message, and when clicked on,
 * a toast message is displayed with the title of the notification.
 *
 * @param listState [LazyGridState] The state of the list.
 * @param notifications [List] The [Notification] list of notifications to display.
 * @param modifier [Modifier] The modifier to apply to the composable function.
 * @param onItemCardClick [() -> Unit][onItemCardClick] The callback function to be used as the item click action button.
 * @param onItemButtonClick: [(notification: Notification) -> Unit = {}][onItemButtonClick] The callback function to be used as the item button click action button.
 */
@Composable
fun NotificationList(
        listState: LazyGridState = rememberLazyGridState(),
        notifications: List<Notification>,
        modifier: Modifier,
        onItemCardClick: (notification: Notification) -> Unit = {},
        onItemButtonClick: (notification: Notification) -> Unit = {}
) {
    val context = LocalContext.current

    LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = modifier.padding(start = 10.dp, end = 10.dp)
    ) {
        items(notifications) { notification ->
            Notification(
                    id = notification.id,
                    title = notification.title,
                    message = notification.message,
                    modifier = Modifier.animateItem(),
                    onCardClick = onItemCardClick,
                    onButtonClick = onItemButtonClick
            )
        }
    }
}

/**
 * A preview of the notification list composable function.
 * The notification list is displayed with 20 notifications, each with a title and message.
 * The notification list is padded by the inner padding of the scaffold.
 * The preview is displayed with the system UI and background.
 */
@Composable
@Preview(
        device = Devices.PHONE,
        name = "Notification List Preview",
        showSystemUi = true,
        showBackground = true,
        uiMode = UI_MODE_NIGHT_YES,
        wallpaper = Wallpapers.YELLOW_DOMINATED_EXAMPLE
)
private fun NotificationListPreview() {
    NotifyerTheme {
        Scaffold { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                val notifications = mutableListOf<Notification>()
                for (i in 0 until 20) {
                    notifications.add(
                            Notification(
                                    id = UUID.randomUUID(),
                                    title = "Notification Title #${i}",
                                    message = "Notification Message #${i}"
                            )
                    )
                }

                notifications.shuffle()

                NotificationList(notifications = notifications, modifier = Modifier)
            }
        }
    }
}