package au.com.skater901.wc3connect.core.domain.exceptions

/**
 * Indicates that the provided notificationId was invalid. IE, if a notification was registered for a Discord channel
 * which has since been deleted, you can throw this exception to indicate that the notification is no longer valid.
 *
 * When this exception is thrown, the notificationId that triggered it will be deleted from the database.
 */
public class InvalidNotificationException : IllegalArgumentException()