package com.glowapps.vidify

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.app.DetailsSupportFragmentBackgroundController
import androidx.leanback.widget.*
import com.glowapps.vidify.model.DetailsSection
import com.glowapps.vidify.presenter.DetailsSectionDescriptionPresenter
import com.glowapps.vidify.util.getBackground

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

        // Initializing the adapters
        val selector = ClassPresenterSelector().apply {
            FullWidthDetailsOverviewRowPresenter(DetailsSectionDescriptionPresenter()).also {
                addClassPresenter(DetailsOverviewRow::class.java, it)
            }
            addClassPresenter(ListRow::class.java, ListRowPresenter())
        }
        rowsAdapter = ArrayObjectAdapter(selector)
        adapter = rowsAdapter

        // Setting the background
        backgroundController = DetailsSupportFragmentBackgroundController(this).apply {
            enableParallax()
            val options = BitmapFactory.Options()
            options.inScaled = false
            coverBitmap = BitmapFactory.decodeResource(resources, getBackground(activity!!), options)
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
}