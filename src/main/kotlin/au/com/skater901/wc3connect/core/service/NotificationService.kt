package au.com.skater901.wc3connect.core.service

import au.com.skater901.wc3connect.core.domain.ChannelNotification
import au.com.skater901.wc3connect.core.dao.ChannelNotificationDAO
import au.com.skater901.wc3connect.core.domain.exceptions.InvalidRegexPatternException
import jakarta.inject.Inject
import java.util.regex.PatternSyntaxException

public class NotificationService @Inject internal constructor(
    private val channelNotificationDAO: ChannelNotificationDAO
) {
    public suspend fun createNotification(id: String, mapPattern: String) {
        val mapRegex = try {
            Regex(mapPattern)
        } catch (e: PatternSyntaxException) {
            throw InvalidRegexPatternException(e.message!!)
        }

        channelNotificationDAO.save(ChannelNotification(id, mapRegex, "regex"))  // TODO
    }

    public suspend fun deleteNotification(channelId: Long) {
        channelNotificationDAO.delete(channelId)
    }
}