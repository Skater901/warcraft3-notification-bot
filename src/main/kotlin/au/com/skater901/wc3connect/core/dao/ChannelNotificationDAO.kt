package au.com.skater901.wc3connect.core.dao

import au.com.skater901.wc3connect.core.domain.ChannelNotification

internal interface ChannelNotificationDAO {
    suspend fun save(channelNotification: ChannelNotification)

    suspend fun find(): List<ChannelNotification>

    suspend fun delete(channelId: String)
}