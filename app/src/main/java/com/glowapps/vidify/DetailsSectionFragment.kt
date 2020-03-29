package com.glowapps.vidify

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.app.DetailsSupportFragmentBackgroundController
import androidx.leanback.widget.*
import com.android.billingclient.api.*
import com.glowapps.vidify.model.DetailsSection
import com.glowapps.vidify.model.DetailsSectionButtonAction
import com.glowapps.vidify.presenter.DetailsSectionDescriptionPresenter
import com.glowapps.vidify.util.getBackground

data class Purchasable(
    val button_type: DetailsSectionButtonAction,
    val product_id: String,
    var skuDetails: SkuDetails?
)

class DetailsSectionFragment : DetailsSupportFragment(), OnItemViewClickedListener,
        PurchasesUpdatedListener {
    companion object {
        const val TAG = "DetailsSectionFragment"
        const val DATA_BUNDLE_ARG = "data_bundle_arg"
    }

    private lateinit var data: DetailsSection
    private lateinit var rowsAdapter: ArrayObjectAdapter
    private lateinit var backgroundController: DetailsSupportFragmentBackgroundController

    // Some buttons need a billing client for in-app purchases and similar.
    private lateinit var billingClient: BillingClient
    private val purchasables = listOf(
        Purchasable(
            DetailsSectionButtonAction.DONATE_1,
            "donate_1",
            null
        ),
        Purchasable(
            DetailsSectionButtonAction.DONATE_5,
            "donate_5",
            null
        ),
        Purchasable(
            DetailsSectionButtonAction.DONATE_15,
            "donate_15",
            null
        ),
        Purchasable(
            DetailsSectionButtonAction.DONATE_50,
            "donate_50",
            null
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initAdapters()
        setupElements()
        initListeners()
        initBillingClient()
    }

    private fun initAdapters() {
        // Initializing the adapters
        val selector = ClassPresenterSelector().apply {
            FullWidthDetailsOverviewRowPresenter(DetailsSectionDescriptionPresenter()).also {
                addClassPresenter(DetailsOverviewRow::class.java, it)
            }
            addClassPresenter(ListRow::class.java, ListRowPresenter())
        }
        rowsAdapter = ArrayObjectAdapter(selector)
        adapter = rowsAdapter
    }

    private fun setupElements() {
        // Setting the background
        backgroundController = DetailsSupportFragmentBackgroundController(this).apply {
            enableParallax()
            val options = BitmapFactory.Options()
            options.inScaled = false
            coverBitmap =
                BitmapFactory.decodeResource(resources, getBackground(activity!!), options)
        }

        // Setting the elements given the DetailsSection data
        if (arguments != null) {
            data = arguments!!.getParcelable(DATA_BUNDLE_ARG)!!
            Log.i(TAG, "Creating '${data.title}' section")

            val actionAdapter = ArrayObjectAdapter().apply {
                if (data.actions != null) {
                    for (action in data.actions!!) {
                        add(Action(action.type.id, action.text))
                    }
                }
            }

            val detailsOverview = DetailsOverviewRow(data).apply {
                // Add images and action buttons to the details view
                imageDrawable = ContextCompat.getDrawable(activity!!, data.image)
                actionsAdapter = actionAdapter
            }
            rowsAdapter.add(detailsOverview)
        }
    }

    private fun initListeners() {
        onItemViewClickedListener = this
    }

    private fun initBillingClient() {
        billingClient = BillingClient.newBuilder(context!!)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
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
                Log.e(TAG, "Billing result unsuccessful: ${billingResult.responseCode}")
                return@querySkuDetailsAsync
            }

            if (skuDetailsList.isEmpty()) {
                Log.e(TAG, "skuDetailsList is empty")
                return@querySkuDetailsAsync
            }

            // Adding the SkuDetails structure to every element in the purchasables list so
            // that they can be used later.
            for (skuDetails in skuDetailsList) {
                Log.e(TAG, "AAAAAA: $skuDetails")
                for (p in purchasables) {
                    if (skuDetails.sku == p.product_id) {
                        Log.i(TAG, "Initialized $p")
                        p.skuDetails = skuDetails
                    }
                }
            }
        }
    }

    private fun promptPurchase(skuDetails: SkuDetails) {
        val billingFlowParams = BillingFlowParams
            .newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onItemClicked(
        itemViewHolder: Presenter.ViewHolder?, item: Any,
        rowViewHolder: RowPresenter.ViewHolder?, row: Row?
    ) {
        val action = item as Action
        Log.i(TAG, "Item clicked: $action")

        // First checking the purchasable items
        for (p in purchasables) {
            if (action.id == p.button_type.id) {
                if (p.skuDetails == null) {
                    Log.e(TAG, "skuDetails for ${p.button_type} is uninitialized")
                } else {
                    promptPurchase(p.skuDetails!!)
                }
                return
            }
        }

        // No other actions apart from purchasables for now.
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

        }
    }
}