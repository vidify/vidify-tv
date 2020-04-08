package com.glowapps.vidify.tv

import android.os.Bundle
import android.util.Log
import com.glowapps.vidify.R
import com.glowapps.vidify.tv.model.DetailsSection

class DetailsSectionActivity : BaseTVActivity() {
    companion object {
        const val TAG = "DetailsSectionActivity"
        const val DATA_INTENT_ARG = "data_intent_arg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tv_details_fragment)

        if (savedInstanceState == null) {
            Log.i(TAG, "Passing section parameters to the section fragment")
            val params: DetailsSection = intent.getParcelableExtra(DATA_INTENT_ARG)!!
            val details = DetailsSectionFragment()
            val bundle = Bundle()
            bundle.putParcelable(DetailsSectionFragment.DATA_BUNDLE_ARG, params)
            details.arguments = bundle

            supportFragmentManager.beginTransaction().add(R.id.details_fragment, details).commit()
        }
    }
}
