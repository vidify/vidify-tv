package com.glowapps.vidify.mobile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.glowapps.vidify.R
import com.glowapps.vidify.billing.BillingSystem
import com.glowapps.vidify.model.Purchasable

class SubscribeFragment : Fragment() {
    private lateinit var billingSystem: BillingSystem

    companion object {
        const val TAG = "SubscribeFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        billingSystem = BillingSystem(activity!!)
        billingSystem.purchasesList.observe(this, Observer {
            it?.let {
                for (purchase in it) {
                    // The subscribe button will be disabled for already subscribed users
                    if (purchase.sku == Purchasable.SUBSCRIBE.sku) {
                        val button = activity!!.findViewById<Button>(R.id.subscribe_button)
                        Log.i(TAG, "Updating purchase status to disabled")
                        disableSubscribeButton(button)
                    }
                }
            }
        })
        billingSystem.init()
    }

    override fun onDestroy() {
        billingSystem.destroy()

        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "Creating view")
        val root = inflater.inflate(R.layout.mobile_fragment_subscribe, container, false)
        val button: Button = root.findViewById(R.id.subscribe_button)
        enableSubscribeButton(button)
        return root
    }

    private fun enableSubscribeButton(button: Button) {
        button.setOnClickListener {
            billingSystem.promptPurchase(activity!!, Purchasable.SUBSCRIBE)
        }
        button.text = getString(R.string.section_subscribe_button)
        button.setCompoundDrawablesWithIntrinsicBounds(
            activity!!.getDrawable(R.drawable.icon_subscribe_small),
            null,
            null,
            null
        )
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

    private fun purchaseUpdate(p: String, isActive: Boolean) {
    }
}
