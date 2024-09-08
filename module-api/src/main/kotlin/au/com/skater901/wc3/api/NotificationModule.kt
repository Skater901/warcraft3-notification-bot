package au.com.skater901.wc3.api

import au.com.skater901.wc3.api.core.service.GameNotifier
import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3.api.scheduled.ScheduledTask
import com.google.inject.AbstractModule
import com.google.inject.Injector
import kotlin.reflect.KClass

/**
 * The main class to implement when registering a module.
 *
 * [C], [G], and [T] are type parameters that represent the classes your module provides.
 *
 * [C] is the class you create for your config. See the [configClass] property for more information.
 *
 * [G] is the class you create to notify on games being hosted and unhosted. As the syntax shows, [G] must implement
 * [GameNotifier].
 *
 * [T] is the class you create to hook into the existing event loop, if you wish to. As the syntax shows, [T] must
 * implement [ScheduledTask]. If you do not wish to provide a [ScheduledTask], set [T] to [ScheduledTask].
 */
public interface NotificationModule<C : Any, G : GameNotifier, T : ScheduledTask> {
    /**
     * The unique name of this module. Recommendation is for this to be a single word with no special characters.
     */
    public val moduleName: String

    /**
     * A simple class with a primary constructor listing all the configuration properties you need. The configuration
     * properties in the config file at runtime need to be namespaced with your [moduleName].
     *
     * For example, if you had a Facebook Module, and your [moduleName] was "facebook", the config properties
     * in the config file would be prefixed with "facebook.". Assume that you need a username and password, your config
     * class would look like this:
     * ```kotlin
     * class FacebookConfig(
     *     val username: String,
     *     val password: String
     * )
     * ```
     * Then, in the config file, you would have the following properties:
     * ```text
     * facebook.username=myUsername
     * facebook.password=myPassword
     * ```
     * Currently, there a limited number of types supported for config. The supported types can be seen in [au.com.skater901.wc3.application.config.ConfigParser.get].
     * New types can be added on request.
     *
     * An instance of your [configClass], populated with configuration values, will be provided to the [initializeNotificationHandlers] method.
     */
    public val configClass: KClass<C>

    /**
     * Provide a Guice module where all injection configurations required for your module are configured. This is
     * optional, hence the default value being an empty [AbstractModule].
     *
     * If you're unfamiliar with Guice, here's an easy way to provide your main class:
     * ```kotlin
     * package my.package
     *
     * import com.facebook.FacebookBuilder
     * import com.facebook.FacebookInterface
     * import com.google.inject.AbstractModule
     * import com.google.inject.Provides
     * import jakarta.inject.Inject
     * import jakarta.inject.Singleton
     * import my.package.FacebookConfiguration
     *
     * FacebookGuiceModule : AbstractModule() {
     *     @Provides
     *     @Inject
     *     @Singleton // This can be omitted if your main class can be instantiated multiple times, but 99% of the time that won't be the case
     *     fun provideFacebook(config: FacebookConfiguration): FacebookInterface {
     *         return FacebookBuilder().withUsername(config.username)
     *             .withPassword(config.password)
     *             .build()
     *     }
     * }
     * ```
     *
     * @return The configured [AbstractModule].
     */
    public fun guiceModule(): AbstractModule = object : AbstractModule() {}

    /**
     * Function to start whatever is needed for your API. This is the place to instantiate any classes required for
     * receiving interactions via your protocol. At a minimum, you should be able to receive requests to add a new
     * notification listener, otherwise your module is useless.
     *
     * @param config An instance of your [configClass] populated with the configuration for this module.
     * @param injector A Guice [Injector] that can be used for instantiating your listeners via injection. As a quick
     * example, if you had a class called `FacebookRegisterNotification`, you would get an instance by going `injector.getInstance(FacebookRegisterNotification::class.java)`
     * @param wc3GameNotificationService An instance of the [wc3GameNotificationService]. This is provided in case you prefer to
     * construct your classes directly, rather than using Guice's dependency injection method.
     */
    public fun initializeNotificationHandlers(
        config: C,
        injector: Injector,
        wc3GameNotificationService: WC3GameNotificationService
    )

    /**
     * The class of an optional [ScheduledTask] that you want to be run. Can be used for registering code that polls for
     * something.
     *
     * Use this if you wish to use dependency injection with Guice. An instance of your class will be instantiated via
     * Guice.
     *
     * If both a [scheduledTaskClass] and [scheduledTask] are provided, an [IllegalArgumentException] will be thrown.
     * Please provide one or the other, or none, not both.
     */
    public val scheduledTaskClass: KClass<T>?
        get() = null

    /**
     * An optional [ScheduledTask] that you want to be run. Can be used for registering code that polls for something.
     *
     * Use this if you wish to instantiate your own instance of your scheduled task, without using dependency injection.
     *
     * This function will be called multiple times by the application, so if your class is expensive to instantiate,
     * you should lazily instantiate it and return the same instance every time this function is called. An easy way to
     * do this is to use a private backing field with Kotlin's lazy delegate.
     *
     * ```kotlin
     * private val myScheduledTask by lazy {
     *     MyScheduledTask()
     * }
     *
     * override fun scheduledTask(): MyScheduledTask = myScheduledTask
     * ```
     *
     * If both a [scheduledTaskClass] and [scheduledTask] are provided, an [IllegalArgumentException] will be thrown.
     * Please provide one or the other, or none, not both.
     */
    public fun scheduledTask(): T? = null

    /**
     * The class you have created that implements [GameNotifier], and is used for notifying on new games, updating
     * existing games that get modified, and closing off games that have started or been unhosted.
     *
     * Use this if you wish to use dependency injection with Guice. An instance of your class will be instantiated via
     * Guice.
     *
     * You must implement either [gameNotifierClass] or [gameNotifier]. If both return null, or both return a value, an
     * [IllegalArgumentException] will be thrown when initializing your module.
     */
    public val gameNotifierClass: KClass<G>?
        get() = null

    /**
     * An instance of the class you have created that implements [GameNotifier], and is used for notifying on new games,
     * updating existing games that get modified, and closing off games that have started or been unhosted.
     *
     * Use this if you wish to instantiate your own instance of your [GameNotifier], without using dependency injection.
     *
     * This function will be called multiple times by the application, so if your class is expensive to instantiate,
     * you should lazily instantiate it and return the same instance every time this function is called. An easy way to
     * do this is to use a private backing field with Kotlin's lazy delegate.
     *
     * ```kotlin
     * private val myGameNotifier by lazy {
     *     MyGameNotifier()
     * }
     *
     * override fun gameNotifier(): MyGameNotifier = myGameNotifier
     * ```
     *
     * You must implement either [gameNotifierClass] or [gameNotifier]. If both return null, or both return a value, an
     * [IllegalArgumentException] will be thrown when initializing your module.
     */
    public fun gameNotifier(): G? = null
}