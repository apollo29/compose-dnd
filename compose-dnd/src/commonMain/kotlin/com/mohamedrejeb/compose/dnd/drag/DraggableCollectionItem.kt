package com.mohamedrejeb.compose.dnd.drag

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.toSize
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.gesture.detectDragStartGesture
import com.mohamedrejeb.compose.dnd.reorder.draggable
import com.mohamedrejeb.compose.dnd.reorder.longPressDraggable
import com.mohamedrejeb.compose.dnd.reorder.rememberReorderableLazyListState


/**
 * A composable that allows items to be reordered by dragging.
 *
 * @param reorderState The return value of [rememberReorderableLazyListState], [rememberReorderableLazyGridState], or [rememberReorderableLazyStaggeredGridState]
 * @param key The key of the item, must be the same as the key passed to the parent composable
 * @param enabled Whether or this item is reorderable. If true, the item will not move for other items but may still be draggable. To make an item not draggable, set `enable = false` in [Modifier.draggable] or [Modifier.longPressDraggable] instead.
 * @param dragging Whether or not this item is currently being dragged
 */
@ExperimentalFoundationApi
@Composable
fun <T> DraggableCollectionItem(
    modifier: Modifier = Modifier,
    key: Any,
    data: T,
    state: DragAndDropState<T>,
    enabled: Boolean = true,
    dragAfterLongPress: Boolean = state.dragAfterLongPress,
    dropTargets: List<Any> = emptyList(),
    dropStrategy: DropStrategy = DropStrategy.SurfacePercentage,
    dropAnimationSpec: AnimationSpec<Offset> = SpringSpec(),
    sizeDropAnimationSpec: AnimationSpec<Size> = SpringSpec(),
    dragging: Boolean,
    draggableContent: (@Composable () -> Unit),
    content: @Composable DraggableCollectionItemScope.(isDragging: Boolean) -> Unit,
) {
    var itemPosition by remember { mutableStateOf(Offset.Zero) }

    // DRAG AND DROP
    LaunchedEffect(key, state, data) {
        state.draggableItemMap[key]?.data = data
    }

    LaunchedEffect(key, state, dropTargets) {
        state.draggableItemMap[key]?.dropTargets = dropTargets
    }

    LaunchedEffect(key, state, dropStrategy) {
        state.draggableItemMap[key]?.dropStrategy = dropStrategy
    }

    LaunchedEffect(key, state, dropAnimationSpec) {
        state.draggableItemMap[key]?.dropAnimationSpec = dropAnimationSpec
    }

    LaunchedEffect(key, state, sizeDropAnimationSpec) {
        state.draggableItemMap[key]?.sizeDropAnimationSpec = sizeDropAnimationSpec
    }

    DisposableEffect(key, state) {
        onDispose {
            state.removeDraggableItem(key)
        }
    }

    val draggableItemScopeShadowImpl = remember(key) {
        DraggableItemScopeShadowImpl(
            key = key,
        )
    }
    // END

    Box(
        modifier
            .onGloballyPositioned {
                itemPosition = it.positionInRoot()

                val draggableItemState = DraggableItemState(
                    key = key,
                    data = data,
                    positionInRoot = it.positionInRoot(),
                    size = it.size.toSize(),
                    dropTargets = dropTargets,
                    dropStrategy = dropStrategy,
                    dropAnimationSpec = dropAnimationSpec,
                    sizeDropAnimationSpec = sizeDropAnimationSpec,
                    content = draggableContent,
                )

                state.addOrUpdateDraggableItem(
                    state = draggableItemState,
                )
            }
            .pointerInput(enabled, key, state, state.enabled) {
                detectDragStartGesture(
                    key = key,
                    state = state,
                    enabled = enabled && state.enabled,
                    dragAfterLongPress = dragAfterLongPress,
                )
            },
    ) {
        val itemScope = remember(key) {
            DraggableCollectionItemScopeImpl(
                key = key,
                state = state,
            )
        }
        itemScope.content(dragging)
    }
}
