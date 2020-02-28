package com.glowapps.vidify

import android.os.Bundle
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.*


class MainFragment : VerticalGridSupportFragment() {
    companion object {
        private const val NUM_COLUMNS = 5
    }

    private lateinit var mAdapter: Adapter

    private class Adapter(presenter: CardPresenter?) : ArrayObjectAdapter(presenter) {
        fun callNotifyChanged() {
            super.notifyChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAdapter = Adapter(CardPresenter())
        adapter = mAdapter
        title = "TEST TITLE"
        if (savedInstanceState == null) {
            prepareEntranceTransition()
        }
        setupFragment()
    }

    private fun setupFragment() {
        val gridPresenter = VerticalGridPresenter()
        gridPresenter.numberOfColumns = NUM_COLUMNS
        setGridPresenter(gridPresenter)

        /* TODO: Search not implemented yet
        setOnSearchClickedListener {
            val intent = Intent(activity, SearchActivity::class.java)
            startActivity(intent)
        }
        */

        onItemViewClickedListener = ItemViewClickedListener()
        setOnItemViewSelectedListener(ItemViewSelectedListener())
    }

    private class ItemViewClickedListener : OnItemViewClickedListener {
        // Called when a user clicks on a item, which should be a Device structure.
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder, item: Any,
            rowViewHolder: RowPresenter.ViewHolder, row: Row
        ) {
            if (item is Device) {
                val device: Device = item as Device
                /* TODO: Details activity not implemented yet
                val intent = Intent(getActivity(), VideoDetailsActivity::class.java)
                intent.putExtra(VideoDetailsActivity.VIDEO, video)
                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    getActivity(),
                    (itemViewHolder.view as ImageCardView).mainImageView,
                    VideoDetailsActivity.SHARED_ELEMENT_NAME
                ).toBundle()
                getActivity().startActivity(intent, bundle)
                 */
            }
        }
    }

    private class ItemViewSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(itemViewHolder: Presenter.ViewHolder, item: Any,
                                    rowViewHolder: RowPresenter.ViewHolder, row: Row) {
        }
    }
}
