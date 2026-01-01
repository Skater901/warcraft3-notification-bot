package au.com.skater901.wc3.application.provider

import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3.core.dao.NotificationDAO
import au.com.skater901.wc3.core.service.WC3GameNotificationServiceImpl
import jakarta.inject.Provider

internal class WC3GameNotificationServiceProvider(
    private val notificationDAOProvider: Provider<NotificationDAO>,
    private val moduleName: String
) : Provider<WC3GameNotificationService> {
    override fun get(): WC3GameNotificationService = WC3GameNotificationServiceImpl(
        notificationDAOProvider.get(),
        moduleName
    )
}