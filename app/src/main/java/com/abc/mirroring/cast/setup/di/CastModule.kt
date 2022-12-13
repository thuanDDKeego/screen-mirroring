package com.abc.mirroring.cast.setup.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.abc.mirroring.cast.shared.cast.Caster
import javax.inject.Singleton

//@Module
//@InstallIn(SingletonComponent::class)
//class CastModule {
//    @Provides
//    @Singleton
//    fun caster(@ApplicationContext context: Context): Caster = Caster(context)
//
////    @Provides
////    @Singleton
////    fun discovery(@ApplicationContext context: Context): DeviceDiscovery = DeviceDiscovery()
//}