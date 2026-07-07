package net.kongbaguni.lightmetter.utill

import android.app.Activity
import android.content.Context
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
                }
            }

            override fun onBillingServiceDisconnected() {
                // 재연결 로직 생략
            }
        })
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

                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

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
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )

                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                billingClient.launchBillingFlow(activity, flowParams)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (purchase.products.contains(BillingConstants.PRODUCT_ID_ONE_TIME)) {
                // 일회성 구매: 소모 처리 후 30일 연장
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.consumeAsync(consumeParams) { billingResult, _ ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        scope.launch {
                            val oneMonthMillis = 30L * 24 * 60 * 60 * 1000
                            val expiryTime = System.currentTimeMillis() + oneMonthMillis
                            dataStore.saveAdFreeUntil(expiryTime)
                        }
                    }
                }
            } else if (purchase.products.contains(BillingConstants.PRODUCT_ID_SUBSCRIPTION)) {
                // 정기 구독: 승인 처리 후 상태 저장
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()

                    billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            scope.launch {
                                dataStore.saveSubscriptionActive(true)
                            }
                        }
                    }
                } else {
                    scope.launch {
                        dataStore.saveSubscriptionActive(true)
                    }
                }
            }
        }
    }

    private fun checkPurchases() {
        // 1. 구독 확인
        val subParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        billingClient.queryPurchasesAsync(subParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val isSubActive = purchases.any { 
                    it.purchaseState == Purchase.PurchaseState.PURCHASED && 
                    it.products.contains(BillingConstants.PRODUCT_ID_SUBSCRIPTION)
                }
                scope.launch { dataStore.saveSubscriptionActive(isSubActive) }
            }
        }

        // 2. 일회성 구매 중 아직 소모되지 않은 것이 있는지 확인
        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(inAppParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && 
                        purchase.products.contains(BillingConstants.PRODUCT_ID_ONE_TIME)) {
                        handlePurchase(purchase)
                    }
                }
            }
        }
    }
}
