package com.glowapps.vidify.presenter

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter
import com.glowapps.vidify.model.DetailsSection

class DetailsSectionDescriptionPresenter : AbstractDetailsDescriptionPresenter() {
    override fun onBindDescription(viewHolder: ViewHolder, item: Any) {
        val data = item as DetailsSection
        viewHolder.apply {
            title.text = data.title
            subtitle.text = data.subtitle
            body.text = data.description
        }
    }
}
