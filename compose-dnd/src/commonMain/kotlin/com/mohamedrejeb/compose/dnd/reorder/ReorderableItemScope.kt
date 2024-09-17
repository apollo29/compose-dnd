package com.mohamedrejeb.compose.dnd.reorder

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.drag.DraggableItemScope
import kotlinx.coroutines.launch

interface ReorderableItemScope : DraggableItemScope {
    /**
     * Make the UI element the draggable handle for the reorderable item.
     *
     * This modifier can only be used on the UI element that is a child of [ReorderableItem].
     *
     * @param enabled Whether or not drag is enabled
     * @param interactionSource [MutableInteractionSource] that will be used to emit [DragInteraction.Start] when this draggable is being dragged.
     * @param onDragStarted The function that is called when the item starts being dragged
     * @param onDragStopped The function that is called when the item stops being dragged
     */
    fun Modifier.draggableHandle(
        enabled: Boolean = true,
        interactionSource: MutableInteractionSource? = null,
        onDragStarted: (startedPosition: Offset) -> Unit = {},
        onDragStopped: () -> Unit = {},
    ): Modifier

    /**
     * Make the UI element the draggable handle for the reorderable item. Drag will start only after a long press.
     *
     * This modifier can only be used on the UI element that is a child of [ReorderableItem].
     *
     * @param enabled Whether or not drag is enabled
     * @param interactionSource [MutableInteractionSource] that will be used to emit [DragInteraction.Start] when this draggable is being dragged.
     * @param onDragStarted The function that is called when the item starts being dragged
     * @param onDragStopped The function that is called when the item stops being dragged
     */
    fun Modifier.longPressDraggableHandle(
        enabled: Boolean = true,
        interactionSource: MutableInteractionSource? = null,
        onDragStarted: (startedPosition: Offset) -> Unit = {},
        onDragStopped: () -> Unit = {},
    ): Modifier
}

class ReorderableItemScopeImpl<N>(
    private val reorderState: ReorderState<N>,
    private val reorderableLazyCollectionState: ReorderableLazyCollectionState<*>,
    override val key: Any,
    private val itemPositionProvider: () -> Offset,
) : ReorderableItemScope {

    override val isDragging: Boolean
        get() = reorderState.draggedItem?.key == key

    /**
     * Make the UI element the draggable handle for the reorderable item.
     *
     * @param enabled Whether or not drag is enabled
     * @param interactionSource [MutableInteractionSource] that will be used to emit [DragInteraction.Start] when this draggable is being dragged.
     * @param onDragStarted The function that is called when the item starts being dragged
     * @param onDragStopped The function that is called when the item stops being dragged
     */
    @OptIn(ExperimentalDndApi::class)
    override fun Modifier.draggableHandle(
        enabled: Boolean,
        interactionSource: MutableInteractionSource?,
        onDragStarted: (startedPosition: Offset) -> Unit,
        onDragStopped: () -> Unit,
    ) = composed {
        var handleOffset by remember { mutableStateOf(Offset.Zero) }
        var handleSize by remember { mutableStateOf(IntSize.Zero) }

        val coroutineScope = rememberCoroutineScope()

        onGloballyPositioned {
            handleOffset = it.positionInRoot()
            handleSize = it.size
        }.draggable(
            key1 = reorderableLazyCollectionState,
            enabled = enabled && (reorderableLazyCollectionState.isItemDragging(key).value || !reorderableLazyCollectionState.isAnyItemDragging),
            interactionSource = interactionSource,
            onDragStarted = {
                coroutineScope.launch {
                    val handleOffsetRelativeToItem = handleOffset - itemPositionProvider()
                    val handleCenter = Offset(
                        handleOffsetRelativeToItem.x + handleSize.width / 2f,
                        handleOffsetRelativeToItem.y + handleSize.height / 2f,
                    )

                    reorderableLazyCollectionState.onDragStart(key, handleCenter)
                }
                onDragStarted(it)
            },
            onDragStopped = {
                reorderableLazyCollectionState.onDragStop()
                onDragStopped()
            },
            onDrag = { change, dragAmount ->
                // DND
                val drag = change
                val draggableItemState =
                    reorderState.dndState.draggableItemMap[key] ?: return@draggable

                coroutineScope.launch {
                    reorderState.dndState.handleDragStart(drag.position + draggableItemState.positionInRoot)
                }

                reorderState.dndState.pointerId = drag.id
                // END
                change.consume()
                reorderableLazyCollectionState.onDrag(dragAmount)
            },
        )
    }

    /**
     * Make the UI element the draggable handle for the reorderable item. Drag will start only after a long press.
     *
     * @param enabled Whether or not drag is enabled
     * @param interactionSource [MutableInteractionSource] that will be used to emit [DragInteraction.Start] when this draggable is being dragged
     * @param onDragStarted The function that is called when the item starts being dragged
     * @param onDragStopped The function that is called when the item stops being dragged
     */
    @OptIn(ExperimentalDndApi::class)
    override fun Modifier.longPressDraggableHandle(
        enabled: Boolean,
        interactionSource: MutableInteractionSource?,
        onDragStarted: (startedPosition: Offset) -> Unit,
        onDragStopped: () -> Unit,
    ) = composed {
        var handleOffset by remember { mutableStateOf(Offset.Zero) }
        var handleSize by remember { mutableStateOf(IntSize.Zero) }

        val coroutineScope = rememberCoroutineScope()

        onGloballyPositioned {
            handleOffset = it.positionInRoot()
            handleSize = it.size
        }.longPressDraggable(
            key1 = reorderableLazyCollectionState,
            enabled = enabled && (reorderableLazyCollectionState.isItemDragging(key).value || !reorderableLazyCollectionState.isAnyItemDragging),
            interactionSource = interactionSource,
            onDragStarted = {
                coroutineScope.launch {
                    val handleOffsetRelativeToItem = handleOffset - itemPositionProvider()
                    val handleCenter = Offset(
                        handleOffsetRelativeToItem.x + handleSize.width / 2f,
                        handleOffsetRelativeToItem.y + handleSize.height / 2f,
                    )

                    reorderableLazyCollectionState.onDragStart(key, handleCenter)
                }
                onDragStarted(it)
            },
            onDragStopped = {
                reorderableLazyCollectionState.onDragStop()
                onDragStopped()
            },
            onDrag = { change, dragAmount ->
                // DND
                val drag = change
                val draggableItemState =
                    reorderState.dndState.draggableItemMap[key] ?: return@longPressDraggable

                coroutineScope.launch {
                    reorderState.dndState.handleDragStart(drag.position + draggableItemState.positionInRoot)
                }

                reorderState.dndState.pointerId = drag.id
                // END

                change.consume()
                reorderableLazyCollectionState.onDrag(dragAmount)
            },
        )
    }
}
