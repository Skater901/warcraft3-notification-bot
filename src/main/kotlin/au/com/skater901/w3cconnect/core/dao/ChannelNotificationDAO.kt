package au.com.skater901.w3cconnect.core.dao

import au.com.skater901.w3cconnect.core.ChannelNotification

interface ChannelNotificationDAO {
    suspend fun save(channelNotification: ChannelNotification)

    suspend fun find(): List<ChannelNotification>

    suspend fun delete(channelId: Long)
}