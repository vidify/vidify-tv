package com.glowapps.vidify.tv

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.app.DetailsSupportFragmentBackgroundController
import androidx.leanback.widget.*
import com.glowapps.vidify.billing.BillingSystem
import com.glowapps.vidify.R
import com.glowapps.vidify.model.DetailsSection
import com.glowapps.vidify.model.DetailsSectionButtonAction
import com.glowapps.vidify.model.Purchasable
import com.glowapps.vidify.presenter.DetailsSectionDescriptionPresenter
import com.glowapps.vidify.util.share

class DetailsSectionFragment : DetailsSupportFragment(), OnItemViewClickedListener {
    companion object {
        const val TAG = "DetailsSectionFragment"
        const val DATA_BUNDLE_ARG = "data_bundle_arg"
    }

    private lateinit var data: DetailsSection
    private lateinit var rowsAdapter: ArrayObjectAdapter
    private lateinit var backgroundController: DetailsSupportFragmentBackgroundController

    // Some buttons need a billing client for in-app purchases and similar.
    private lateinit var billingSystem: BillingSystem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initAdapters()
        setupElements()
        initListeners()
        initBillingSystem()
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
                BitmapFactory.decodeResource(resources,
                    R.drawable.background, options)
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

    private fun initBillingSystem() {
        billingSystem = BillingSystem(context!!)
    }

    override fun onItemClicked(
        itemViewHolder: Presenter.ViewHolder?, item: Any,
        rowViewHolder: RowPresenter.ViewHolder?, row: Row?
    ) {
        val action = item as Action
        Log.i(TAG, "Item clicked: $action")

        // First checking the purchasable items
        when (action.id) {
            DetailsSectionButtonAction.SUBSCRIBE.id -> {
                billingSystem.promptPurchase(activity!!, Purchasable.SUBSCRIBE)
            }
            DetailsSectionButtonAction.SHARE.id -> {
                // Sharing on a television will open an activity with a QR code and more
                // details. On Android, the standard share menu will be shown.
                share(activity!!)
            }
            else -> {
                Log.e(TAG, "Unknown action ID: ${action.id}")
            }
        }
    }
}