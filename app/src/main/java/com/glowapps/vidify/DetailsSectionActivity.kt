package com.glowapps.vidify

import android.os.Bundle
import android.util.Log
import com.glowapps.vidify.model.DetailsSection


class DetailsSectionActivity : TVActivity() {
    companion object {
        const val TAG = "DetailsSectionActivity"
        // Extra parameter names
        const val DATA_INTENT_ARG = "data_intent_arg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.details_fragment)

        if (savedInstanceState == null) {
            // Obtaining the extra parameters and passing them to the fragment
            val params: DetailsSection = intent.getParcelableExtra(DATA_INTENT_ARG)!!
            val details = DetailsSectionFragment()
            val bundle = Bundle()
            bundle.putParcelable(DetailsSectionFragment.DATA_BUNDLE_ARG, params)
            details.arguments = bundle

            supportFragmentManager.beginTransaction().add(R.id.details_fragment, details).commit()
        }
    }
}
