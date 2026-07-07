package net.kongbaguni.lightmetter.utill

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import kotlinx.coroutines.launch

class BillingManager(
    context: Context,
    private val dataStore: DataStore
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
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
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            val currentUser = auth.currentUser
            if (currentUser != null) {
                // 기존 익명 계정과 구글 계정 연동 (구매 내역 보존)
                currentUser.linkWithCredential(credential)
                    .addOnCompleteListener { linkTask ->
                        if (linkTask.isSuccessful) {
                            syncWithFirestore()
                            updateLoginState()
                        } else {
                            // 연동 실패 시 (이미 다른 계정이 있을 경우 등) 직접 로그인
                            auth.signInWithCredential(credential).addOnCompleteListener { signInTask ->
                                if (signInTask.isSuccessful) {
                                    syncWithFirestore()
                                    updateLoginState()
                                }
                            }
                        }
                    }
            }
        } catch (e: Exception) {
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
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val expiryTime = document.getLong("ad_free_until") ?: 0L
                    val isSubActive = document.getBoolean("is_subscription_active") ?: false
                    
                    scope.launch {
                        if (expiryTime > System.currentTimeMillis()) {
                            dataStore.restoreAdFreeUntil(expiryTime)
                        }
                        dataStore.saveSubscriptionActive(isSubActive)
                    }
                }
            }
    }

    private fun uploadPurchaseToFirestore(expiryTime: Long? = null, isSubActive: Boolean? = null) {
        val uid = auth.currentUser?.uid ?: return
        val updates = mutableMapOf<String, Any>()
        expiryTime?.let { updates["ad_free_until"] = it }
        isSubActive?.let { updates["is_subscription_active"] = it }

        if (updates.isNotEmpty()) {
            db.collection("users").document(uid).set(updates, SetOptions.merge())
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
                            val newExpiry = dataStore.getAdFreeUntil()
                            uploadPurchaseToFirestore(expiryTime = newExpiry)
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
                                uploadPurchaseToFirestore(isSubActive = true)
                            }
                        }
                    }
                } else {
                    scope.launch {
                        dataStore.saveSubscriptionActive(true)
                        uploadPurchaseToFirestore(isSubActive = true)
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
                    uploadPurchaseToFirestore(isSubActive = isSubActive)
                }
            }
        }
    }
}
