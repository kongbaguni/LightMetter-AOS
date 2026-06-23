package net.kongbaguni.lightmetter.composable.screen

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.kongbaguni.lightmetter.R
import net.kongbaguni.lightmetter.composable.component.AdBanner
import net.kongbaguni.lightmetter.composable.component.TabBar
import net.kongbaguni.lightmetter.model.BodyModel
import net.kongbaguni.lightmetter.model.LensModel
import net.kongbaguni.lightmetter.utill.DataStore

enum class Page {
    MAIN, BODYLIST, LENSLIST, BODY_EDIT, LENS_EDIT, SETTINGS
}

@Composable
fun ContentScreen(context: Context, modifier: Modifier) {
    var currentPage by remember { mutableStateOf<Page>(Page.MAIN) }
    var bodyToEdit by remember { mutableStateOf<BodyModel?>(null) }
    var lensToEdit by remember { mutableStateOf<LensModel?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }
    val dataStore = remember { DataStore(context) }

    BackHandler {
        when (currentPage) {
            Page.BODY_EDIT -> currentPage = Page.BODYLIST
            Page.LENS_EDIT -> currentPage = Page.LENSLIST
            Page.BODYLIST, Page.LENSLIST, Page.SETTINGS -> currentPage = Page.MAIN
            Page.MAIN -> showExitDialog = true
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.exit_confirm_title)) },
            text = { Text(stringResource(R.string.exit_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        (context as? Activity)?.finish()
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .padding(5.dp)
            .fillMaxSize()
    ) {
        AdBanner()
        Spacer(modifier = Modifier.height(8.dp))

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
            Page.SETTINGS -> SettingsScreen(
                dataStore = dataStore,
                onBack = { currentPage = Page.MAIN }
            )
        }
    }
}

