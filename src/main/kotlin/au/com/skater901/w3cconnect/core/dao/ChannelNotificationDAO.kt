package au.com.skater901.w3cconnect.core.dao

interface ChannelNotificationDAO {
    suspend fun save()

    suspend fun find(): List<Any>
}