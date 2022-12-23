package com.abc.mirroring.cast.section.data.iptv

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update

@Dao
interface M3uDAO {

    @Query("select * from m3u")
    suspend fun getAllM3U(): List<M3U>

    @Query("select * from m3u where id = :id")
    suspend fun findM3UById(id: Long): M3U

    @Insert(onConflict = REPLACE)
    suspend fun insertM3U(m3u: M3U)

    @Update(onConflict = REPLACE)
    suspend fun updateM3U(m3u: M3U)

    @Delete
    suspend fun deleteM3U(m3u: M3U)
}