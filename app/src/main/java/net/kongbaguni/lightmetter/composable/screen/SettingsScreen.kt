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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.kongbaguni.lightmetter.R
import net.kongbaguni.lightmetter.utill.BillingManager
import net.kongbaguni.lightmetter.utill.DataStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    dataStore: DataStore,
    billingManager: BillingManager,
    onBack: () -> Unit,
    onNavigateToBodyList: () -> Unit,
    onNavigateToLensList: () -> Unit,
) {
    val context = LocalContext.current
    
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
    val unknownVersion = stringResource(R.string.settings_version_unknown)
    val networkErrorMsg = stringResource(R.string.settings_network_error)
    
    val versionName = remember(unknownVersion) {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: unknownVersion
        } catch (_: Exception) {
            unknownVersion
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
                text = stringResource(R.string.settings_account_section),
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
                                    Text(stringResource(R.string.settings_google_connected), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    userEmail?.let {
                                        Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            TextButton(onClick = { billingManager.signOut() }) {
                                Text(stringResource(R.string.settings_logout), color = MaterialTheme.colorScheme.error)
                            }
                        }
                    } else {
                        Text(
                            stringResource(R.string.settings_google_connect_msg),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (isOnline()) {
                                    googleSignInLauncher.launch(billingManager.getGoogleSignInIntent(context))
                                } else {
                                    Toast.makeText(context, networkErrorMsg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.settings_google_connect_btn))
                        }
                    }
                }
            }

            if(!isAdFree) {
                // Support Section
                Text(
                    text = stringResource(R.string.settings_support_section),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

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
                            text = stringResource(R.string.settings_coffee_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.settings_coffee_msg),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
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
                                    Text(stringResource(R.string.settings_subscription_manage))
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
                                            Toast.makeText(context, networkErrorMsg, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    val text = if (isAdFree) stringResource(R.string.settings_one_time_support_extend) else stringResource(R.string.settings_one_time_support)
                                    Text(text)
                                }

                                Button(
                                    onClick = {
                                        if (isOnline()) {
                                            (context as? Activity)?.let { activity ->
                                                billingManager.subscribeCoffee(activity)
                                            }
                                        } else {
                                            Toast.makeText(context, networkErrorMsg, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Text(stringResource(R.string.settings_subscription_support))
                                }
                            }
                        }

                    }
                }
            }

            // Equipment Section
            Text(
                text = stringResource(R.string.settings_equipment_section),
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
                        title = stringResource(R.string.settings_body),
                        value = selectedBody?.let { "${it.brand} ${it.name}" } ?: stringResource(R.string.settings_body_select),
                        onClick = onNavigateToBodyList
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    SettingsItem(
                        title = stringResource(R.string.settings_lens),
                        value = selectedLens?.let { "${it.brand} ${it.name}" } ?: stringResource(R.string.settings_lens_select),
                        onClick = onNavigateToLensList
                    )
                }
            }

            // Information Section
            Text(
                text = stringResource(R.string.settings_info_section),
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
                    SettingsItem(title = stringResource(R.string.settings_app_version), value = versionName)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    SettingsItem(
                        title = stringResource(R.string.settings_developer),
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun SettingScreenPreview() {
    val context = LocalContext.current
    val dataStore = remember { DataStore(context) }
    val billingManager = remember { BillingManager(context, dataStore) }
    SettingsScreen(
        dataStore = dataStore,
        billingManager = billingManager,
        onBack = { },
        onNavigateToBodyList = { },
        onNavigateToLensList = { }
    )
}
