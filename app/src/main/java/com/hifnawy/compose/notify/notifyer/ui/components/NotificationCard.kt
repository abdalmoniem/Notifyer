package com.hifnawy.compose.notify.notifyer.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hifnawy.compose.notify.notifyer.R
import com.hifnawy.compose.notify.notifyer.model.Notification
import com.hifnawy.compose.notify.notifyer.ui.theme.NotifyerTheme
import java.util.UUID

/**
 * A composable function that displays a notification card.
 * The notification card is elevated and has a rounded corner shape.
 * The card contains a title and a message, as well as a button to send the notification.
 * The button is located at the end of the row and has a weight of 1f, which means it takes up all the available space.
 * The title and message are displayed in a column, with the title being bold and the message being normal.
 * The title and message are padded by 10.dp.
 * The button text is "Send" and is displayed with a font size of 20.sp.
 * The function takes four parameters: the title of the notification, the message of the notification,
 * a modifier to apply to the notification card, and a function to call when the button is clicked.
 *
 * @property id [UUID] The unique identifier of the notification.
 * @param title [String] The title of the notification.
 * @param message [String] The message of the notification.
 * @param modifier [Modifier] The modifier to apply to the notification card.
 * @param onCardClick [()->Unit][onCardClick] The function to call when the notification card is clicked.
 * @param onButtonClick [()->Unit][onButtonClick] The function to call when the button is clicked.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun NotificationCard(
        id: UUID,
        title: String,
        message: String,
        modifier: Modifier = Modifier,
        onCardClick: (notification: Notification) -> Unit = {},
        onButtonClick: (notification: Notification) -> Unit = {}
) {
    ElevatedCard(
            modifier = modifier.fillMaxWidth(),
            onClick = { onCardClick(Notification(id = id, title = title, message = message)) },
            elevation = CardDefaults.elevatedCardElevation(20.dp),
            shape = RoundedCornerShape(20.dp),
    ) {
        Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                        text = id.toString().uppercase(),
                        fontSize = 15.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Light
                )

                Text(
                        text = title,
                        fontSize = 20.sp,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold
                )

                Text(
                        text = message,
                        fontSize = 15.sp,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Normal
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { onButtonClick(Notification(id = id, title = title, message = message)) }) {
                Icon(
                        painter = painterResource(id = R.drawable.notifications_24px),
                        contentDescription = "Send Notification"
                )
            }
        }
    }
}

/**
 * A preview of the notification card composable function.
 * The notification card is displayed with a title of "Notification Title" and a message of "Notification Message".
 * The notification card is padded by the inner padding of the scaffold.
 * The preview is displayed with the system UI and background.
 */
@Composable
@Preview(
        name = "Notification Preview",
        showSystemUi = true,
        showBackground = true,
        uiMode = UI_MODE_NIGHT_YES,
        wallpaper = Wallpapers.YELLOW_DOMINATED_EXAMPLE
)
private fun NotificationPreview() {
    NotifyerTheme {
        Scaffold { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NotificationCard(
                        id = UUID.randomUUID(),
                        title = "Notification Title",
                        message = "Notification Message",
                        modifier = Modifier
                )
            }
        }
    }
}