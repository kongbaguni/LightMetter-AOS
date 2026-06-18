package net.kongbaguni.lightmetter.composable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import net.kongbaguni.lightmetter.composable.screen.Page

@Composable
fun TabBar(
    current: Page,
    onTabSelected: (Page) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp)
    ) {
        TabItem(Page.MAIN, current, onTabSelected, "MAIN", Modifier.weight(1f))
        TabItem(Page.BODYLIST, current, onTabSelected, "BODY", Modifier.weight(1f))
        TabItem(Page.LENSLIST, current, onTabSelected, "LENS", Modifier.weight(1f))
    }
}
