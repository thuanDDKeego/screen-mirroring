package com.abc.mirroring.ui.premium

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.abc.mirroring.BuildConfig
import com.abc.mirroring.R
import com.abc.mirroring.base.BaseActivity
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.ActivitySubscriptionsBinding
import com.abc.mirroring.ui.policy.PolicyActivity
import com.abc.mirroring.utils.Global
import java.text.SimpleDateFormat
import java.util.*

class SubscriptionsActivity : BaseActivity<ActivitySubscriptionsBinding>() {
    private final val SUBSCRIPTION_URL = "http://play.google.com/store/account/subscriptions?package=${BuildConfig.APPLICATION_ID}&sku=${Global.SUB_PURCHASE_ID}"

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, SubscriptionsActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun initBinding(): ActivitySubscriptionsBinding =
        ActivitySubscriptionsBinding.inflate(layoutInflater)

    override fun initViews() {
        val expiryDate = PremiumUtils.getExpiryTime(AppPreferences().purchaseDate!!)
        val date = Date(expiryDate)
        val expiryDateFormat = SimpleDateFormat("MM/dd/yyyy")
        binding.txtExpiryDate.text = getString(R.string.expire_on, expiryDateFormat.format(date))

    }

    override fun initActions() {
        binding.apply {
            btnClose.setOnClickListener {
                onBackPressed()
            }
            txtCancel.setOnClickListener {
                openPlaystoreAccount()
            }
            txtTermAndPolicy.setOnClickListener {
                PolicyActivity.gotoActivity(this@SubscriptionsActivity)
            }
        }
    }


    private fun openPlaystoreAccount() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SUBSCRIPTION_URL)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Cant open the browser", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

}