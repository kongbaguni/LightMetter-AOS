package net.kongbaguni.lightmetter.composable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import net.kongbaguni.lightmetter.BuildConfig

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val width = LocalConfiguration.current.screenWidthDp
    val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, width)
    val adHeight = adSize.height

    val adid = if (BuildConfig.DEBUG) {
        "ca-app-pub-3940256099942544/9214589741" // 테스트 ID
    } else {
        "ca-app-pub-7714069006629518/7193351465" // 실제 배너 ID
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(adHeight.dp)
            .background(Color.LightGray.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "AD",
            color = Color.Gray,
            fontSize = 12.sp
        )

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                AdView(ctx).apply {
                    adUnitId = adid
                    setAdSize(adSize)
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
