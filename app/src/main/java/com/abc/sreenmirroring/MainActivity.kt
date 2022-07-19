package com.abc.sreenmirroring

import android.view.Menu
import android.view.MenuItem
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun initBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun initViews() {
    }

    override fun initActions() {
    }
}