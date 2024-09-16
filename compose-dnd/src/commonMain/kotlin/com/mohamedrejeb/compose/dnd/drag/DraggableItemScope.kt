package com.mohamedrejeb.compose.dnd.drag

import androidx.compose.runtime.Stable
import com.mohamedrejeb.compose.dnd.DragAndDropState

@Stable
interface DraggableItemScope {
    val key: Any
    val isDragging: Boolean
}

class DraggableItemScopeImpl<T>(
    val state: DragAndDropState<T>,
    override val key: Any,
) : DraggableItemScope {
    override val isDragging: Boolean
        get() = state.draggedItem?.key == key
}

class DraggableItemScopeShadowImpl<T>(
    val state: DragAndDropState<T>,
    override val key: Any,
) : DraggableItemScope {
    override val isDragging: Boolean
        get() = false
}
