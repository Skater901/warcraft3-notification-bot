package au.com.skater901.wc3.core.dao

import au.com.skater901.wc3.core.domain.WC3GameNotification

internal interface NotificationDAO {
    suspend fun save(wc3GameNotification: WC3GameNotification)

    suspend fun find(): List<WC3GameNotification>

    suspend fun delete(id: String): Boolean
}