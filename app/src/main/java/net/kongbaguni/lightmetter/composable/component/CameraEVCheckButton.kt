package net.kongbaguni.lightmetter.composable.component

import android.Manifest
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.kongbaguni.lightmetter.model.LightMetterModel
import net.kongbaguni.lightmetter.utill.LightMetterCameraManager
@Composable
fun CameraEVCheckButton(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    var ev by remember { mutableStateOf<LightMetterModel?>(null) }

    val lightMetterCameraManager = remember {
        if (!isPreview) LightMetterCameraManager(context) else null
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted && !isPreview) {
                lightMetterCameraManager?.photometry(
                    { value -> ev = value },
                    {}
                )
            }
        }

    Column(modifier) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Button(onClick = {

                if (isPreview) return@Button

                lightMetterCameraManager?.photometry(
                    { value ->
                        ev = value
                    },
                    {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )

            }) {

                Text("측광")

                CameraMonitorIndicator(
                    modifier = Modifier.padding(10.dp),
                    context = context
                )
            }
        }

        EvProgressBar(ev?.getEv() ?: 0.0)
    }
}

@Preview(showBackground = true)
@Composable
fun CarmeraEvCheckButtonPreview() {
    CameraEVCheckButton()
}