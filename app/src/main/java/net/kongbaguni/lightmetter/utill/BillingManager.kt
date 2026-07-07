package net.kongbaguni.lightmetter.utill

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BillingManager(
    context: Context,
    private val dataStore: DataStore
) {
    private val scope = CoroutineScope(Dispatchers.Main)
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
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    checkPurchases()
                    restorePurchases() // 재설치 시 복구 로직 추가
                }
            }

            override fun onBillingServiceDisconnected() {}
        })
    }

    /** 구독 관리 페이지로 이동 */
    fun openSubscriptionManagement(activity: Activity) {
        val packageName = activity.packageName
        val url = "https://play.google.com/store/account/subscriptions?package=$packageName&sku=${BillingConstants.PRODUCT_ID_SUBSCRIPTION}"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        activity.startActivity(intent)
    }

    /** 정기 구독 시작 */
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

    /** 일회성 커피 구매 */
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
                            dataStore.addAdFreeDays(30) // 30일 연장
                        }
                    }
                }
            } else if (purchase.products.contains(BillingConstants.PRODUCT_ID_SUBSCRIPTION)) {
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken).build()
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            scope.launch { dataStore.saveSubscriptionActive(true) }
                        }
                    }
                } else {
                    scope.launch { dataStore.saveSubscriptionActive(true) }
                }
            }
        }
    }

    /** 현재 유효한 구매 확인 */
    private fun checkPurchases() {
        val subParams = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        billingClient.queryPurchasesAsync(subParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val isSubActive = purchases.any { 
                    it.purchaseState == Purchase.PurchaseState.PURCHASED && it.products.contains(BillingConstants.PRODUCT_ID_SUBSCRIPTION)
                }
                scope.launch { dataStore.saveSubscriptionActive(isSubActive) }
            }
        }
    }

    /** [중요] 재설치 시 이미 소모된 내역을 바탕으로 기간 복구 */
    private fun restorePurchases() {
        val params = QueryPurchaseHistoryParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchaseHistoryAsync(params) { billingResult, historyList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && historyList != null) {
                val lastCoffeePurchase = historyList.find { it.products.contains(BillingConstants.PRODUCT_ID_ONE_TIME) }
                lastCoffeePurchase?.let { history ->
                    val purchaseTime = history.purchaseTime
                    val oneMonthMillis = 30L * 24 * 60 * 60 * 1000
                    val expiryTime = purchaseTime + oneMonthMillis

                    // 아직 30일이 지나지 않았다면 복구
                    if (expiryTime > System.currentTimeMillis()) {
                        scope.launch {
                            dataStore.restoreAdFreeUntil(expiryTime)
                        }
                    }
                }
            }
        }
    }
}
