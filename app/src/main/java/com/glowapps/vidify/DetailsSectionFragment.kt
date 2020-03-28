package com.glowapps.vidify

import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.widget.*
import com.glowapps.vidify.model.DetailsSection
import com.glowapps.vidify.model.DetailsSectionAction
import com.glowapps.vidify.presenter.DetailsSectionDescriptionPresenter

class DetailsSectionFragment : DetailsSupportFragment() {
    companion object {
        const val TAG = "DetailsSectionFragment"
        const val DATA_BUNDLE_ARG = "data_bundle_arg"
    }

    private lateinit var data: DetailsSection
    private lateinit var rowsAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate: $arguments")
        super.onCreate(savedInstanceState)

        val selector = ClassPresenterSelector().apply {
            // Attach your media item details presenter to the row presenter:
            FullWidthDetailsOverviewRowPresenter(DetailsSectionDescriptionPresenter()).also {
                addClassPresenter(DetailsOverviewRow::class.java, it)
            }
            addClassPresenter(ListRow::class.java, ListRowPresenter())
        }

        rowsAdapter = ArrayObjectAdapter(selector)
        adapter = rowsAdapter

        if (arguments != null) {
            data = arguments!!.getParcelable(DATA_BUNDLE_ARG)!!
            Log.i(TAG, "data: $data")

            // TODO add actions to DetailsSection
            val actionAdapter = ArrayObjectAdapter().apply {
                add(Action(1, "Buy $9.99"))
                add(Action(2, "Rent $2.99"))
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