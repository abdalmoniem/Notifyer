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

/**
 * A list of notifications that are persisted to storage and
 * observed for updates. This list is backed by a
 * [SnapshotStateList] and is updated whenever the underlying
 * data is changed.
 *
 * @param context [Context] The context used to access the data store.
 * @param coroutineScope [CoroutineScope] The coroutine scope used to launch background
 * operations.
 * @param notificationsFlow [Flow<List<Notification>>][Flow] The flow of notifications that is observed
 * for updates.
 *
 * @see [MutableList]
 * @see [SnapshotStateList]
 */
class PersistentNotificationList(
        private val context: Context,
        private val coroutineScope: CoroutineScope,
        private val notificationsFlow: Flow<List<Notification>>
) : MutableList<Notification> by mutableStateListOf() {

    /**
     * The list of notifications that are persisted to storage and
     * observed for updates. This list is backed by a
     * [SnapshotStateList] and is updated whenever the underlying
     * data is changed.
     *
     * @see [SnapshotStateList]
     */
    private val items = mutableStateListOf<Notification>()

    /**
     * Initializes the list of notifications by loading and observing
     * DataStore updates.
     */
    init {
        // Load and observe DataStore updates
        coroutineScope.launch {
            notificationsFlow.collectLatest { list ->
                items.clear()
                items.addAll(list)
            }
        }
    }

    /**
     * Returns the list of notifications as a [SnapshotStateList].
     *
     * @return [SnapshotStateList<Notification>][SnapshotStateList] The list of notifications as a [SnapshotStateList].
     *
     * @see SnapshotStateList
     */
    val asSnapshotStateList: SnapshotStateList<Notification>
        get() = items

    /**
     * Persist the list of notifications to storage. This method is
     * called after every operation that modifies the list of
     * notifications.
     *
     * @see [DataStoreInstance.saveNotifications]
     */
    private fun persist() {
        coroutineScope.launch { DataStoreInstance.saveNotifications(context, items.toList()) }
    }

    /**
     * Update the glance widgets with the current list of notifications.
     *
     * @param resetIndex [Boolean] Whether to reset the index of the glance widgets.
     *
     * @see [GlanceAppWidgetManager]
     * @see [updateAppWidgetState]
     */
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

    /**
     * Returns the size of the list of notifications.
     *
     * @return [Int] The size of the list of notifications.
     *
     * @see MutableList.size
     */
    override val size: Int
        get() = items.size

    /**
     * Returns the notification at the specified index.
     *
     * @param index [Int] The index of the notification to return.
     * @return [Notification] The notification at the specified index.
     *
     * @see MutableList.get
     */
    override fun get(index: Int): Notification = items[index]

    /**
     * Replaces the element at the specified position in this list with the specified element.
     *
     * @param index [Int] The index of the element to replace.
     * @param element [Notification] The element to be stored at the specified position.
     * @return [Notification] The element previously at the specified position.
     *
     * @see MutableList.set
     */
    override fun set(index: Int, element: Notification): Notification {
        val result = items.set(index, element)
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        return result
    }

    /**
     * Returns `true` if this list contains the specified element.
     *
     * @param element [Notification] Element whose presence in this list is to be tested.
     * @return [Boolean] `true` if this list contains the specified element.
     *
     * @see MutableList.contains
     */
    override fun contains(element: Notification): Boolean = items.contains(element)

    /**
     * Returns `true` if this list contains all of the elements in the specified collection.
     *
     * @param elements [Collection<Notification>][Collection] A collection of elements to test for presence in this list.
     * @return [Boolean] `true` if this list contains all of the elements in the specified collection.
     *
     * @see MutableList.containsAll
     */
    override fun containsAll(elements: Collection<Notification>): Boolean = items.containsAll(elements)

    /**
     * Returns `true` if this list contains no elements.
     *
     * @return [Boolean] `true` if this list contains no elements.
     *
     * @see MutableList.isEmpty
     */
    override fun isEmpty(): Boolean = items.isEmpty()

    /**
     * Returns the index of the first occurrence of the specified element in this list, or -1 if this list does not contain the element.
     *
     * @param element [Notification] Element to search for.
     * @return [Int] The index of the first occurrence of the specified element in this list, or -1 if this list does not contain the element.
     *
     * @see MutableList.indexOf
     */
    override fun indexOf(element: Notification): Int = items.indexOf(element)

    /**
     * Returns the index of the last occurrence of the specified element in this list, or -1 if this list does not contain the element.
     *
     * @param element [Notification] Element to search for.
     * @return [Int] The index of the last occurrence of the specified element in this list, or -1 if this list does not contain the element.
     *
     * @see MutableList.lastIndexOf
     */
    override fun lastIndexOf(element: Notification): Int = items.lastIndexOf(element)

    /**
     * Returns a view of the portion of this list between the specified
     * `fromIndex` (inclusive) and `toIndex` (exclusive).
     * (If `fromIndex` and `toIndex` are equal, the returned list is empty.)
     * The returned list is backed by this list, so changes in the returned list are reflected in this list, and vice versa.
     * The returned list supports all of the optional list operations supported by this list.
     *
     * @param fromIndex [Int] The starting index of the returned view.
     * @param toIndex [Int] The ending index of the returned view.
     * @return [MutableList<Notification>] A view of the portion of this list between the specified
     *         `fromIndex` (inclusive) and `toIndex` (exclusive).
     *
     * @see MutableList.subList
     */
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Notification> = items.subList(fromIndex, toIndex)

    /**
     * Inserts the specified element at the end of this list.
     *
     * If this list already contains the specified element, the call returns false.
     * Otherwise, it returns true.
     *
     * Note that this implementation is not synchronized.
     * If multiple threads access this list concurrently and one of the threads modifies the list structurally,
     * the other threads must be prepared for a ConcurrentModificationException.
     * The iterators returned by this list's iterator and listIterator methods are fail-fast:
     * if the list is structurally modified at any time after the iterator is created, in any way except
     * through the iterator's own remove() or add() methods, the iterator will throw a
     * ConcurrentModificationException. Thus, in the face of concurrent modification, the iterator fails quickly
     * and cleanly, rather than risking arbitrary, non-deterministic behavior at an undetermined time in the future.
     *
     * @param element [Notification] The element to be inserted at the end of this list.
     * @return [Boolean] `true` if the element was added to the list, `false` otherwise.
     *
     * @see MutableList.add
     */
    override fun add(element: Notification): Boolean {
        val result = items.add(element)
        if (result) {
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = false) }
        }
        return result
    }

    /**
     * Inserts the specified element at the specified position in this list.
     *
     * Shifts the element currently at that position (if any) and any subsequent elements to the right.
     * Increases the list size by one.
     *
     * @param index [Int] The index at which the specified element is to be inserted.
     * @param element [Notification] The element to be inserted at the specified position in this list.
     *
     * @see MutableList.add
     */
    override fun add(index: Int, element: Notification) {
        items.add(index, element)
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = false) }
    }

    /**
     * Inserts all of the elements in the specified collection into this list, starting at the specified position.
     *
     * Shifts the element currently at that position (if any) and any subsequent elements to the right.
     * Increases the list size by the number of elements added.
     *
     * @param index [Int] The index at which to insert the first element from the specified collection.
     * @param elements [Collection<Notification>] A collection of elements to be inserted into this list.
     * @return [Boolean] `true` if this list changed as a result of the call.
     *
     * @see MutableList.addAll
     */
    override fun addAll(index: Int, elements: Collection<Notification>): Boolean {
        val result = items.addAll(index, elements)
        if (result) {
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = false) }
        }
        return result
    }

    /**
     * Adds all of the elements in the specified collection to the end of this list.
     *
     * If this list already contains one or more of the specified elements, the call returns false.
     * Otherwise, it returns true.
     *
     * Note that this implementation is not synchronized.
     * If multiple threads access this list concurrently and one of the threads modifies the list structurally,
     * the other threads must be prepared for a ConcurrentModificationException.
     * The iterators returned by this list's iterator and listIterator methods are fail-fast:
     * if the list is structurally modified at any time after the iterator is created, in any way except
     * through the iterator's own remove() or add() methods, the iterator will throw a
     * ConcurrentModificationException. Thus, in the face of concurrent modification, the iterator fails quickly
     * and cleanly, rather than risking arbitrary, non-deterministic behavior at an undetermined time in the future.
     *
     * @param elements [Collection<Notification>] A collection of elements to be added to the end of this list.
     * @return [Boolean] `true` if the elements were added to the list, `false` otherwise.
     *
     * @see MutableList.addAll
     */
    override fun addAll(elements: Collection<Notification>): Boolean {
        val result = items.addAll(elements)
        if (result) {
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = false) }
        }
        return result
    }

    /**
     * Inserts the specified element at the beginning of this list.
     *
     * Shifts the element currently at that position (if any) and any subsequent elements to the right.
     * Increases the list size by one.
     *
     * @param element [Notification] The element to be inserted at the beginning of this list.
     *
     * @throws UnsupportedOperationException if adding elements to the list is not supported.
     *
     * @see MutableList.addFirst
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun addFirst(element: Notification) {
        items.addFirst(element)
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = false) }
    }

    /**
     * Inserts the specified element at the end of this list.
     *
     * Shifts the element currently at that position (if any) and any subsequent elements to the right.
     * Increases the list size by one.
     *
     * @param element [Notification] The element to be inserted at the end of this list.
     *
     * @throws UnsupportedOperationException if adding elements to the list is not supported.
     *
     * @see MutableList.addLast
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun addLast(element: Notification) {
        items.addLast(element)
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = false) }
    }

    /**
     * Removes the first occurrence of the specified element from this list, if it is present.
     * If this list does not contain the element, it is unchanged.
     * Returns `true` if this list contained the specified element (in other words, if the list changed as a result of the call).
     *
     * @param element [Notification] The element to be removed from this list, if present.
     * @return [Boolean] `true` if the element was removed from the list, `false` otherwise.
     *
     * @see MutableList.remove
     */
    override fun remove(element: Notification): Boolean {
        val result = items.remove(element)
        if (result) {
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        }
        return result
    }

    /**
     * Removes the element at the specified position in this list.
     *
     * Shifts any subsequent elements to the left (reduces their indices).
     * The list size is decreased by one.
     *
     * @param index [Int] The index of the element to be removed.
     * @return [Notification] The element that was removed from the list.
     *
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size()).
     *
     * @see MutableList.removeAt
     */
    override fun removeAt(index: Int): Notification {
        val result = items.removeAt(index)
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        return result
    }

    /**
     * Removes from this list all of its elements that are contained in the specified collection.
     *
     * If this list changed as a result of the call, returns `true`. Otherwise returns `false`.
     *
     * @param elements [Collection<Notification>][Collection] A collection of elements to be removed from this list.
     * @return [Boolean] `true` if the list changed as a result of the call, `false` otherwise.
     *
     * @see MutableList.removeAll
     */
    override fun removeAll(elements: Collection<Notification>): Boolean {
        val result = items.removeAll(elements)
        if (result) {
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        }
        return result
    }

    /**
     * Removes and returns the first element from this list.
     *
     * If this list is empty, throws a NoSuchElementException.
     *
     * @return [Notification] The first element from this list.
     *
     * @throws NoSuchElementException if this list is empty.
     *
     * @see MutableList.removeFirst
     *
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun removeFirst(): Notification {
        val notification = items.removeFirst()
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        return notification
    }

    /**
     * Removes and returns the last element from this list.
     *
     * If this list is empty, throws a NoSuchElementException.
     *
     * @return [Notification] The last element from this list.
     *
     * @throws NoSuchElementException if this list is empty.
     *
     * @see MutableList.removeLast
     *
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun removeLast(): Notification {
        val notification = items.removeLast()
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        return notification
    }

    /**
     * Removes all elements from this list.
     *
     * If this list is not empty, it will be cleared and the changes will be persisted.
     * The coroutine will be launched to update the glance widgets with the reset index as true.
     *
     * @see MutableList.clear
     */
    override fun clear() {
        if (items.isNotEmpty()) {
            items.clear()
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        }
    }

    /**
     * Replaces each element in this list with the result of applying the given operator to that element.
     *
     * @param operator [UnaryOperator<Notification>][UnaryOperator] The operator to apply to each element.
     *
     * @see MutableList.replaceAll
     */
    override fun replaceAll(operator: UnaryOperator<Notification>) {
        items.replaceAll(operator)
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
    }

    /**
     * Replaces the elements in this list with the elements from the given list.
     *
     * @param newList [List<Notification>][List] The list of elements to replace the elements in this list.
     *
     * If this list is not empty, it will be cleared and the elements from the given list will be added.
     * The coroutine will be launched to update the glance widgets with the reset index as true.
     *
     * @see MutableList.clear
     * @see MutableList.addAll
     */
    fun replaceAll(newList: List<Notification>) {
        Snapshot.withMutableSnapshot {
            items.clear()
            items.addAll(newList)
        }
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
    }

    /**
     * Shuffles the elements in this list.
     *
     * If this list is not empty, the elements will be shuffled.
     * The coroutine will be launched to update the glance widgets with the reset index as true.
     *
     * @see MutableList.shuffle
     */
    fun shuffle() {
        Snapshot.withMutableSnapshot {
            items.shuffle()
        }
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
    }

    /**
     * Retains only the elements in this list that are contained in the specified collection.
     * If this list is modified as a result of the call, returns `true`. Otherwise returns `false`.
     *
     * @param elements [Collection<Notification>][Collection] A collection of elements to be retained in this list.
     * @return [Boolean] `true` if the list changed as a result of the call, `false` otherwise.
     *
     * @see MutableList.retainAll
     */
    override fun retainAll(elements: Collection<Notification>): Boolean {
        val result = items.retainAll(elements)
        if (result) {
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        }
        return result
    }

    /**
     * Sorts the list according to the order induced by the specified comparator.
     * If the comparator is null, the list is sorted according to the natural ordering of its elements.
     * All elements in the list must be mutually comparable by the specified comparator (that is, for any `e1` and `e2` in the list,
     * `e1.compareTo(e2))` does not throw a `ClassCastException`).
     *
     * @param comparator [Comparator<in Notification>?] The comparator to determine the order of the list.
     *        If null, the list is sorted according to the natural ordering of its elements.
     *
     * @see MutableList.sort
     */
    override fun sort(comparator: Comparator<in Notification>?) {
        sort(items, comparator)
        persist()
        coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
    }

    /**
     * Returns an iterator over the elements of this list in proper sequence.
     *
     * The iterator returned by this iterator method is fail-fast: if the list is structurally modified at any point after the iterator is created, in any way except through the iterator's own remove() or add() methods, the iterator will throw a ConcurrentModificationException. Thus, in the face of concurrent modification, the iterator fails quickly and cleanly, rather than risking arbitrary, non-deterministic behavior at an undetermined time in the future.
     *
     * @return [MutableIterator<Notification>][MutableIterator] An iterator over the elements of this list in proper sequence.
     *
     * @see MutableList.iterator
     */
    override fun iterator(): MutableIterator<Notification> = object : MutableIterator<Notification> {
        /**
         * A private iterator over the elements of this list in proper sequence.
         *
         * This iterator is fail-fast: if the list is structurally modified at any point after the iterator is created,
         * in any way except through the iterator's own remove() or add() methods, the iterator will throw a
         * ConcurrentModificationException. Thus, in the face of concurrent modification, the iterator fails quickly
         * and cleanly, rather than risking arbitrary, non-deterministic behavior at an undetermined time in the future.
         *
         * @return [MutableIterator<Notification>][MutableIterator] A private iterator over the elements of this list in proper sequence.
         *
         * @see MutableList.iterator
         */
        private val iterator = items.iterator()

        /**
         * Returns `true` if the iteration has more elements.
         *
         * (Note use of short circuit or the explicit and/or
         * operators without excessive parentheses; if a comment is associated
         * with a control flow statement, do not restate the comment on the same line.)
         *
         * @return [Boolean] `true` if the iteration has more elements.
         *
         * @see MutableIterator.hasNext
         */
        override fun hasNext() = iterator.hasNext()

        /**
         * Returns the next element from the iterator and advances the iterator to the next element.
         *
         * @return [Notification] The next element from the iterator.
         *
         * @throws NoSuchElementException If the iterator has no more elements.
         *
         * @see MutableIterator.next
         */
        override fun next() = iterator.next()

        /**
         * Removes from the underlying collection the last element returned by this iterator (optional operation).
         * This method can be called only once per call to next().
         *
         * @throws IllegalStateException if the next() method has not yet been called, or the remove() method has already been called after the last call to next().
         * @throws UnsupportedOperationException if the remove() operation is not supported by this iterator.
         *
         * @see MutableIterator.remove
         */
        override fun remove() {
            iterator.remove()
            persist()
        }
    }

    /**
     * Returns a list iterator over the elements in this list.
     *
     * The returned iterator is fail-fast: if the list is structurally modified at any point after the iterator is created,
     * in any way except through the iterator's own remove() or add() methods, the iterator will throw a ConcurrentModificationException.
     * Thus, in the face of concurrent modification, the iterator fails quickly and cleanly, rather than risking arbitrary,
     * non-deterministic behavior at an undetermined time in the future.
     *
     * @return [MutableListIterator<Notification>] A list iterator over the elements in this list.
     *
     * @see MutableList.listIterator
     */
    override fun listIterator(): MutableListIterator<Notification> = listIterator(0)

    /**
     * Returns a list iterator over the elements in this list starting from the specified index.
     *
     * The returned iterator is fail-fast: if the list is structurally modified at any point after the iterator is created,
     * in any way except through the iterator's own remove() or add() methods, the iterator will throw a ConcurrentModificationException.
     * Thus, in the face of concurrent modification, the iterator fails quickly and cleanly, rather than risking arbitrary,
     * non-deterministic behavior at an undetermined time in the future.
     *
     * @param index [Int] The index to start the iteration from.
     * @return [MutableListIterator<Notification>] A list iterator over the elements in this list starting from the specified index.
     *
     * @see MutableList.listIterator
     */
    override fun listIterator(index: Int): MutableListIterator<Notification> = object : MutableListIterator<Notification> {
        /**
         * An iterator over the elements in this list starting from the specified index.
         *
         * This iterator is fail-fast: if the list is structurally modified at any point after the iterator is created,
         * in any way except through the iterator's own remove() or add() methods, the iterator will throw a ConcurrentModificationException.
         * Thus, in the face of concurrent modification, the iterator fails quickly and cleanly, rather than risking arbitrary,
         * non-deterministic behavior at an undetermined time in the future.
         *
         * @see MutableList.listIterator
         */
        private val iterator = items.listIterator(index)

        /**
         * Returns `true` if the iteration has more elements.
         *
         * (Note use of short circuit or the explicit and/or
         * operators without excessive parentheses; if a comment is associated
         * with a control flow statement, do not restate the comment on the same line.)
         *
         * @return [Boolean] `true` if the iteration has more elements.
         *
         * @see MutableIterator.hasNext
         */
        override fun hasNext() = iterator.hasNext()

        /**
         * Returns the next element from the iterator and advances the iterator to the next element.
         *
         * @return [Notification] The next element from the iterator.
         *
         * @throws NoSuchElementException If the iterator has no more elements.
         *
         * @see MutableIterator.next
         */
        override fun next() = iterator.next()

        /**
         * Returns `true` if this list iterator has more elements when traversing the list in the reverse direction.
         *
         * @return [Boolean] `true` if this list iterator has more elements when traversing the list in the reverse direction.
         *
         * @see ListIterator.hasPrevious
         */
        override fun hasPrevious() = iterator.hasPrevious()

        /**
         * Returns the previous element in the list and moves the cursor position backwards.
         * This function will throw a NoSuchElementException if the list iterator has no previous element.
         *
         * @return [Notification] The previous element in the list.
         *
         * @throws NoSuchElementException If the list iterator has no previous element.
         *
         * @see ListIterator.previous
         */
        override fun previous() = iterator.previous()

        /**
         * Returns the index of the next element to return from the iterator, or the list size if there are no more elements.
         *
         * @return [Int] The index of the next element to return from the iterator, or the list size if there are no more elements.
         *
         * @see ListIterator.nextIndex
         */
        override fun nextIndex() = iterator.nextIndex()

        /**
         * Returns the index of the previous element to return from the iterator, or -1 if there are no previous elements.
         *
         * @return [Int] The index of the previous element to return from the iterator, or -1 if there are no previous elements.
         *
         * @see ListIterator.previousIndex
         */
        override fun previousIndex() = iterator.previousIndex()

        /**
         * Inserts the specified element into this list iterator.
         * The element will be inserted immediately before the next element that would be returned by next(),
         * if any. If the list iterator has no next element, the new element becomes the last element of the list.
         *
         * @param element [Notification] The element to be inserted into this list iterator.
         *
         * @see MutableIterator.add
         */
        override fun add(element: Notification) {
            iterator.add(element)
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = false) }
        }

        /**
         * Removes from the underlying list the last element returned by this iterator (optional operation).
         *
         * This method can be called only once per call to next(). If the underlying list defines a concept of "current element",
         * then "removing the current element" means removing the element that would be returned by the next call to next(),
         * if this list iterator were not a ListIterator nor the equivalent of the remove() operation were not called, in other words,
         * if the ListIterator were positioned before the current element.
         *
         * The ListIterator throws an IllegalStateException if the next() method has not yet been called, or the remove() method has already
         * been called after the last call to next().
         *
         * @throws IllegalStateException If the next() method has not yet been called, or the remove() method has already been called after the last call to next().
         *
         * @see MutableIterator.remove
         */
        override fun remove() {
            iterator.remove()
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        }

        /**
         * Replaces the element at the specified position in this list with the specified element.
         *
         * @param element [Notification] The element to be stored at the specified position.
         * @return [Notification] The element previously at the specified position.
         *
         * Note that this method is not thread-safe. It is the caller's responsibility to ensure that the list
         * is not modified concurrently by other threads.
         *
         * @see MutableList.set
         */
        override fun set(element: Notification) {
            iterator.set(element)
            persist()
            coroutineScope.launch { updateGlanceWidgets(resetIndex = true) }
        }
    }
}