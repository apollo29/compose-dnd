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
package com.mohamedrejeb.compose.dnd.drag

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset

/**
 * State of the dragged item
 *
 * @param key - key of the dragged item
 * @param data - data of the dragged item
 * @param dragAmount - amount of the drag
 */
@Immutable
class DraggedItemState<T> internal constructor(
    val key: Any,
    val data: T,
    val dragAmount: Offset,
) {
    internal fun copy(
        key: Any = this.key,
        data: T = this.data,
        dragAmount: Offset = this.dragAmount,
    ): DraggedItemState<T> {
        return DraggedItemState(
            key = key,
            data = data,
            dragAmount = dragAmount,
        )
    }
}
