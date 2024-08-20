package au.com.skater901.wc3connect.core.service

import au.com.skater901.wc3connect.api.core.service.NotificationService
import au.com.skater901.wc3connect.core.dao.ChannelNotificationDAO
import au.com.skater901.wc3connect.core.domain.ChannelNotification
import au.com.skater901.wc3connect.api.core.domain.exceptions.InvalidRegexPatternException
import jakarta.inject.Inject
import java.util.regex.PatternSyntaxException

internal class NotificationServiceImpl @Inject constructor(
    private val channelNotificationDAO: ChannelNotificationDAO
) : NotificationService {
    override suspend fun createNotification(id: String, mapNameRegexPattern: String) {
        val mapRegex = try {
            Regex(mapNameRegexPattern)
        } catch (e: PatternSyntaxException) {
            throw InvalidRegexPatternException(e.message!!)
        }

        channelNotificationDAO.save(ChannelNotification(id, mapRegex, "regex"))  // TODO
    }

    override suspend fun deleteNotification(id: String): Boolean {
        channelNotificationDAO.delete(id)
        return true // TODO
    }
}