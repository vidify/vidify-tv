package com.glowapps.vidify.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.glowapps.vidify.R
import com.glowapps.vidify.billing.BillingSystem
import com.glowapps.vidify.model.Purchasable

class SubscribeFragment : Fragment() {
    private lateinit var billingSystem: BillingSystem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        billingSystem = BillingSystem(activity!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.mobile_fragment_subscribe, container, false)
        val button = root.findViewById<Button>(R.id.subscribe_button)
        // The subscribe button will be disabled for already subscribed users
        if (billingSystem.isActive(Purchasable.SUBSCRIBE)) {
            disableSubscribeButton(button)
        } else {
            button.setOnClickListener { billingSystem.promptPurchase(activity!!, Purchasable.SUBSCRIBE) }
        }
        return root
    }

    private fun disableSubscribeButton(button: Button) {
        button.text = getString(R.string.section_subscribe_button_disabled)
        button.setCompoundDrawablesWithIntrinsicBounds(
            activity!!.getDrawable(R.drawable.icon_subscribe_done),
            null,
            null,
            null
        )
        button.isEnabled = false
    }
}
