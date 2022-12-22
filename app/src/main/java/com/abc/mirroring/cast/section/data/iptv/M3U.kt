package com.abc.mirroring.cast.section.data.iptv

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "m3u")
data class M3U(@ColumnInfo(name = "name") var name: String = "",
                @ColumnInfo(name = "url") var url: String) {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true) var id: Long = 0
}