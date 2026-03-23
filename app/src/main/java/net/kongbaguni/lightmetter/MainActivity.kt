package net.kongbaguni.lightmetter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import net.kongbaguni.lightmetter.composable.screen.ContentScreen
import net.kongbaguni.lightmetter.ui.theme.LightMetterTheme

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
