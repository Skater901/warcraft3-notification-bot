package au.com.skater901.w3cconnect.core.service

import au.com.skater901.w3cconnect.core.ChannelNotification
import au.com.skater901.w3cconnect.core.dao.ChannelNotificationDAO
import au.com.skater901.w3cconnect.core.domain.exceptions.InvalidRegexPatternException
import jakarta.inject.Inject
import java.util.regex.PatternSyntaxException

class NotificationService @Inject constructor(
    private val channelNotificationDAO: ChannelNotificationDAO
) {
    suspend fun createNotification(channelId: Long, mapPattern: String) {
        val mapRegex = try {
            Regex(mapPattern)
        } catch (e: PatternSyntaxException) {
            throw InvalidRegexPatternException(e.message ?: "")
        }

        channelNotificationDAO.save(ChannelNotification(channelId, mapRegex))
    }

    suspend fun deleteNotification(channelId: Long) {
        channelNotificationDAO.delete(channelId)
    }
}