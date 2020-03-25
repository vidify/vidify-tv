package com.glowapps.vidify.misc

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.widget.*
import com.glowapps.vidify.R

class DetailViewFragment(
    private val contentTitle: String,
    private val contentDescription: String,
    private val contentImage: Drawable
) : DetailsSupportFragment() {
    companion object {
        const val TAG = "DetailViewFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "$contentTitle, $contentDescription, $contentImage")

        title = contentTitle
        val rowPresenter: FullWidthDetailsOverviewRowPresenter =
            object : FullWidthDetailsOverviewRowPresenter(
                DetailsDescriptionPresenter(activity)
            ) {
                override fun createRowViewHolder(parent: ViewGroup): RowPresenter.ViewHolder { // Customize Actionbar and Content by using custom colors.
                    val viewHolder = super.createRowViewHolder(parent)
                    val actionsView =
                        viewHolder.view.findViewById<View>(R.id.details_overview_actions_background)
                    actionsView.setBackgroundColor(activity!!.resources.getColor(R.color.detail_view_actionbar_background))
                    val detailsView =
                        viewHolder.view.findViewById<View>(R.id.details_frame)
                    detailsView.setBackgroundColor(
                        resources.getColor(R.color.detail_view_background)
                    )
                    return viewHolder
                }
            }

        val mHelper =
            FullWidthDetailsOverviewSharedElementHelper()
        mHelper.setSharedElementEnterTransition(
            activity,
            androidx.leanback.leanbackshowcase.app.details.DetailViewExampleFragment.TRANSITION_NAME
        )
        rowPresenter.setListener(mHelper)
        rowPresenter.isParticipatingEntranceTransition = false
        prepareEntranceTransition()

        val shadowDisabledRowPresenter = ListRowPresenter()
        shadowDisabledRowPresenter.shadowEnabled = false

        // Setup PresenterSelector to distinguish between the different rows.
        // Setup PresenterSelector to distinguish between the different rows.
        val rowPresenterSelector = ClassPresenterSelector()
        rowPresenterSelector.addClassPresenter(DetailsOverviewRow::class.java, rowPresenter)
        rowPresenterSelector.addClassPresenter(CardListRow::class.java, shadowDisabledRowPresenter)
        rowPresenterSelector.addClassPresenter(ListRow::class.java, ListRowPresenter())
        mRowsAdapter = ArrayObjectAdapter(rowPresenterSelector)

        // Setup action and detail row.
        // Setup action and detail row.
        val detailsOverview = DetailsOverviewRow(data)
        var imageResId: Int = data.getLocalImageResourceId(activity)

        val extras = activity!!.intent.extras
        if (extras != null && extras.containsKey(androidx.leanback.leanbackshowcase.app.details.DetailViewExampleFragment.EXTRA_CARD)) {
            imageResId = extras.getInt(
                androidx.leanback.leanbackshowcase.app.details.DetailViewExampleFragment.EXTRA_CARD,
                imageResId
            )
        }
        detailsOverview.imageDrawable = resources.getDrawable(imageResId, null)
        val actionAdapter = ArrayObjectAdapter()

        mActionBuy = Action(
            androidx.leanback.leanbackshowcase.app.details.DetailViewExampleFragment.ACTION_BUY,
            getString(R.string.action_buy) + data.getPrice()
        )
        mActionWishList = Action(
            androidx.leanback.leanbackshowcase.app.details.DetailViewExampleFragment.ACTION_WISHLIST,
            getString(R.string.action_wishlist)
        )
        mActionRelated = Action(
            androidx.leanback.leanbackshowcase.app.details.DetailViewExampleFragment.ACTION_RELATED,
            getString(R.string.action_related)
        )

        actionAdapter.add(mActionBuy)
        actionAdapter.add(mActionWishList)
        actionAdapter.add(mActionRelated)
        detailsOverview.actionsAdapter = actionAdapter
        mRowsAdapter.add(detailsOverview)
    }
}