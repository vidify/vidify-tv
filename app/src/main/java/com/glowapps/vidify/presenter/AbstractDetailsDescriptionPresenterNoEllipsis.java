package com.glowapps.vidify.presenter;

import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.leanback.widget.Presenter;

// Quick fix so that AbstractDetailsDescriptionPresenter doesn't use text ellipsis for the body
// description. See https://stackoverflow.com/q/55304143 for more.
public abstract class AbstractDetailsDescriptionPresenterNoEllipsis extends Presenter {
    public static class ViewHolder extends Presenter.ViewHolder {
        final TextView mTitle;
        final TextView mSubtitle;
        final TextView mBody;
        final int mTitleMargin;
        final int mUnderTitleBaselineMargin;
        final int mUnderSubtitleBaselineMargin;
        final int mTitleLineSpacing;
        final int mBodyLineSpacing;
        final int mBodyMaxLines;
        final int mBodyMinLines;
        final FontMetricsInt mTitleFontMetricsInt;
        final FontMetricsInt mSubtitleFontMetricsInt;
        final FontMetricsInt mBodyFontMetricsInt;
        final int mTitleMaxLines;

        public ViewHolder(final View view) {
            super(view);
            mTitle = (TextView) view.findViewById(androidx.leanback.R.id.lb_details_description_title);
            mSubtitle = (TextView) view.findViewById(androidx.leanback.R.id.lb_details_description_subtitle);
            mBody = (TextView) view.findViewById(androidx.leanback.R.id.lb_details_description_body);

            FontMetricsInt titleFontMetricsInt = getFontMetricsInt(mTitle);
            final int titleAscent = view.getResources().getDimensionPixelSize(
                    androidx.leanback.R.dimen.lb_details_description_title_baseline);
            // Ascent is negative
            mTitleMargin = titleAscent + titleFontMetricsInt.ascent;

            mUnderTitleBaselineMargin = view.getResources().getDimensionPixelSize(
                    androidx.leanback.R.dimen.lb_details_description_under_title_baseline_margin);
            mUnderSubtitleBaselineMargin = view.getResources().getDimensionPixelSize(
                    androidx.leanback.R.dimen.lb_details_description_under_subtitle_baseline_margin);

            mTitleLineSpacing = view.getResources().getDimensionPixelSize(
                    androidx.leanback.R.dimen.lb_details_description_title_line_spacing);
            mBodyLineSpacing = view.getResources().getDimensionPixelSize(
                    androidx.leanback.R.dimen.lb_details_description_body_line_spacing);

            mBodyMaxLines = view.getResources().getInteger(
                    androidx.leanback.R.integer.lb_details_description_body_max_lines);
            mBodyMinLines = view.getResources().getInteger(
                    androidx.leanback.R.integer.lb_details_description_body_min_lines);
            mTitleMaxLines = mTitle.getMaxLines();

            mTitleFontMetricsInt = getFontMetricsInt(mTitle);
            mSubtitleFontMetricsInt = getFontMetricsInt(mSubtitle);
            mBodyFontMetricsInt = getFontMetricsInt(mBody);
        }

        public TextView getTitle() {
            return mTitle;
        }

        public TextView getSubtitle() {
            return mSubtitle;
        }

        public TextView getBody() {
            return mBody;
        }

        private FontMetricsInt getFontMetricsInt(TextView textView) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setTextSize(textView.getTextSize());
            paint.setTypeface(textView.getTypeface());
            return paint.getFontMetricsInt();
        }
    }

    @Override
    public final AbstractDetailsDescriptionPresenterNoEllipsis.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(androidx.leanback.R.layout.lb_details_description, parent, false);
        return new AbstractDetailsDescriptionPresenterNoEllipsis.ViewHolder(v);
    }

    @Override
    public final void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        AbstractDetailsDescriptionPresenterNoEllipsis.ViewHolder vh = (AbstractDetailsDescriptionPresenterNoEllipsis.ViewHolder) viewHolder;
        onBindDescription(vh, item);

        boolean hasTitle = true;
        if (TextUtils.isEmpty(vh.mTitle.getText())) {
            vh.mTitle.setVisibility(View.GONE);
            hasTitle = false;
        } else {
            vh.mTitle.setVisibility(View.VISIBLE);
            vh.mTitle.setLineSpacing(vh.mTitleLineSpacing - vh.mTitle.getLineHeight()
                    + vh.mTitle.getLineSpacingExtra(), vh.mTitle.getLineSpacingMultiplier());
            vh.mTitle.setMaxLines(vh.mTitleMaxLines);
        }
        setTopMargin(vh.mTitle, vh.mTitleMargin);

        boolean hasSubtitle = true;
        if (TextUtils.isEmpty(vh.mSubtitle.getText())) {
            vh.mSubtitle.setVisibility(View.GONE);
            hasSubtitle = false;
        } else {
            vh.mSubtitle.setVisibility(View.VISIBLE);
            if (hasTitle) {
                setTopMargin(vh.mSubtitle, vh.mUnderTitleBaselineMargin
                        + vh.mSubtitleFontMetricsInt.ascent - vh.mTitleFontMetricsInt.descent);
            } else {
                setTopMargin(vh.mSubtitle, 0);
            }
        }

        if (TextUtils.isEmpty(vh.mBody.getText())) {
            vh.mBody.setVisibility(View.GONE);
        } else {
            vh.mBody.setVisibility(View.VISIBLE);
            vh.mBody.setLineSpacing(vh.mBodyLineSpacing - vh.mBody.getLineHeight()
                    + vh.mBody.getLineSpacingExtra(), vh.mBody.getLineSpacingMultiplier());

            if (hasSubtitle) {
                setTopMargin(vh.mBody, vh.mUnderSubtitleBaselineMargin
                        + vh.mBodyFontMetricsInt.ascent - vh.mSubtitleFontMetricsInt.descent);
            } else if (hasTitle) {
                setTopMargin(vh.mBody, vh.mUnderTitleBaselineMargin
                        + vh.mBodyFontMetricsInt.ascent - vh.mTitleFontMetricsInt.descent);
            } else {
                setTopMargin(vh.mBody, 0);
            }
        }
    }

    protected abstract void onBindDescription(AbstractDetailsDescriptionPresenterNoEllipsis.ViewHolder vh, Object item);

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {}

    private void setTopMargin(TextView textView, int topMargin) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) textView.getLayoutParams();
        lp.topMargin = topMargin;
        textView.setLayoutParams(lp);
    }
}
