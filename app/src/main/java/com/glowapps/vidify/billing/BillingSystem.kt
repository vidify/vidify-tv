package com.glowapps.vidify.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.glowapps.vidify.model.Purchasable

data class PurchasableData(
    val id: Purchasable,
    var skuDetails: SkuDetails?
)

// Abstraction class used to prompt and query subscriptions
class BillingSystem(private val context: Context) : PurchasesUpdatedListener,
        BillingClientStateListener {

    companion object {
        private const val TAG = "BillingSystem"
    }

    // Purchases are observable. This list will be updated when the Billing Library
    // detects new or existing purchases. All observers will be notified.
    val purchasesList = MutableLiveData<List<Purchase>>()

    // SkuDetails for all known SKUs.
    val skusWithSkuDetails = MutableLiveData<Map<String, SkuDetails>>()

    private lateinit var billingClient: BillingClient

    fun init() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        if (!billingClient.isReady) {
            billingClient.startConnection(this)
        }
    }

    // The BillingClient can only be once, so this'll have to create a new instance when
    // starting again.
    fun destroy() {
        if (billingClient.isReady) {
            Log.i(TAG, "Ending connection with billing client")
            billingClient.endConnection()
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // The BillingClient is ready.
            Log.i(TAG, "Billing connection successful")
            loadSKUs()
            queryPurchases()
        } else {
            Log.e(TAG, "Billing connection unsuccessful: ${billingResult.responseCode},"
                    + billingResult.debugMessage)
        }
    }

    override fun onBillingServiceDisconnected() {
        // Try to restart the connection on the next request to Google Play by calling
        // the startConnection() method.
        Log.e(TAG, "Billing connection closed")
    }

    private fun loadSKUs() {
        if (!billingClient.isReady) {
            Log.e(TAG, "loadSKUs: Billing client not ready")
            return
        }

        // Getting a list with the purchasable IDs and initializing all of them
        val skuList = mutableListOf<String>()
        for (p in Purchasable.values()) {
            skuList.add(p.sku)
        }

        val params = SkuDetailsParams
            .newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.SUBS)
            .build()

        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.e(TAG, "loadSKUs: Billing result unsuccessful:" +
                        " ${billingResult.responseCode} ${billingResult.debugMessage}")
                return@querySkuDetailsAsync
            }

            if (skuDetailsList == null || skuDetailsList.isEmpty()) {
                Log.e(TAG, "loadSKUs: skuDetailsList is empty or null")
                skusWithSkuDetails.postValue(emptyMap())
                return@querySkuDetailsAsync
            }

            // Adding the SkuDetails structure to every element in the purchasables list so
            // that they can be used later.
            skusWithSkuDetails.postValue(HashMap<String, SkuDetails>().apply {
                for (details in skuDetailsList) {
                    put(details.sku, details)
                }
            }.also { postedValue ->
                Log.i(TAG, "loadSKUs: SkuDetails query response successful, count ${postedValue.size}")
            })
        }
    }

    private fun queryPurchases() {
        if (!billingClient.isReady) {
            Log.e(TAG, "queryPurchases: Billing client not ready")
            return
        }

        val result: Purchase.PurchasesResult? =
            billingClient.queryPurchases(BillingClient.SkuType.SUBS)

        if (result == null) {
            Log.e(TAG, "queryPurchases: Null result")
        } else {
            if (result.purchasesList == null) {
                Log.e(TAG, "queryPurchases: Null purchases list")
            } else {
                Log.i(TAG, "queryPurchases: Query successful, updating"
                    + "${result.purchasesList?.size}")
                purchasesList.postValue(result.purchasesList)
            }
        }
    }

    // Attempting to prompt the user to purchase an item.
    fun promptPurchase(activity: Activity, purchasable: Purchasable) {
        if (!billingClient.isReady) {
            Log.e(TAG, "Billing client not ready for promptPurchase")
            return
        }

        val data: SkuDetails? = skusWithSkuDetails.value?.get(purchasable.sku)

        // If the provided purchasable isn't found in the initialized list, nothing is done.
        if (data == null) {
            Log.e(TAG, "Purchasable $purchasable not found")
            return
        }

        Log.i(TAG, "Launching Billing flow activity")
        val billingFlowParams = BillingFlowParams
            .newBuilder()
            .setSkuDetails(data)
            .build()
        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            if (purchases == null) {
                Log.e(TAG, "onPurchasesUpdated: Null purchases")
                purchasesList.postValue(null)
            } else {
                Log.i(TAG, "onPurchasesUpdated: Successful purchase update")
                for (purchase in purchases) {
                    Log.i(TAG, "onPurchasesUpdated: Update for ${purchase.sku} sent")
                    acknowledgePurchase(purchase.purchaseToken)
                }
                purchasesList.postValue(purchases)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Log.e(TAG, "onPurchasesUpdated: User cancelled purchase")
        } else {
            // Handle any other error codes.
            Log.e(TAG, "onPurchasesUpdated: Unexpected ${billingResult.responseCode}")
        }
    }

    private fun acknowledgePurchase(purchaseToken: String) {
        Log.i(TAG, "Purchase acknowledged")

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { billingResult ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage
            Log.i(TAG, "Acknowledgement response: $responseCode $debugMessage")
        }
    }
}