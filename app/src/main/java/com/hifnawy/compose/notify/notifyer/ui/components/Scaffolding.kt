package com.hifnawy.compose.notify.notifyer.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.hifnawy.compose.notify.notifyer.BuildConfig
import com.hifnawy.compose.notify.notifyer.R
import com.hifnawy.compose.notify.notifyer.ui.theme.NotifyerTheme

/**
 * A composable function that displays a scaffolding with a top bar, a floating action button, and a content area.
 * The top bar contains a title and two buttons: one to delete all notifications and one to clear all notifications.
 * The floating action button is used to add new notifications to the list.
 * The content area is where the list of notifications is displayed.
 *
 * @param darkTheme [Boolean] Whether to use dark theme or not.
 * @param dynamicColor [Boolean] Whether to use dynamic color or not.
 * @param floatingActionButton [() -> Unit][floatingActionButton] The composable function to be used as the floating action button.
 * @param onShuffleClick [() -> Unit][onShuffleClick] The callback function to be used as the shuffle action button.
 * @param onDeleteAllClick [() -> Unit][onDeleteAllClick] The callback function to be used as the delete all action button.
 * @param onClearAllClick [() -> Unit][onClearAllClick] The callback function to be used as the clear all action button.
 * @param onClearAndInsertClick [() -> Unit][onClearAndInsertClick] The callback function to be used as the clear and insert action button.
 * @param scaffoldContent [(paddingValues: PaddingValues) -> Unit][scaffoldContent] The composable function to be used as the content area.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun Scaffolding(
        darkTheme: Boolean = isSystemInDarkTheme(),
        dynamicColor: Boolean = true,
        floatingActionButton: @Composable () -> Unit = {},
        onShuffleClick: () -> Unit = {},
        onDeleteAllClick: () -> Unit = {},
        onClearAllClick: () -> Unit = {},
        onClearAndInsertClick: () -> Unit = {},
        scaffoldContent: @Composable (paddingValues: PaddingValues) -> Unit
) {
    NotifyerTheme(
            darkTheme = darkTheme,
            dynamicColor = dynamicColor
    ) {
        Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                            title = { Text("Notification List") },
                            actions = {
                                if (BuildConfig.DEBUG) {
                                    IconButton(onClick = onClearAndInsertClick) {
                                        Icon(
                                                painter = painterResource(id = R.drawable.format_list_bulleted_add_24px),
                                                contentDescription = "Clear and Insert 30 Items"
                                        )
                                    }
                                }
                                IconButton(onClick = onShuffleClick) {
                                    Icon(
                                            painter = painterResource(id = R.drawable.shuffle_24px),
                                            contentDescription = "Shuffle Notifications"
                                    )
                                }
                                IconButton(onDeleteAllClick) {
                                    Icon(
                                            painter = painterResource(id = R.drawable.delete_sweep_24px),
                                            contentDescription = "Delete All Notifications"
                                    )
                                }

                                IconButton(onClick = onClearAllClick) {
                                    Icon(
                                            painter = painterResource(id = R.drawable.clear_all_24px),
                                            contentDescription = "Clear All Notifications"
                                    )
                                }
                            }
                    )
                },
                floatingActionButton = floatingActionButton,
        ) { innerPadding ->
            scaffoldContent(innerPadding)
        }
    }
}

/**
 * A preview of the scaffolding composable function.
 * The scaffolding composable function is displayed with a top bar, a floating action button, and a content area.
 * The top bar contains a title and two buttons: one to delete all notifications and one to clear all notifications.
 * The floating action button is used to add new notifications to the list.
 * The content area is where the list of notifications is displayed.
 * The preview is displayed with the system UI and background.
 */
@Composable
@Preview(
        device = Devices.PHONE,
        name = "Scaffolding Preview",
        showSystemUi = true,
        showBackground = true,
        uiMode = UI_MODE_NIGHT_YES,
        wallpaper = Wallpapers.YELLOW_DOMINATED_EXAMPLE
)
private fun ScaffoldingPreview() {
    Scaffolding(
            darkTheme = true,
            dynamicColor = true,
            floatingActionButton = {
                FloatingActionButton(onClick = { }) {
                    Icon(
                            painter = painterResource(id = R.drawable.add_24px),
                            contentDescription = "Add new Notification"
                    )
                }
            }
    ) { innerPadding ->
        Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Hello, World!")
            Button(onClick = {}) {
                Text("Click Me")
            }
        }
    }
}