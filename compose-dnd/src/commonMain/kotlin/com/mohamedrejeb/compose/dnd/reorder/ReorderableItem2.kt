package com.mohamedrejeb.compose.dnd.reorder

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.drag.DraggedItemState
import com.mohamedrejeb.compose.dnd.drag.DropStrategy


/**
 * A composable that allows an item in LazyColumn or LazyRow to be reordered by dragging.
 *
 * @param state The return value of [rememberReorderableLazyListState]
 * @param key The key of the item, must be the same as the key passed to [LazyListScope.item](androidx.compose.foundation.lazy.item), [LazyListScope.items](androidx.compose.foundation.lazy.items) or similar functions in [LazyListScope](androidx.compose.foundation.lazy.LazyListScope)
 * @param enabled Whether or this item is reorderable. If true, the item will not move for other items but may still be draggable. To make an item not draggable, set `enable = false` in [Modifier.draggable] or [Modifier.longPressDraggable] instead.
 * @param animateItemModifier The [Modifier] that will be applied to items that are not being dragged.
 * @param modifier The modifier to be applied to the item.
 * @param state The reorder state.
 * @param key The key used to identify the item.
 * @param data The data associated with the item.
 * @param zIndex The z-index of the item.
 * @param enabled Whether the reorder is enabled.
 * @param dragAfterLongPress if true, drag will start after long press, otherwise drag will start after simple press
 * @param dropTargets - list of drop targets ids to which this item can be dropped, if empty, item can be dropped to any drop target
 * @param dropStrategy - strategy to determine the drop target
 * @param onDrop The action to perform when an item is dropped onto the target.
 * Accepts the dragged item state as a parameter.
 * @param onDragEnter The action to perform when an item is dragged over the target.
 * Accepts the dragged item state as a parameter.
 * @param onDragExit The action to perform when an item is dragged out of the target.
 * Accepts the dragged item state as a parameter.
 * @param dropAnimationSpec - animation spec for the drop animation
 * @param sizeDropAnimationSpec - animation spec for the size drop animation
 * @param draggableContent The content of the draggable item, if null, the content of the item will be used.
 * @param content The content of the item.
 */
@OptIn(ExperimentalDndApi::class, ExperimentalFoundationApi::class)
@Composable
fun <T> LazyItemScope.ReorderableItem2(
    modifier: Modifier = Modifier,
    state: ReorderableLazyListState,
    reorderState: ReorderState<T>,
    key: Any,
    data: T,
    zIndex: Float = 0f,
    enabled: Boolean = true,
    dragAfterLongPress: Boolean = reorderState.dndState.dragAfterLongPress,
    dropTargets: List<Any> = emptyList(),
    dropStrategy: DropStrategy = DropStrategy.SurfacePercentage,
    onDrop: (state: DraggedItemState<T>) -> Unit = {},
    onDragEnter: (state: DraggedItemState<T>) -> Unit = {},
    onDragExit: (state: DraggedItemState<T>) -> Unit = {},
    dropAnimationSpec: AnimationSpec<Offset> = SpringSpec(),
    sizeDropAnimationSpec: AnimationSpec<Size> = SpringSpec(),
    draggableContent: (@Composable () -> Unit),
    animateItemModifier: Modifier = Modifier.animateItemPlacement(),
    content: @Composable ReorderableCollectionItemScope.(isDragging: Boolean) -> Unit,
) {
    // DND
    LaunchedEffect(key, reorderState, data) {
        reorderState.dndState.draggableItemMap[key]?.data = data
    }

    LaunchedEffect(key, reorderState, dropTargets) {
        reorderState.dndState.draggableItemMap[key]?.dropTargets = dropTargets
    }

    LaunchedEffect(key, reorderState, dropStrategy) {
        reorderState.dndState.draggableItemMap[key]?.dropStrategy = dropStrategy
    }

    LaunchedEffect(key, reorderState, dropAnimationSpec) {
        reorderState.dndState.draggableItemMap[key]?.dropAnimationSpec = dropAnimationSpec
    }

    LaunchedEffect(key, reorderState, sizeDropAnimationSpec) {
        reorderState.dndState.draggableItemMap[key]?.sizeDropAnimationSpec = sizeDropAnimationSpec
    }

    DisposableEffect(key, reorderState) {
        onDispose {
            reorderState.dndState.removeDraggableItem(key)
        }
    }
    // END

    val orientation by derivedStateOf { state.orientation }
    val dragging by state.isItemDragging(key)
    val offsetModifier = if (dragging) {
        Modifier
            .zIndex(1f)
            .then(
                when (orientation) {
                    Orientation.Vertical -> Modifier.graphicsLayer {
                        translationY = state.draggingItemOffset.y
                    }

                    Orientation.Horizontal -> Modifier.graphicsLayer {
                        translationX = state.draggingItemOffset.x
                    }
                },
            )
    } else if (key == state.previousDraggingItemKey) {
        Modifier
            .zIndex(1f)
            .then(
                when (orientation) {
                    Orientation.Vertical -> Modifier.graphicsLayer {
                        translationY = state.previousDraggingItemOffset.value.y
                    }

                    Orientation.Horizontal -> Modifier.graphicsLayer {
                        translationX = state.previousDraggingItemOffset.value.x
                    }
                },
            )
    } else {
        animateItemModifier
    }

    ReorderableCollectionItem(
        modifier = modifier.then(offsetModifier),
        state = state,
        reorderState = reorderState,
        key = key,
        data = data,
        enabled = enabled,
        dragAfterLongPress = dragAfterLongPress,
        zIndex = zIndex,
        onDrop = onDrop,
        onDragEnter = onDragEnter,
        onDragExit = onDragExit,
        dropTargets = dropTargets,
        dropStrategy = dropStrategy,
        dropAnimationSpec = dropAnimationSpec,
        sizeDropAnimationSpec = sizeDropAnimationSpec,
        dragging = dragging,
        draggableContent = draggableContent,
        content = content,
    )
}
