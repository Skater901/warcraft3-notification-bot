package au.com.skater901.wc3.api.core.service

/**
 * Implement this interface to publish admin messages to subscribers. Admin messages will be used to announce things
 * like server downtime due to maintenance.
 */
public interface AdminMessageNotifier {
    /**
     * This function will be called with the admin message, and all the registered notifications for this module.
     *
     * @param message The admin message.
     * @param notifications All the unique IDs of the registered notifications for this module. These are the IDs
     * registered in the [WC3GameNotificationService].
     */
    public suspend fun sendAdminMessage(message: String, notifications: List<String>)
}