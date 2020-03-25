package com.glowapps.vidify.misc

import android.os.Bundle
import com.glowapps.vidify.R
import com.glowapps.vidify.TVActivity


class DetailViewActivity : TVActivity() {
    companion object {
        // Extra parameter names
        const val TITLE_ARG = "title_arg"
        const val DESCRIPTION_ARG = "description_arg"
        const val IMAGE_ARG = "image_arg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_view_fragment)

        if (savedInstanceState == null) {
            // Obtaining the extra parameters
            val details = DetailViewFragment(
                intent.getStringExtra("title")!!,
                intent.getStringExtra("description")!!,
                getDrawable(intent.getIntExtra("image", -1))!!
            )
            supportFragmentManager.beginTransaction().add(
                R.id.content, details
            ).commit()
        }
    }
}
