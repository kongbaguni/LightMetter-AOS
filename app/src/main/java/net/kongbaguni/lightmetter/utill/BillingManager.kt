package net.kongbaguni.lightmetter.utill

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.android.billingclient.api.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillingManager(
    context: Context,
    private val dataStore: DataStore
) {
    private val applicationContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.Main)
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail

    private var billingClient: BillingClient = BillingClient.newBuilder(applicationContext)
        .setListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            }
        }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    init {
        startConnection()
        signInAnonymously()
    }

    private fun updateLoginState() {
        val user = auth.currentUser
        val loggedIn = user != null && !user.isAnonymous
        _isUserLoggedIn.value = loggedIn
        _userEmail.value = if (loggedIn) user?.email else null
        
        if (loggedIn) {
            syncWithFirestore()
        }
    }

    /** 로그아웃 (다시 익명 계정으로 전환) */
    fun signOut() {
        auth.signOut()
        signInAnonymously()
    }

    private fun signInAnonymously() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    syncWithFirestore()
                    updateLoginState()
                } else {
                    Log.e("BillingManager", "Anonymous sign-in failed: ${task.exception?.message}")
                }
            }
        } else {
            syncWithFirestore()
            updateLoginState()
        }
    }

    /** 구글 로그인 인텐트 생성 */
    fun getGoogleSignInIntent(context: Context): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("781605620834-tpof4a8ms7bkffphbtobnpd0j1b5pll9.apps.googleusercontent.com")
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso).signInIntent
    }

    /** 구글 로그인 결과 처리 및 계정 연동 */
    fun handleGoogleSignInResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)!!
            val idToken = account.idToken
            
            if (idToken == null) {
                Log.e("BillingManager", "Google ID Token is NULL. Check Web Client ID in Firebase Console.")
                return
            }

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val currentUser = auth.currentUser
            
            Log.d("BillingManager", "Attempting to link/sign-in with Google: ${account.email}")

            if (currentUser != null) {
                // 기존 익명 계정과 구글 계정 연동
                currentUser.linkWithCredential(credential)
                    .addOnCompleteListener { linkTask ->
                        if (linkTask.isSuccessful) {
                            Log.d("BillingManager", "Account linked successfully")
                            syncWithFirestore()
                            updateLoginState()
                        } else {
                            val exception = linkTask.exception
                            Log.w("BillingManager", "Linking failed: ${exception?.message}")
                            
                            // 이미 연동된 계정이 있다면 해당 계정으로 바로 로그인
                            auth.signInWithCredential(credential).addOnCompleteListener { signInTask ->
                                if (signInTask.isSuccessful) {
                                    Log.d("BillingManager", "Signed in with credential successfully")
                                    syncWithFirestore()
                                    updateLoginState()
                                } else {
                                    Log.e("BillingManager", "Sign-in failed: ${signInTask.exception?.message}")
                                }
                            }
                        }
                    }
            }
        } catch (e: com.google.android.gms.common.api.ApiException) {
            Log.e("BillingManager", "Google Sign-In ApiException: status=${e.statusCode}, message=${e.message}")
            if (e.statusCode == 7) {
                Log.e("BillingManager", "NETWORK_ERROR detected. Please check internet connection or Google Play Services.")
            }
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e("BillingManager", "Google Sign-In Exception: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    checkPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun syncWithFirestore() {
        val uid = auth.currentUser?.uid ?: return
        
        // 1. Upload local data to Firestore (Merge)
        uploadAllLocalData()

        // 2. Download from Firestore and update local
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    scope.launch {
                        // Billing & Settings
                        val expiryTime = document.getLong("ad_free_until") ?: 0L
                        val isSubActive = document.getBoolean("is_subscription_active") ?: false
                        val showPreview = document.getBoolean("show_preview") ?: true
                        val selectedBrand = document.getString("selected_brand")
                        val isoValue = document.getLong("iso_value")?.toInt() ?: 200
                        val apertureValue = document.getDouble("aperture_value") ?: 5.6
                        val shutterSpeedValue = document.getString("shutter_speed_value") ?: "1/125"

                        if (expiryTime > System.currentTimeMillis()) {
                            dataStore.restoreAdFreeUntil(expiryTime)
                        }
                        dataStore.saveSubscriptionActive(isSubActive)
                        dataStore.saveShowPreview(showPreview)
                        dataStore.saveSelectedBrand(selectedBrand)
                        dataStore.saveIso(isoValue)
                        dataStore.saveAperture(apertureValue)
                        dataStore.saveShutterSpeed(shutterSpeedValue)
                        
                        // Sync custom bodies
                        val bodies = document.get("custom_bodies") as? List<Map<String, Any>>
                        bodies?.forEach { map ->
                            val brand = map["brand"] as? String ?: ""
                            val name = map["name"] as? String ?: ""
                            val shutterSpeeds = map["shutterSpeeds"] as? List<String> ?: emptyList()
                            
                            // Check if exists locally to avoid duplicates (simplified)
                            val currentBodies = dataStore.repository.getAllBodies().first()
                            if (currentBodies.none { it.brand == brand && it.name == name }) {
                                dataStore.repository.insertBody(brand, name, shutterSpeeds)
                            }
                        }

                        // Sync custom lenses
                        val lenses = document.get("custom_lenses") as? List<Map<String, Any>>
                        lenses?.forEach { map ->
                            val brand = map["brand"] as? String ?: ""
                            val name = map["name"] as? String ?: ""
                            val apertures = map["apertures"] as? List<Double> ?: emptyList()
                            
                            val currentLenses = dataStore.repository.getAllLenses().first()
                            if (currentLenses.none { it.brand == brand && it.name == name }) {
                                dataStore.repository.insertLens(brand, name, apertures)
                            }
                        }
                    }
                }
            }
    }

    fun uploadAllLocalData() {
        val uid = auth.currentUser?.uid ?: return
        if (auth.currentUser?.isAnonymous == true) return
        
        scope.launch {
            try {
                val adFreeUntil = dataStore.getAdFreeUntil()
                val isSubActive = dataStore.isSubscriptionActive.first()
                val showPreview = dataStore.showPreview.first()
                val selectedBrand = dataStore.selectedBrand.first()
                val isoValue = dataStore.selectedIsoValue.first()
                val apertureValue = dataStore.selectedApertureValue.first()
                val shutterSpeedValue = dataStore.selectedShutterSpeedValue.first()
                val body = dataStore.selectedBody.first()
                val lens = dataStore.selectedLens.first()
                val filter = dataStore.selectedFilter.first()

                val customBodies = dataStore.repository.getAllBodies().first()
                    .filter { it.id >= 10000 }
                    .map { mapOf("brand" to it.brand, "name" to it.name, "shutterSpeeds" to it.shutterSpeeds) }

                val customLenses = dataStore.repository.getAllLenses().first()
                    .filter { it.id >= 10000 }
                    .map { mapOf("brand" to it.brand, "name" to it.name, "apertures" to it.apertures) }

                val data = mutableMapOf<String, Any?>(
                    "ad_free_until" to adFreeUntil,
                    "is_subscription_active" to isSubActive,
                    "show_preview" to showPreview,
                    "selected_brand" to selectedBrand,
                    "iso_value" to isoValue,
                    "aperture_value" to apertureValue,
                    "shutter_speed_value" to shutterSpeedValue,
                    "body_id" to body.id,
                    "lens_id" to lens.id,
                    "filter_id" to filter?.id,
                    "custom_bodies" to customBodies,
                    "custom_lenses" to customLenses
                )
                
                db.collection("users").document(uid).set(data.filterValues { it != null }, SetOptions.merge())
                    .addOnFailureListener { e -> Log.e("BillingManager", "Upload failed", e) }
            } catch (e: Exception) {
                Log.e("BillingManager", "Error preparing upload", e)
            }
        }
    }

    fun openSubscriptionManagement(activity: Activity) {
        val packageName = activity.packageName
        val url = "https://play.google.com/store/account/subscriptions?package=$packageName&sku=${BillingConstants.PRODUCT_ID_SUBSCRIPTION}"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        activity.startActivity(intent)
    }

    fun subscribeCoffee(activity: Activity, productId: String = BillingConstants.PRODUCT_ID_SUBSCRIPTION) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
        billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            val productDetailsList = queryProductDetailsResult.productDetailsList
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
                val flowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList).build()
                billingClient.launchBillingFlow(activity, flowParams)
            }
        }
    }

    fun buyCoffee(activity: Activity, productId: String = BillingConstants.PRODUCT_ID_ONE_TIME) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
        billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            val productDetailsList = queryProductDetailsResult.productDetailsList
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails).build()
                )
                val flowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList).build()
                billingClient.launchBillingFlow(activity, flowParams)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (purchase.products.contains(BillingConstants.PRODUCT_ID_ONE_TIME)) {
                val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                billingClient.consumeAsync(consumeParams) { billingResult, _ ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        scope.launch {
                            dataStore.addAdFreeDays(30)
                            uploadAllLocalData()
                        }
                    }
                }
            } else if (purchase.products.contains(BillingConstants.PRODUCT_ID_SUBSCRIPTION)) {
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken).build()
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            scope.launch {
                                dataStore.saveSubscriptionActive(true)
                                uploadAllLocalData()
                            }
                        }
                    }
                } else {
                    scope.launch {
                        dataStore.saveSubscriptionActive(true)
                        uploadAllLocalData()
                    }
                }
            }
        }
    }

    private fun checkPurchases() {
        val subParams = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        billingClient.queryPurchasesAsync(subParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val isSubActive = purchases.any { 
                    it.purchaseState == Purchase.PurchaseState.PURCHASED && it.products.contains(BillingConstants.PRODUCT_ID_SUBSCRIPTION)
                }
                scope.launch {
                    dataStore.saveSubscriptionActive(isSubActive)
                    uploadAllLocalData()
                }
            }
        }
    }
}
