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
import net.kongbaguni.lightmetter.model.BodyModel
import net.kongbaguni.lightmetter.model.LensModel
import net.kongbaguni.lightmetter.utill.DataStore

enum class Page {
    MAIN, BODYLIST, LENSLIST, BODY_EDIT, LENS_EDIT
}

@Composable
fun ContentScreen(context: Context, modifier: Modifier) {
    var currentPage by remember { mutableStateOf<Page>(Page.MAIN) }
    var bodyToEdit by remember { mutableStateOf<BodyModel?>(null) }
    var lensToEdit by remember { mutableStateOf<LensModel?>(null) }
    val dataStore = remember { DataStore(context) }

    Column(
        modifier = modifier
            .padding(5.dp)
            .fillMaxSize()
    ) {
        if (currentPage != Page.BODY_EDIT && currentPage != Page.LENS_EDIT) {
            TabBar(
                current = currentPage,
                onTabSelected = { currentPage = it }
            )
        }

        when (currentPage) {
            Page.MAIN -> MainScreen(onNavigate = { currentPage = it })
            Page.BODYLIST -> BodyListScreen(
                onAddBody = { 
                    bodyToEdit = null
                    currentPage = Page.BODY_EDIT 
                },
                onEditBody = { body ->
                    bodyToEdit = body
                    currentPage = Page.BODY_EDIT
                }
            )
            Page.LENSLIST -> LensListScreen(
                onAddLens = {
                    lensToEdit = null
                    currentPage = Page.LENS_EDIT
                },
                onEditLens = { lens ->
                    lensToEdit = lens
                    currentPage = Page.LENS_EDIT
                }
            )
            Page.BODY_EDIT -> CustomBodyEditScreen(
                dataStore = dataStore,
                bodyToEdit = bodyToEdit,
                onBack = { currentPage = Page.BODYLIST }
            )
            Page.LENS_EDIT -> CustomLensEditScreen(
                dataStore = dataStore,
                lensToEdit = lensToEdit,
                onBack = { currentPage = Page.LENSLIST }
            )
        }
    }
}

