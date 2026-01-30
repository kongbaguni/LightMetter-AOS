package net.kongbaguni.lightmetter.composable

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import net.kongbaguni.lightmetter.MainActivity
import net.kongbaguni.lightmetter.utill.LightMetterCameraManager

@Composable
fun TestComposable (
    modifier : Modifier
){
    val lightMetterCameraManager: LightMetterCameraManager = LightMetterCameraManager(LocalContext.current)

    var iso by remember { mutableStateOf<Double?>(null) }
    var shutter by remember { mutableStateOf<Double?>(null) }
    var aperture by remember { mutableStateOf<Double?>(null) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                lightMetterCameraManager.watch(
                    onChangeISO = { iso = it },
                    onChangeShutterSpeed = { shutter = it },
                    onChangeAperture = { aperture = it },
                    onRequestCameraPermission = {}
                )
            }
        }

    Column {
        Row() {
            Button(onClick = {
                lightMetterCameraManager.watch(
                    { iso = it },
                    { shutter = it },
                    { aperture = it },
                    {
                        permissionLauncher.launch(Manifest.permission.CAMERA)

                    }
                )
            }) {
                Text(
                    text = "start",
                )
            }

            Button({
                lightMetterCameraManager.stop()
                iso = null
                shutter = null
                aperture = null
            }) {
                Text(
                    "stop"
                )
            }

        }
        Row {
            Text("iso : ${iso ?: 0.0}")
            Text("shutter : ${shutter ?: 0.0}")
            Text("aperture : ${aperture ?: 0.0}")
        }
    }


}