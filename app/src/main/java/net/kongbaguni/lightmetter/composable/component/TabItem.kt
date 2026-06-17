package net.kongbaguni.lightmetter.composable.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.kongbaguni.lightmetter.composable.screen.Page

@Composable
fun TabItem(
    page: Page,
    current: Page,
    onClick: (Page) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val selected = page == current

    val background by animateColorAsState(
        if (selected) Color(0xFFFFD54F) else Color.Transparent
    )

    val contentColor by animateColorAsState(
        if (selected) Color.Black else Color.Gray
    )

    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable { onClick(page) }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = contentColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
