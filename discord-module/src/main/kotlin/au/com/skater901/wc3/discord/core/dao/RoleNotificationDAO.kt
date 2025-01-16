package au.com.skater901.wc3.discord.core.dao

internal interface RoleNotificationDAO {
    suspend fun save(channelId: String, roleId: String)

    suspend fun find(channelId: String): String?

    suspend fun delete(channelId: String)
}