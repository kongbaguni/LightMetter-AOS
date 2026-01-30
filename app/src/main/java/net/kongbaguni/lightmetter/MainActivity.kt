package net.kongbaguni.lightmetter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import net.kongbaguni.lightmetter.composable.TestComposable
import net.kongbaguni.lightmetter.ui.theme.LightMetterTheme
import net.kongbaguni.lightmetter.utill.LightMetterCameraManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LightMetterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TestComposable(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

