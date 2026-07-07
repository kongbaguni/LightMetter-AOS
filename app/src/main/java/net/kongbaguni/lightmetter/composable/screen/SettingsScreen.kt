package net.kongbaguni.lightmetter.composable.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.kongbaguni.lightmetter.utill.BillingManager
import net.kongbaguni.lightmetter.utill.DataStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    dataStore: DataStore,
    onBack: () -> Unit,
    onNavigateToBodyList: () -> Unit,
    onNavigateToLensList: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    val isAdFree by dataStore.isAdFree.collectAsState(initial = false)
    val isSubscriptionActive by dataStore.isSubscriptionActive.collectAsState(initial = false)
    val selectedBody by dataStore.selectedBody.collectAsState(initial = null)
    val selectedLens by dataStore.selectedLens.collectAsState(initial = null)

    val billingManager = remember { BillingManager(context, dataStore) }
    val isUserLoggedIn by billingManager.isUserLoggedIn.collectAsState()
    val userEmail by billingManager.userEmail.collectAsState()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            billingManager.handleGoogleSignInResult(result.data)
        }
    }

    // 앱 버전 가져오기
    val versionName = remember {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "알 수 없음"
        } catch (e: Exception) {
            "알 수 없음"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Section
            Text(
                text = "계정",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isUserLoggedIn) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, tint = Color.Green)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("구글 계정 연결됨", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    userEmail?.let {
                                        Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            TextButton(onClick = { billingManager.signOut() }) {
                                Text("로그아웃", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    } else {
                        Text(
                            "구매 내역을 안전하게 보관하려면 구글 계정을 연결하세요.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (isOnline()) {
                                    googleSignInLauncher.launch(billingManager.getGoogleSignInIntent(context))
                                } else {
                                    Toast.makeText(context, "인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("구글 계정 연결하기")
                        }
                    }
                }
            }

            if(!isAdFree) {
                // Support Section
                Text(
                    text = "후원",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
// ... 나머지 코드 동일 ...

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Coffee,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "커피 한 잔 선물하기",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "개발자에게 커피 한 잔을 선물하고\n광고 없이 모든 기능을 즐기세요!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isSubscriptionActive) {
                                // 구독 중인 경우: 관리 버튼 표시
                                Button(
                                    onClick = {
                                        (context as? Activity)?.let { activity ->
                                            billingManager.openSubscriptionManagement(activity)
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Text("정기 구독 관리하기")
                                }
                            } else {
                                // 구독 중이 아닌 경우: 후원 버튼들 표시
                                Button(
                                    onClick = {
                                        if (isOnline()) {
                                            (context as? Activity)?.let { activity ->
                                                billingManager.buyCoffee(activity)
                                            }
                                        } else {
                                            Toast.makeText(context, "인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    val text = if (isAdFree) "일회성 후원 연장하기 (30일)" else "한번만 사주기 (30일)"
                                    Text(text)
                                }

                                Button(
                                    onClick = {
                                        if (isOnline()) {
                                            (context as? Activity)?.let { activity ->
                                                billingManager.subscribeCoffee(activity)
                                            }
                                        } else {
                                            Toast.makeText(context, "인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Text("정기 후원하기 (매달)")
                                }
                            }
                        }

                    }
                }
            }

            // Equipment Section
            Text(
                text = "내 장비",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    SettingsItem(
                        title = "바디",
                        value = selectedBody?.let { "${it.brand} ${it.name}" } ?: "바디 선택",
                        onClick = onNavigateToBodyList
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    SettingsItem(
                        title = "렌즈",
                        value = selectedLens?.let { "${it.brand} ${it.name}" } ?: "렌즈 선택",
                        onClick = onNavigateToLensList
                    )
                }
            }

            // Information Section
            Text(
                text = "정보",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    SettingsItem(title = "앱 버전", value = versionName)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    SettingsItem(
                        title = "개발자",
                        value = "kongbaguni@gmail.com",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:kongbaguni@gmail.com")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 15.sp)
        Text(
            text = value,
            fontSize = 14.sp,
            color = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingScreenPreview() {
    val context = LocalContext.current
    val dataStore = remember { DataStore(context) }
    SettingsScreen(
        dataStore = dataStore,
        onBack = { },
        onNavigateToBodyList = { },
        onNavigateToLensList = { }
    )
}