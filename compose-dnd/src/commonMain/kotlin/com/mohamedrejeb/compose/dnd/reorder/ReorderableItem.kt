/*
 * Copyright 2023, Mohamed Ben Rejeb and the Compose Dnd project contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mohamedrejeb.compose.dnd.reorder

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyItemScope
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
import androidx.compose.ui.graphics.graphicsLayer
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
 * Mark this composable as a reorderable item.
 *
 * @param modifier The modifier to be applied to the item.
 * @param state The return value of [rememberReorderableLazyListState]
 * @param reorderState The reorder state.
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
fun <T> LazyItemScope.ReorderableItem(
    modifier: Modifier = Modifier,
    state: ReorderableLazyListState<T>,
    key: Any,
    data: T,
    zIndex: Float = 0f,
    enabled: Boolean = true,
    dropTargets: List<Any> = emptyList(),
    dropStrategy: DropStrategy = DropStrategy.SurfacePercentage,
    onDrop: (state: DraggedItemState<T>) -> Unit = {},
    onDragEnter: (state: DraggedItemState<T>) -> Unit = {},
    onDragExit: (state: DraggedItemState<T>) -> Unit = {},
    dropAnimationSpec: AnimationSpec<Offset> = SpringSpec(),
    sizeDropAnimationSpec: AnimationSpec<Size> = SpringSpec(),
    draggableContent: (@Composable () -> Unit),
    animateItemModifier: Modifier = Modifier.animateItemPlacement(),
    content: @Composable ReorderableItemScope.(isDraggable: Boolean) -> Unit,
) {
    var itemPosition by remember { mutableStateOf(Offset.Zero) }
    val reorderState = state.reorderState

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

    val reorderableItemScopeImpl = remember(state, key) {
        ReorderableItemScopeImpl(
            reorderState = reorderState,
            reorderableLazyCollectionState = state,
            key = key,
            itemPositionProvider = { itemPosition },
        )
    }
    // END

    val dragging by state.isItemDragging(key)
    val offsetModifier = if (dragging) {
        Modifier.then(
            Modifier.graphicsLayer {
                translationY = state.draggingItemOffset.y
                translationX = state.draggingItemOffset.x
            },
        )
    } else if (key == state.previousDraggingItemKey) {
        println("** previousDraggingItemKey $key")
        Modifier.then(
            Modifier.graphicsLayer {
                translationY = state.previousDraggingItemOffset.value.y
                translationX = state.previousDraggingItemOffset.value.x
            },
        )
    } else {
        //println("** animateItemModifier $key")
        animateItemModifier
    }

    with(reorderableItemScopeImpl) {
        Box(
            modifier = modifier
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
                        dragAfterLongPress = true,
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
            content(isDragging)
        }
    }

    LaunchedEffect(state.reorderableKeys, enabled) {
        if (enabled) {
            state.reorderableKeys.add(key)
        } else {
            state.reorderableKeys.remove(key)
        }
    }
}
