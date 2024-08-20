package au.com.skater901.wc3connect.api.core.service

/**
 * Service for managing notifications.
 */
public interface WC3GameNotificationService {
    /**
     * Create a new notification.
     *
     * @param id The unique identifier of this notification. The exact form of this will vary depending on your specific
     * protocol. If this id is a duplicate of a notification that already exists, the existing notification will be
     * updated with the provided [mapNameRegexPattern]
     *
     * @param mapNameRegexPattern The regex pattern for matching Warcraft 3 map names. Must be a valid regex string.
     *
     * @throws au.com.skater901.wc3connect.api.core.domain.exceptions.InvalidRegexPatternException If the value of
     * [mapNameRegexPattern] is an invalid regex pattern.
     */
    public suspend fun createNotification(id: String, mapNameRegexPattern: String)

    /**
     * Delete the specified notification.
     *
     * @param id The unique identifier of the notification you wish to delete.
     *
     * @return Whether a matching notification was found and deleted or not.
     */
    public suspend fun deleteNotification(id: String): Boolean
}