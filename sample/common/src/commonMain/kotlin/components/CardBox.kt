package components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun CardBox(
    modifier: Modifier = Modifier,
    item: String,
    isDragging: Boolean = false,
    isDraggableContent: Boolean = false,
) {
    Box(
        modifier = modifier
            .width(155.dp)
            .height(105.dp)
            .padding(5.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, Color.LightGray, RoundedCornerShape(10.dp))
                .background(color = if (isDragging) Color.LightGray else Color.White)
                .graphicsLayer {
                    alpha = if (isDraggableContent) 1f else .5f
                },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = item, style = MaterialTheme.typography.titleLarge)
        }
    }
}
