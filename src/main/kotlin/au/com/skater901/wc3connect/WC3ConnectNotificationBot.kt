package au.com.skater901.wc3connect

import au.com.skater901.wc3connect.api.NotificationModule
import au.com.skater901.wc3connect.application.database.MigrationsManager
import au.com.skater901.wc3connect.application.managed.ModuleManager
import au.com.skater901.wc3connect.application.module.*
import au.com.skater901.wc3connect.core.job.NotifyGamesJob
import com.google.inject.Guice
import com.google.inject.Injector
import java.io.File

internal class WC3ConnectNotificationBot {
    internal fun run() {
        validateConfig()

        val moduleManager = ModuleManager()

        val injector = createInjector(moduleManager.notificationModules)

        // Run database migrations
        injector.getInstance(MigrationsManager::class.java).runMigrations()

        moduleManager.initializeModules(injector)

        injector.getInstance(NotifyGamesJob::class.java).start()

        // Hack to keep the app running
        while (true) {

        }
    }

    private fun validateConfig() {
        val configFilePath = System.getProperty("configFile")
            ?: throw IllegalArgumentException("Required system property [ configFile ] has not been set. Please set it, with a path to a config file, using -DconfigFile=/path/to/config/file.properties")

        // check file exists
        if (!File(configFilePath).exists()) throw IllegalArgumentException("Config file [ $configFilePath ] does not exist.")
    }

    private fun createInjector(notificationModules: List<NotificationModule<Any>>): Injector {
        val guiceModules = notificationModules.map { it.guiceModule() }

        return Guice.createInjector(
            AppModule(),
            ClientModule(),
            ConfigModule(notificationModules),
            DatabaseModule(),
            GameNotifierModule(notificationModules),
            *guiceModules.toTypedArray()
        )
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            WC3ConnectNotificationBot().run()
        }
    }
}