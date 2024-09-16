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
package ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.reorder.ReorderContainer
import com.mohamedrejeb.compose.dnd.reorder.ReorderableItem
import com.mohamedrejeb.compose.dnd.reorder.rememberReorderState
import com.mohamedrejeb.compose.dnd.reorder.rememberReorderableLazyListState
import components.CardBox

object ListToListWithReorderScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "From list to list (with reorder)",
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                navigator.pop()
                            },
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            ListToListWithReorderContent(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(paddingValues)
                    .padding(20.dp),
            )
        }
    }
}

@OptIn(ExperimentalDndApi::class)
@Composable
private fun ListToListWithReorderContent(
    modifier: Modifier = Modifier,
) {
    var listOne by remember {
        mutableStateOf(
            listOf(
                "item1",
                "item2",
                "item3",
                "item4",
            ),
        )
    }

    var listTwo by remember {
        mutableStateOf(
            listOf(
                "item5",
                "item6",
                "item7",
                "item8",
                "item9",
                "item10",
                "item11",
                "item12",
            ),
        )
    }

    val lazyListStateOne = rememberLazyListState()
    val lazyListStateTwo = rememberLazyListState()

    val reorderState = rememberReorderState<String>(true)

    val reorderableLazyListStateOne =
        rememberReorderableLazyListState(
            lazyListState = lazyListStateOne,
            dragAfterLongPress = true,
        ) { from, to ->
            listOne = listOne.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        }

    val reorderableLazyListStateTimeline =
        rememberReorderableLazyListState(
            lazyListState = lazyListStateTwo,
            dragAfterLongPress = true,
        ) { from, to ->
            listTwo = listTwo.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        }


    ReorderContainer(
        state = reorderState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            LazyRow(
                state = lazyListStateOne,
                contentPadding = PaddingValues(10.dp),
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(24.dp),
                    )
                    .dropTarget(
                        key = "listOne",
                        state = reorderState.dndState,
                        dropAnimationEnabled = false,
                        onDragEnter = { state ->
                            listTwo = listTwo.toMutableList().apply {
                                val isRemoved = remove(state.data)
                                if (!isRemoved) return@dropTarget
                            }

                            listOne = listOne.toMutableList().apply {
                                add(state.data)
                            }
                        },
                    ),
            ) {
                items(listOne, key = { it }) { item ->
                    ReorderableItem(
                        state = reorderableLazyListStateOne,
                        reorderState = reorderState,
                        key = item,
                        data = item,
                        zIndex = 1f,
                        dropStrategy = DropStrategy.CenterDistance,
                        onDragEnter = { state ->
                            listOne = listOne.toMutableList().apply {
                                val index = indexOf(item)
                                if (index == -1) return@ReorderableItem
                                if (!remove(state.data)) {
                                    // If the item is not in listOne, it means it's coming from the listTwo
                                    listTwo = listTwo.toMutableList().apply {
                                        remove(state.data)
                                    }
                                }

                                add(index, state.data)
                            }
                        },
                        draggableContent = {
                            CardBox(
                                item = item,
                                isDraggableContent = true,
                            )
                        },
                        modifier = Modifier,
                    ) {
                        // todo isDragging is Redundant
                        CardBox(item = item, isDragging = isDragging)
                    }
                }
            }

            LazyColumn(
                state = lazyListStateTwo,
                contentPadding = PaddingValues(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(24.dp),
                    )
                    .dropTarget(
                        key = "listTwo",
                        state = reorderState.dndState,
                        dropAnimationEnabled = false,
                        onDragEnter = { state ->
                            listOne = listOne.toMutableList().apply {
                                val isRemoved = remove(state.data)
                                if (!isRemoved) return@dropTarget
                            }

                            listTwo = listTwo.toMutableList().apply {
                                add(state.data)
                            }
                        },
                    ),
            ) {
                items(listTwo, key = { it }) { item ->
                    ReorderableItem(
                        state = reorderableLazyListStateTimeline,
                        reorderState = reorderState,
                        key = item,
                        data = item,
                        zIndex = 1f,
                        dropStrategy = DropStrategy.CenterDistance,
                        onDragEnter = { state ->
                            listTwo = listTwo.toMutableList().apply {
                                val index = indexOf(item)
                                if (index == -1) return@ReorderableItem
                                if (!remove(state.data)) {
                                    // If the item is not in listTwo, it means it's coming from the listOne
                                    listOne = listOne.toMutableList().apply {
                                        remove(state.data)
                                    }
                                }

                                add(index, state.data)
                            }
                        },
                        draggableContent = {
                            CardBox(
                                item = item,
                                isDraggableContent = true,
                            )
                        },
                        modifier = Modifier,
                    ) {
                        CardBox(
                            item = item,
                            isDragging = isDragging,
                        )
                    }
                }
            }
        }
    }
}
