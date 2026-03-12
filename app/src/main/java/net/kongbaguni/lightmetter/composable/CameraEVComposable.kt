package net.kongbaguni.lightmetter.composable

import android.Manifest
import android.graphics.Camera
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import net.kongbaguni.lightmetter.model.LightMetterModel
import net.kongbaguni.lightmetter.utill.LightMetterCameraManager

@Composable
fun CameraEVComposable (
    modifier : Modifier = Modifier
){
    val lightMetterCameraManager: LightMetterCameraManager = LightMetterCameraManager(LocalContext.current)

    var ev by remember { mutableStateOf<LightMetterModel?>(null) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                lightMetterCameraManager.photometry ({ value ->
                    ev = value
                }, {

                }
                )
            }
        }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                lightMetterCameraManager.photometry({ value ->
                    ev = value
                },
                {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
                )
            }) {
                Text(
                    text = "측광",
                )
                CameraMonitorUI(context = LocalContext.current, Modifier.padding(10.dp))

            }
        }
        EvProgressBar(ev?.getEv() ?: 0.0)
    }
}
