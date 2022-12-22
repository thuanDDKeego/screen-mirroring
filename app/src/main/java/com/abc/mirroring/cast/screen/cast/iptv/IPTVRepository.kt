package com.abc.mirroring.cast.screen.cast.iptv

import com.abc.mirroring.cast.section.data.iptv.M3U
import com.abc.mirroring.cast.section.data.iptv.M3uDAO
import javax.inject.Inject

class IPTVRepository @Inject constructor(val m3uDAO: M3uDAO) {

    suspend fun getAllM3U(): List<M3U> = m3uDAO.getAllM3U()

    suspend fun addM3U(m3u: M3U) = m3uDAO.insertTask(m3u)

    suspend fun delete(m3u: M3U) = m3uDAO.deleteTask(m3u)
}
