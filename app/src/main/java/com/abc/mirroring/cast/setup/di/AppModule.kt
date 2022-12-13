package com.abc.mirroring.cast.setup.di

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.abc.mirroring.cast.shared.utils.GsonParser
import com.abc.mirroring.cast.shared.utils.UriTypeAdapter

//@Module
//@InstallIn(SingletonComponent::class)
//class AppModule {
//    @Provides
//    fun provideGson(): Gson = GsonBuilder().registerTypeAdapter(Uri::class.java, UriTypeAdapter).create()
//
//    @Provides
//    fun provideGsonParser(gson: Gson): GsonParser {
//        return GsonParser(gson)
//    }
//}