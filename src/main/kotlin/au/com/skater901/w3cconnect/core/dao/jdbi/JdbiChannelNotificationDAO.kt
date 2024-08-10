package au.com.skater901.w3cconnect.core.dao.jdbi

import au.com.skater901.w3cconnect.core.ChannelNotification
import au.com.skater901.w3cconnect.core.dao.ChannelNotificationDAO

class JdbiChannelNotificationDAO : ChannelNotificationDAO {
    private val database = mutableListOf<ChannelNotification>()
    override suspend fun save(channelNotification: ChannelNotification) {
        database.add(channelNotification)
    }

    override suspend fun find(): List<ChannelNotification> = database

    override suspend fun delete(channelId: Long) {
        database.removeIf { it.channelId == channelId }
    }
}