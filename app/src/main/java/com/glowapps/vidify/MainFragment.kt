package com.glowapps.vidify

import android.os.Bundle
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.*


class MainFragment : VerticalGridSupportFragment() {
    companion object {
        private const val NUM_COLUMNS = 5
    }

    private lateinit var cardAdapter: ArrayObjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setting the card adapter, an interface used to manage the cards displayed in the
        // grid view. The cards are set up with a Device structure.
        cardAdapter = ArrayObjectAdapter(CardPresenter())
        // TODO: Use R.drawable.pic1 as an Int, rather than an example string.
        val dummyDevice = Device("Name", "description", "https://storage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202012_%20Year%20In%20Review/bg.jpg", "127.0.0.1", 32005)
        cardAdapter.add(dummyDevice)
        cardAdapter.add(dummyDevice)
        cardAdapter.add(dummyDevice)
        cardAdapter.add(dummyDevice)
        cardAdapter.add(dummyDevice)
        adapter = cardAdapter

        title = getString(R.string.app_name)
        if (savedInstanceState == null) {
            prepareEntranceTransition()
        }
        setupFragment()
    }

    private fun setupFragment() {
        // Including the presenter to manage the grid layout itself.
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