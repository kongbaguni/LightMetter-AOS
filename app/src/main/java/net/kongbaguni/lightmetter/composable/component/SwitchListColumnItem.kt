package net.kongbaguni.lightmetter.composable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest

@Composable
fun SwitchListColumnItem(
    brand: String,
    name: String,
    isSelected: Boolean,
    onClick: (Boolean) -> Unit
) {
    val context = LocalContext.current
    
    // SVG 파일 경로 생성 (예: assets/brand_icons/leica.svg)
    val iconPath = remember(brand) {
        "file:///android_asset/brand_icons/${brand.lowercase().trim()}.svg"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        onClick = { onClick(!isSelected) }
    ) {
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 브랜드 아이콘 표시 (Coil AsyncImage 사용)
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(iconPath)
                            .decoderFactory(SvgDecoder.Factory())
                            .crossfade(true)
                            .build(),
                        contentDescription = brand,
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.Fit,
                        error = null, // 에러 시 아래 fallback Surface가 보이게 됨
                    )
                    
                    // 이미지가 없거나 로딩 중일 때 보여줄 대체 UI (첫 글자 표시)
                    // AsyncImage의 success 여부를 직접 체크하기 어려우므로 
                    // 아래 레이어에 깔아두는 방식으로 처리
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = brand.take(1).uppercase(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // AsyncImage를 Surface 위에 다시 그려서 로드 성공 시 가리도록 함
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(iconPath)
                            .decoderFactory(SvgDecoder.Factory())
                            .build(),
                        contentDescription = brand,
                        modifier = Modifier.size(40.dp).background(Color.Transparent),
                        contentScale = ContentScale.Fit
                    )
                }

                Column {
                    Text(
                        text = brand,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = name,
                        fontSize = 18.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            ToggleSwitch(
                checked = isSelected,
                onCheckedChange = onClick,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Preview
@Composable
fun SwitchListColumnItemPreview() {
    Column() {
        SwitchListColumnItem(
            brand = "Leica",
            name = "I",
            isSelected = true,
            onClick = {}
        )
        SwitchListColumnItem(
            brand = "Leica",
            name = "M3",
            isSelected = false,
            onClick = {}
        )

    }
}
