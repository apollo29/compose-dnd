package com.mohamedrejeb.compose.dnd.drag

import androidx.compose.runtime.Stable
import com.mohamedrejeb.compose.dnd.DragAndDropState

@Stable
interface DraggableCollectionItemScope {
    val key: Any
    val isDragging: Boolean
}

class DraggableCollectionItemScopeImpl<T>(
    val state: DragAndDropState<T>,
    override val key: Any,
) : DraggableCollectionItemScope {
    override val isDragging: Boolean
        get() = state.draggedItem?.key == key
}
