package com.abc.sreenmirroring.ui.selectLanguage

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abc.sreenmirroring.ads.AdmobHelper
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.databinding.ActivitySelectLanguageBinding
import com.abc.sreenmirroring.ui.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SelectLanguageActivity : BaseActivity<ActivitySelectLanguageBinding>() {

    @Inject
    lateinit var admobHelper: AdmobHelper

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, SelectLanguageActivity::class.java)
        }
    }

    private val listLanguage =
        arrayListOf(
            SelectLanguageViewState("English", "en"),
            SelectLanguageViewState("Vietnamese", "vi")
        )

    private var localeSelected: Locale? = dLocale
    private val selectLanguageAdapter: SelectLanguageAdapter =
        SelectLanguageAdapter(listLanguage, dLocale)

    override fun initBinding() = ActivitySelectLanguageBinding.inflate(layoutInflater)

    override fun initViews() {
        selectLanguageAdapter.itemClicked = {
            onSelectItem(it)
        }
        binding.rcvLanguages.adapter = selectLanguageAdapter
        binding.rcvLanguages.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    override fun initAdmob() {
        admobHelper.loadAdBanner(binding.adBannerLanguage.adView)
    }

    private fun onSelectItem(item: SelectLanguageViewState) {
        localeSelected = Locale(item.locale)
        showDialogConfirm()
    }

    private fun onChangeLanguage() {
        dLocale = localeSelected
        startActivity(HomeActivity.newIntent(this))
        finish()
    }

    private fun showDialogConfirm() {
        AlertDialog.Builder(this)
            .setTitle("Language Change")
            .setMessage("To change the language you need to restart the app. Do you want to continue?")
            .setPositiveButton(android.R.string.yes,
                DialogInterface.OnClickListener { dialog, which ->
                    Timber.d("onPress Confirm")
                    onChangeLanguage()
                })
            .setNegativeButton(android.R.string.no, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    override fun initActions() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }
}