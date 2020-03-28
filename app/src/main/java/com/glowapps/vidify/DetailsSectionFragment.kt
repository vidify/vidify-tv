package com.glowapps.vidify

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.app.DetailsSupportFragmentBackgroundController
import androidx.leanback.widget.*
import com.glowapps.vidify.model.DetailsSection
import com.glowapps.vidify.model.DetailsSectionButtonAction
import com.glowapps.vidify.model.DetailsSectionCard
import com.glowapps.vidify.presenter.DetailsSectionDescriptionPresenter
import com.glowapps.vidify.util.getBackground
import com.glowapps.vidify.util.isTV

class DetailsSectionFragment : DetailsSupportFragment() {
    companion object {
        const val TAG = "DetailsSectionFragment"
        const val DATA_BUNDLE_ARG = "data_bundle_arg"
    }

    private lateinit var data: DetailsSection
    private lateinit var rowsAdapter: ArrayObjectAdapter
    private lateinit var backgroundController: DetailsSupportFragmentBackgroundController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initAdapters()
        setupElements()
        initListeners()
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
            Log.i(TAG, "creating section with data: $data")

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
        onItemViewClickedListener = ItemViewClickedListener()
    }

    private class ItemViewClickedListener : OnItemViewClickedListener {

        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder?, item: Any,
            rowViewHolder: RowPresenter.ViewHolder?, row: Row?
        ) {
            val action = item as Action
            Log.i(TAG, "Item clicked: $action")

            // TODO
            when (action.id) {
                DetailsSectionButtonAction.DONATE_1.id -> {}
                DetailsSectionButtonAction.DONATE_5.id -> {}
                DetailsSectionButtonAction.DONATE_15.id -> {}
                DetailsSectionButtonAction.DONATE_50.id -> {}
                else -> Log.e(TAG, "Unknown item action clicked ${action.id}")
            }
        }
    }
}