package com.hifnawy.compose.notify.notifyer.model

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.hifnawy.compose.notify.notifyer.dataStore.DataStoreInstance
import com.hifnawy.compose.notify.notifyer.ui.components.AppWidget
import com.hifnawy.compose.notify.notifyer.ui.components.AppWidget.Companion.KEY_INDEX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Collections.sort
import java.util.function.UnaryOperator

class PersistentNotificationList(
        private val context: Context,
        private val coroutineScope: CoroutineScope,
        private val notificationsFlow: Flow<List<Notification>>
) : MutableList<Notification> by mutableStateListOf() {

    private val items = mutableStateListOf<Notification>()

    init {
        // Load and observe DataStore updates
        coroutineScope.launch {
            notificationsFlow.collectLatest { list ->
                items.clear()
                items.addAll(list)
            }
        }
    }

    val asSnapshotStateList: SnapshotStateList<Notification>
        get() = items

    private fun persist() {
        coroutineScope.launch { DataStoreInstance.saveNotifications(context, items.toList()) }
    }

    private suspend fun updateGlanceWidgets(resetIndex: Boolean = false) {
        val manager = GlanceAppWidgetManager(context)

        // Get all active GlanceIds for your specific widget class
        val glanceIds = manager.getGlanceIds(AppWidget::class.java)

        // Iterate over all instances and call update()
        glanceIds.forEach { glanceId ->
            if (resetIndex) {
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        set(KEY_INDEX, 0)
                    }
                }
            }

            AppWidget().update(context, glanceId)
        }
    }

    override val size: Int
        get() = items.size

    override fun get(index: Int): Notification =
            items[index]

    override fun set(index: Int, element: Notification): Notification {
        val result = items.set(index, element)
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        return result
    }

    override fun contains(element: Notification): Boolean = items.contains(element)

    override fun containsAll(elements: Collection<Notification>): Boolean = items.containsAll(elements)

    override fun isEmpty(): Boolean = items.isEmpty()

    override fun indexOf(element: Notification): Int = items.indexOf(element)

    override fun lastIndexOf(element: Notification): Int = items.lastIndexOf(element)

    override fun iterator(): MutableIterator<Notification> = object : MutableIterator<Notification> {
        private val iterator = items.iterator()
        override fun hasNext() = iterator.hasNext()
        override fun next() = iterator.next()
        override fun remove() {
            iterator.remove()
            persist()
        }
    }

    override fun add(element: Notification): Boolean {
        val result = items.add(element)
        if (result) {
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = false) }
        }
        return result
    }

    override fun add(index: Int, element: Notification) {
        items.add(index, element)
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = false) }
    }

    override fun addAll(index: Int, elements: Collection<Notification>): Boolean {
        val result = items.addAll(index, elements)
        if (result) {
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = false) }
        }
        return result
    }

    override fun addAll(elements: Collection<Notification>): Boolean {
        val result = items.addAll(elements)
        if (result) {
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = false) }
        }
        return result
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun addFirst(element: Notification) {
        items.addFirst(element)
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = false) }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun addLast(element: Notification) {
        items.addLast(element)
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = false) }
    }

    override fun remove(element: Notification): Boolean {
        val result = items.remove(element)
        if (result) {
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        }
        return result
    }

    override fun removeAt(index: Int): Notification {
        val result = items.removeAt(index)
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        return result
    }

    override fun removeAll(elements: Collection<Notification>): Boolean {
        val result = items.removeAll(elements)
        if (result) {
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        }
        return result
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun removeFirst(): Notification {
        val notification = items.removeFirst()
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        return notification
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun removeLast(): Notification {
        val notification = items.removeLast()
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        return notification
    }

    override fun clear() {
        if (items.isNotEmpty()) {
            items.clear()
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        }
    }

    override fun replaceAll(operator: UnaryOperator<Notification>) {
        items.replaceAll(operator)
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
    }

    fun replaceAll(newList: List<Notification>) {
        Snapshot.withMutableSnapshot {
            items.clear()
            items.addAll(newList)
        }
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
    }

    fun shuffle() {
        Snapshot.withMutableSnapshot {
            items.shuffle()
        }
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
    }

    override fun retainAll(elements: Collection<Notification>): Boolean {
        val result = items.retainAll(elements)
        if (result) {
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        }
        return result
    }

    override fun listIterator(): MutableListIterator<Notification> = listIterator(0)

    override fun listIterator(index: Int): MutableListIterator<Notification> = object : MutableListIterator<Notification> {
        private val iterator = items.listIterator(index)

        override fun hasNext() = iterator.hasNext()
        override fun next() = iterator.next()
        override fun hasPrevious() = iterator.hasPrevious()
        override fun previous() = iterator.previous()
        override fun nextIndex() = iterator.nextIndex()
        override fun previousIndex() = iterator.previousIndex()

        override fun add(element: Notification) {
            iterator.add(element)
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = false) }
        }

        override fun remove() {
            iterator.remove()
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        }

        override fun set(element: Notification) {
            iterator.set(element)
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Notification> = items.subList(fromIndex, toIndex)

    override fun sort(comparator: Comparator<in Notification>?) {
        sort(items, comparator)
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
    }
}