package com.glowapps.vidify.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.glowapps.vidify.R
import com.glowapps.vidify.util.share

class ShareFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        share(activity!!)
    }
}
