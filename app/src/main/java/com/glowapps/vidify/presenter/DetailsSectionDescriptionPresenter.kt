package com.glowapps.vidify.presenter

import com.glowapps.vidify.model.DetailsSection

class DetailsSectionDescriptionPresenter : AbstractDetailsDescriptionPresenterNoEllipsis () {
    override fun onBindDescription(viewHolder: ViewHolder, item: Any) {
        val data = item as DetailsSection
        viewHolder.apply {
            title.text = data.title
            subtitle.text = data.subtitle
            body.text = data.description
        }
    }
}
