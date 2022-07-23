package com.abc.sreenmirroring.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.config.AppPreferences
import com.abc.sreenmirroring.databinding.ActivitySettingBinding
import com.abc.sreenmirroring.databinding.LayoutDialogChangePinCodeBinding
import timber.log.Timber

class SettingActivity : BaseActivity<ActivitySettingBinding>() {

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, SettingActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun initBinding() = ActivitySettingBinding.inflate(layoutInflater)

    override fun initViews() {
        binding.switchOnOffPinCode.isChecked = AppPreferences().isTurnOnPinCode == true
        binding.txtPinCode.text = AppPreferences().pinCode
    }

    override fun initActions() {
        binding.switchOnOffPinCode.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences().isTurnOnPinCode = isChecked
        }
        binding.llChangePinCode.setOnClickListener {
            if (AppPreferences().isTurnOnPinCode == true) {
                val dialog = LayoutDialogChangePinCodeBinding.inflate(layoutInflater, binding.root, true)
                dialog.btnSaveNewPinCode.setOnClickListener {
                    AppPreferences().pinCode = dialog.pinView.text.toString()
                    binding.txtPinCode.text = AppPreferences().pinCode
                    dialog.root.visibility = View.INVISIBLE
                    hideKeyboard(this)
                }
                dialog.bgDialog.setOnClickListener {
                    hideKeyboard(this)
                    dialog.root.visibility = View.INVISIBLE
                }
            }
        }
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

    }

    private fun hideKeyboard(context: Context) {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val v = (context as Activity).currentFocus ?: return
        inputManager.hideSoftInputFromWindow(v.windowToken, 0)
    }

}