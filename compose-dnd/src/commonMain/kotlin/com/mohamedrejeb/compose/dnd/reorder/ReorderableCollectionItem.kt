package com.mohamedrejeb.compose.dnd.reorder

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
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
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.drag.DraggableItemState
import com.mohamedrejeb.compose.dnd.drag.DraggedItemState
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.gesture.detectDragStartGesture


/**
 * A composable that allows items to be reordered by dragging.
 *
 * @param state The return value of [rememberReorderableLazyListState], [rememberReorderableLazyGridState], or [rememberReorderableLazyStaggeredGridState]
 * @param key The key of the item, must be the same as the key passed to the parent composable
 * @param enabled Whether or this item is reorderable. If true, the item will not move for other items but may still be draggable. To make an item not draggable, set `enable = false` in [Modifier.draggable] or [Modifier.longPressDraggable] instead.
 * @param dragging Whether or not this item is currently being dragged
 */
@OptIn(ExperimentalDndApi::class)
@ExperimentalFoundationApi
@Composable
fun <N> ReorderableCollectionItem(
    modifier: Modifier = Modifier,
    state: ReorderableLazyCollectionState<*>,
    reorderState: ReorderState<N>,
    key: Any,
    data: N,
    enabled: Boolean = true,
    dragAfterLongPress: Boolean = reorderState.dndState.dragAfterLongPress,
    zIndex: Float = 0f,
    onDrop: (state: DraggedItemState<N>) -> Unit = {},
    onDragEnter: (state: DraggedItemState<N>) -> Unit = {},
    onDragExit: (state: DraggedItemState<N>) -> Unit = {},
    dropTargets: List<Any> = emptyList(),
    dropStrategy: DropStrategy = DropStrategy.SurfacePercentage,
    dropAnimationSpec: AnimationSpec<Offset> = SpringSpec(),
    sizeDropAnimationSpec: AnimationSpec<Size> = SpringSpec(),
    dragging: Boolean,
    draggableContent: @Composable () -> Unit,
    content: @Composable (ReorderableCollectionItemScope.(isDragging: Boolean) -> Unit),
) {
    var itemPosition by remember { mutableStateOf(Offset.Zero) }

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

                reorderState.dndState.addOrUpdateDraggableItem(
                    state = draggableItemState,
                )
            }
            .pointerInput(enabled, key, reorderState, reorderState.dndState.enabled) {
                detectDragStartGesture(
                    key = key,
                    state = reorderState.dndState,
                    enabled = enabled && reorderState.dndState.enabled,
                    dragAfterLongPress = dragAfterLongPress,
                )
            }
            .dropTarget(
                key = key,
                state = reorderState.dndState,
                zIndex = zIndex,
                onDrop = onDrop,
                onDragEnter = onDragEnter,
                onDragExit = onDragExit,
            )
            .then(modifier),
    ) {
        val itemScope = remember(state, key) {
            ReorderableCollectionItemScopeImpl(
                reorderState = reorderState,
                reorderableLazyCollectionState = state,
                key = key,
                itemPositionProvider = { itemPosition },
            )
        }
        itemScope.content(dragging)
    }

    LaunchedEffect(state.reorderableKeys, enabled) {
        if (enabled) {
            state.reorderableKeys.add(key)
        } else {
            state.reorderableKeys.remove(key)
        }
    }
}
