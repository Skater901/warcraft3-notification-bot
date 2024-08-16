package au.com.skater901.wc3connect

import au.com.skater901.wc3connect.core.service.GameNotifier
import au.com.skater901.wc3connect.core.service.NotificationService
import com.google.inject.AbstractModule
import com.google.inject.Injector
import kotlin.reflect.KClass

public interface NotificationModule<T : Any, C : Any> {
    public val moduleName: String

    public val mainSystemClass: KClass<T>

    public val configClass: KClass<C>

    public fun guiceModule(): AbstractModule = object : AbstractModule() {}

    public fun initializeNotificationHandlers(
        mainClass: T,
        config: C,
        injector: Injector,
        notificationService: NotificationService
    )

    public val gameNotifier: KClass<out GameNotifier>
}