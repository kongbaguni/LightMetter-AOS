package net.kongbaguni.lightmetter.composable.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun EVIndecator(
    measuredEv: Double?,
    calculatedEv: Double?,
    isMeasuring: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val diff = if (measuredEv != null && calculatedEv != null) measuredEv - calculatedEv else null
        val activeColor = Color.Red
        val inactiveColor = Color.White.copy(alpha = 0.2f)

        // 노출 부족: 오른쪽 방향 세모 (더 밝게 조절 필요)
        val leftActive = isMeasuring || (diff != null && diff < -0.3)
        TriangleSymbol(direction = TriangleDirection.Right, color = if (leftActive) activeColor else inactiveColor)

        // 정노출: 동그라미
        val centerActive = isMeasuring || (diff != null && diff >= -0.3 && diff <= 0.3)
        CircleSymbol(color = if (centerActive) activeColor else inactiveColor)

        // 노출 과다: 왼쪽 방향 세모 (더 어둡게 조절 필요)
        val rightActive = isMeasuring || (diff != null && diff > 0.3)
        TriangleSymbol(direction = TriangleDirection.Left, color = if (rightActive) activeColor else inactiveColor)
    }
}

enum class TriangleDirection { Left, Right }

@Composable
private fun TriangleSymbol(direction: TriangleDirection, color: Color) {
    Canvas(modifier = Modifier.size(14.dp)) {
        val path = Path().apply {
            if (direction == TriangleDirection.Right) {
                moveTo(0f, 0f)
                lineTo(size.width, size.height / 2f)
                lineTo(0f, size.height)
            } else {
                moveTo(size.width, 0f)
                lineTo(0f, size.height / 2f)
                lineTo(size.width, size.height)
            }
            close()
        }
        drawPath(path = path, color = color)
    }
}

@Composable
private fun CircleSymbol(color: Color) {
    Canvas(modifier = Modifier.size(10.dp)) {
        drawCircle(color = color)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun EVIndecatorPreview() {
    EVIndecator(measuredEv = 12.0, calculatedEv = 12.0, isMeasuring = false)
}
