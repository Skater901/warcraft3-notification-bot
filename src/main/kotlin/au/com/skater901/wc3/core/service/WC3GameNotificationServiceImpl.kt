package au.com.skater901.wc3.core.service

import au.com.skater901.wc3.api.core.domain.exceptions.InvalidRegexPatternException
import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3.core.dao.NotificationDAO
import au.com.skater901.wc3.core.domain.WC3GameNotification
import jakarta.inject.Inject
import jakarta.inject.Named
import java.util.regex.PatternSyntaxException

internal class WC3GameNotificationServiceImpl @Inject constructor(
    private val notificationDAO: NotificationDAO,
    @Named("moduleName")
    private val type: String
) : WC3GameNotificationService {
    override suspend fun createNotification(id: String, mapNameRegexPattern: String) {
        val mapRegex = try {
            Regex(mapNameRegexPattern)
        } catch (e: PatternSyntaxException) {
            throw InvalidRegexPatternException(e.message!!)
        }

        notificationDAO.save(WC3GameNotification(id, type, mapRegex))
    }

    override suspend fun deleteNotification(id: String): Boolean = notificationDAO.delete(id)
}