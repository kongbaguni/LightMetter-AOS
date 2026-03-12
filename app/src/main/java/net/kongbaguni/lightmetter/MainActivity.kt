package net.kongbaguni.lightmetter

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import net.kongbaguni.lightmetter.composable.CameraEVComposable
import net.kongbaguni.lightmetter.composable.CameraMonitorUI
import net.kongbaguni.lightmetter.ui.theme.LightMetterTheme
import net.kongbaguni.lightmetter.utill.CameraUsageMonitor

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LightMetterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ContentScreen(LocalContext.current, Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ContentScreen(context: Context, modifier: Modifier) {
    Column(modifier = modifier.padding(5.dp)) {
        CameraEVComposable()
    }
}