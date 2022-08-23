package com.abc.mirroring.ui.selectLanguage

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abc.mirroring.base.BaseActivity
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.ActivitySelectLanguageBinding
import com.abc.mirroring.ui.home.HomeActivity
import java.util.*

class SelectLanguageActivity : BaseActivity<ActivitySelectLanguageBinding>() {

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

    private fun onSelectItem(item: SelectLanguageViewState) {
        dLocale = Locale(item.locale)
        AppPreferences().languageSelected = item.locale
        startActivity(HomeActivity.newIntent(this))
        finish()
        startActivity(newIntent(this))
    }

    override fun initActions() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }
}