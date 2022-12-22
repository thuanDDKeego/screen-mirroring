package com.abc.mirroring.cast.section.data.iptv

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [M3U::class], version = 1, exportSchema = false)
abstract class IPTVDatabase : RoomDatabase() {

    abstract fun m3uDao(): M3uDAO
}

