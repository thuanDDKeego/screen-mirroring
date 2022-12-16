package com.abc.mirroring.di

import android.content.Context
import android.net.Uri
import com.abc.mirroring.ads.AdmobHelper
import com.abc.mirroring.cast.shared.cast.Caster
import com.abc.mirroring.cast.shared.utils.GsonParser
import com.abc.mirroring.cast.shared.utils.UriTypeAdapter
import com.abc.mirroring.config.AppConfigRemote
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PurchasesUpdatedListener
import com.google.api.Billing
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.sofi.ads.AdCenter
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providesAdmobHelper(): AdmobHelper = AdmobHelper()

    @Singleton
    @Provides
    fun providesAdCenter(): AdCenter = AdCenter.getInstance()

    @Singleton
    @Provides
    fun providesAppConfigRemote(): AppConfigRemote = AppConfigRemote()

    @Provides
    @Singleton
    fun caster(@ApplicationContext context: Context): Caster = Caster(context)

    @Provides
    fun provideGson(): Gson = GsonBuilder().registerTypeAdapter(Uri::class.java, UriTypeAdapter).create()

    @Provides
    fun provideGsonParser(gson: Gson): GsonParser {
        return GsonParser(gson)
    }
}