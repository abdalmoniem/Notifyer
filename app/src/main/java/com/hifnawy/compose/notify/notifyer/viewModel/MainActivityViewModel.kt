package com.hifnawy.compose.notify.notifyer.viewModel

import android.app.Application
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.hifnawy.compose.notify.notifyer.dataStore.DataStoreInstance
import com.hifnawy.compose.notify.notifyer.model.PersistentNotificationList

/**
 * A view model that is responsible for managing the state of the main activity.
 * The view model is responsible for storing the list of notifications and the state of the lazy grid.
 * The view model is also responsible for saving and restoring the state of the lazy grid.
 *
 * @param application [Application] The application that is used to access the data store.
 * @param savedStateHandle [SavedStateHandle] The saved state handle that is used to save and restore the state of the lazy grid.
 */
@OptIn(SavedStateHandleSaveableApi::class)
class MainActivityViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    /**
     * A persistent list of notifications that is stored in the data store.
     * The list of notifications is observable and can be observed by the UI.
     * The list of notifications is also saved and restored using the saved state handle.
     */
    val notifications = PersistentNotificationList(
            context = application.applicationContext,
            coroutineScope = viewModelScope,
            notificationsFlow = DataStoreInstance.getNotifications(application.applicationContext)
    )

    /**
     * A lazy grid state that stores the state of the lazy grid.
     * The state of the lazy grid is observable and can be observed by the UI.
     * The state of the lazy grid is also saved and restored using the saved state handle.
     */
    val lazyListState by savedStateHandle.saveable(stateSaver = LazyGridState.Saver) { mutableStateOf(LazyGridState()) }
}