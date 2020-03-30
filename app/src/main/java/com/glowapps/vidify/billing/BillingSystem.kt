package com.glowapps.vidify.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.glowapps.vidify.model.Purchasable

data class PurchasableData(
    val type: Purchasable,
    val product_id: String,
    var skuDetails: SkuDetails?
)

val purchasables = listOf(
    PurchasableData(
        Purchasable.SUBSCRIBE,
        "full_app",
        null
    )
)

// Abstraction class used to prompt and query purchasable items
class BillingSystem(private val context: Context) : PurchasesUpdatedListener {
    companion object {
        private const val TAG = "BillingSystem"
    }

    private lateinit var billingClient: BillingClient

    init {
        initClient()
    }

    private fun initClient() {
        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready.
                    Log.i(TAG, "Billing connection successful")
                    loadSKUs()
                } else {
                    Log.e(TAG, "Billing connection unsuccessful: ${billingResult.responseCode}")
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to Google Play by calling
                // the startConnection() method.
                Log.e(TAG, "Billing connection closed")
            }
        })
    }

    private fun loadSKUs() {
        if (!billingClient.isReady) {
            Log.e(TAG, "Billing client not ready")
            return
        }

        // Getting a list with the purchasable IDs and initializing all of them
        val skuList = mutableListOf<String>()
        for (p in purchasables) {
            skuList.add(p.product_id)
        }

        val params = SkuDetailsParams
            .newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)
            .build()

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            // Process the result.
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.e(
                    TAG, "Billing result unsuccessful:" +
                        " ${billingResult.responseCode} ${billingResult.debugMessage}")
                return@querySkuDetailsAsync
            }

            if (skuDetailsList.isEmpty()) {
                Log.e(TAG, "skuDetailsList is empty")
                return@querySkuDetailsAsync
            }

            // Adding the SkuDetails structure to every element in the purchasables list so
            // that they can be used later.
            for (skuDetails in skuDetailsList) {
                for (p in purchasables) {
                    if (skuDetails.sku == p.product_id) {
                        Log.i(TAG, "Initialized $p")
                        p.skuDetails = skuDetails
                    }
                }
            }
        }
    }

    // Checks if an item is active (as in, it was purchased).
    fun isActive(purchasable: Purchasable): Boolean {
        val data: PurchasableData? = purchasables.find{ p -> p.type == purchasable }
        val purchasesResult: Purchase.PurchasesResult =
            billingClient.queryPurchases(BillingClient.SkuType.INAPP)

        Log.i(TAG, "${purchasesResult.purchasesList}")
        return false
    }

    // Attempting to prompt the user to purchase an item.
    fun promptPurchase(activity: Activity, purchasable: Purchasable) {
        val data: PurchasableData? = purchasables.find{ p -> p.type == purchasable }

        // If the provided purchasable isn't found in the initialized list, nothing is done.
        if (data == null) {
            Log.e(TAG, "Purchasable $purchasable not found")
            return
        }

        // If the data is initialized, the user is prompted.
        if (data.skuDetails == null) {
            Log.e(TAG, "skuDetails for $purchasable is uninitialized")
        } else {
            val billingFlowParams = BillingFlowParams
                .newBuilder()
                .setSkuDetails(data.skuDetails)
                .build()
            billingClient.launchBillingFlow(activity, billingFlowParams)
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult?,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                acknowledgePurchase(purchase.purchaseToken)
            }
        } else if (billingResult?.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Log.e(TAG, "User cancelled purchase")
        } else {
            // Handle any other error codes.
            Log.e(TAG, "Unexpected ${billingResult?.responseCode}")
        }
    }

    private fun acknowledgePurchase(purchaseToken: String) {
        Log.e(TAG, "Purchase acknowledged")

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { billingResult ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage
            Log.i(TAG, "Acknowledgement: $responseCode $debugMessage")
        }
    }
}