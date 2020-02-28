package com.glowapps.vidify

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.leanback.app.OnboardingSupportFragment
import androidx.preference.PreferenceManager
import java.util.*

class OnboardingFragment : OnboardingSupportFragment() {
    private val pageImages = intArrayOf(
        R.drawable.pic1,
        R.drawable.pic2,
        R.drawable.pic3
    )
    private var mContentAnimator: Animator? = null
    private var mContentView: ImageView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { // Set the logo to display a splash animation
        logoResourceId = R.mipmap.logo
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onFinishFragment() {
        super.onFinishFragment()
        // User has seen OnboardingSupportFragment, so mark our SharedPreferences
        // flag as completed so that we don't show our OnboardingSupportFragment
        // the next time the user launches the app.
        PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
            putBoolean(COMPLETED_ONBOARDING_PREF_NAME, true)
            apply()
        }
    }

    override fun getPageCount(): Int {
        return pageTitles.size
    }

    override fun getPageTitle(pageIndex: Int): String {
        return getString(pageTitles[pageIndex])
    }

    override fun getPageDescription(pageIndex: Int): String {
        return getString(pageDescriptions[pageIndex])
    }

    override fun onCreateBackgroundView(inflater: LayoutInflater, container: ViewGroup): View? {
        val bgView = View(activity)
        bgView.setBackgroundColor(resources.getColor(R.color.fastlane_background))
        return bgView
    }

    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup): View? {
        mContentView = ImageView(activity)
        mContentView!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
        mContentView!!.setPadding(0, 32, 0, 32)
        return mContentView
    }

    override fun onCreateForegroundView(inflater: LayoutInflater, container: ViewGroup): View? {
        return null
    }

    override fun onPageChanged(newPage: Int, previousPage: Int) {
        if (mContentAnimator != null) {
            mContentAnimator!!.end()
        }
        val animators = ArrayList<Animator>()
        val fadeOut = createFadeOutAnimator(mContentView)
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mContentView!!.setImageDrawable(resources.getDrawable(pageImages[newPage]))
                (mContentView!!.drawable as AnimationDrawable).start()
            }
        })
        animators.add(fadeOut)
        animators.add(createFadeInAnimator(mContentView))
        val set = AnimatorSet()
        set.playSequentially(animators)
        set.start()
        mContentAnimator = set
    }

    override fun onCreateEnterAnimation(): Animator? {
        mContentView!!.setImageDrawable(resources.getDrawable(pageImages[0]))
        (mContentView!!.drawable as AnimationDrawable).start()
        mContentAnimator = createFadeInAnimator(mContentView)
        return mContentAnimator
    }

    private fun createFadeInAnimator(view: View?): Animator {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 0.0f, 1.0f).setDuration(ANIMATION_DURATION)
    }

    private fun createFadeOutAnimator(view: View?): Animator {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 1.0f, 0.0f).setDuration(ANIMATION_DURATION)
    }

    companion object {
        const val COMPLETED_ONBOARDING_PREF_NAME = "completed_onboarding"
        private val pageTitles = intArrayOf(
            R.string.onboarding_title_one,
            R.string.onboarding_title_two,
            R.string.onboarding_title_three
        )
        private val pageDescriptions = intArrayOf(
            R.string.onboarding_description_one,
            R.string.onboarding_description_two,
            R.string.onboarding_description_three
        )
        private const val ANIMATION_DURATION: Long = 500
    }
}
