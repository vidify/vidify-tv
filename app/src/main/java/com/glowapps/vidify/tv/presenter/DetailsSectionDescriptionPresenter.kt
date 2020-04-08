package com.glowapps.vidify.tv.presenter

import com.glowapps.vidify.tv.model.DetailsSection

class DetailsSectionDescriptionPresenter : AbstractDetailsDescriptionPresenterNoEllipsis() {
    override fun onBindDescription(viewHolder: ViewHolder, item: Any) {
        val data = item as DetailsSection
        viewHolder.apply {
            title.text = data.title
            subtitle.text = data.subtitle
            body.text = data.description
        }
    }
}
