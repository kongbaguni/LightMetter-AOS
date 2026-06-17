import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import net.kongbaguni.lightmetter.model.DialModel

@Composable
fun DialSelector(
    items: List<DialModel>,
    modifier: Modifier = Modifier,
    initialIndex: Int = 0,
    onValueChanged: (DialModel) -> Unit
) {

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    val flingBehavior = rememberSnapFlingBehavior(listState)

    var selectedIndex by remember { mutableStateOf(0) }

    LaunchedEffect(listState) {

        snapshotFlow {
            if (items.isEmpty()) null
            else listState.layoutInfo.visibleItemsInfo
        }
            .filterNotNull()
            .map { visibleItems ->
                val layoutInfo = listState.layoutInfo
                val center =
                    (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2

                visibleItems.minByOrNull {
                    kotlin.math.abs(
                        (it.offset + it.size / 2) - center
                    )
                }?.index ?: 0
            }
            .distinctUntilChanged()
            .collectLatest { index ->
                if (index in items.indices) {
                    selectedIndex = index
                    onValueChanged(items[index])
                }
            }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {

        LazyRow(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(horizontal = 150.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            itemsIndexed(items) { index, item ->

                val isSelected = index == selectedIndex

                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(60.dp)
                        .background(
                            if (isSelected) Color(0xFFFFD54F)
                            else Color.LightGray,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = item.title,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 중앙 선택 표시선
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(2.dp)
                .fillMaxHeight()
                .background(Color.Red)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DialSelectorPreview() {
    DialSelector(listOf<DialModel>(
        DialModel("1", 1.0),
        DialModel("2", 2.0),
        DialModel("3", 3.0),
        DialModel("4", 4.0),
        DialModel("4", 5.0),
        DialModel("4", 6.0),
        DialModel("4", 7.0),
    )) { }
}

