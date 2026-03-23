package net.kongbaguni.lightmetter.composable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            .background(Color.DarkGray)
            .padding(4.dp)
    ) {
        TabItem(Page.MAIN, current, onTabSelected, "Main")
        TabItem(Page.BODYLIST, current, onTabSelected, "Body")
        TabItem(Page.LENSLIST, current, onTabSelected, "Lens")
    }
}