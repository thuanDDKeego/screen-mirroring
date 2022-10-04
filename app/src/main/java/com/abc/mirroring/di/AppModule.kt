package com.abc.mirroring.di

import android.content.Context
import com.abc.mirroring.ads.AdmobHelper
import com.abc.mirroring.config.AppConfigRemote
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PurchasesUpdatedListener
import com.google.api.Billing
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import info.dvkr.screenstream.data.settings.Settings
import info.dvkr.screenstream.data.settings.SettingsImpl
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providesAdmobHelper(): AdmobHelper = AdmobHelper()

    @Singleton
    @Provides
    fun providesAppConfigRemote(): AppConfigRemote = AppConfigRemote()
}