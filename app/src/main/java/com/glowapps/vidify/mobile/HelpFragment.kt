package com.glowapps.vidify.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.glowapps.vidify.R
import com.glowapps.vidify.util.openURL

class HelpFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.mobile_fragment_help, container, false)

        val image: ImageView = root.findViewById(R.id.website_qrcode)
        image.setOnClickListener { openURL(activity!!, getString(R.string.website_link)) }

        return root
    }
}
