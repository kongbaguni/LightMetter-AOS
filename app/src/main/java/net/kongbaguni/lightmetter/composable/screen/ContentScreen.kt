package net.kongbaguni.lightmetter.composable.screen

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.kongbaguni.lightmetter.composable.component.TabBar

enum class Page {
    MAIN, BODYLIST, LENSLIST
}

@Composable
fun ContentScreen(context: Context, modifier: Modifier) {
    var currentPage by remember { mutableStateOf<Page>(Page.MAIN) }
    Column(
        modifier = modifier
            .padding(5.dp)
            .fillMaxSize()
    ) {
        TabBar(
            current = currentPage,
            onTabSelected = { currentPage = it }
        )

        when (currentPage) {
            Page.MAIN -> MainScreen(onNavigate = { currentPage = it })
            Page.BODYLIST -> BodyListScreen()
            Page.LENSLIST -> LensListScreen()
        }

    }
}

