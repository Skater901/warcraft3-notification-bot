package au.com.skater901.wc3connect

import au.com.skater901.wc3connect.application.database.MigrationsManager
import au.com.skater901.wc3connect.application.managed.ModuleManager
import au.com.skater901.wc3connect.application.module.*
import au.com.skater901.wc3connect.core.job.NotifyGamesJob
import au.com.skater901.wc3connect.utils.getInstance
import com.google.inject.Guice
import com.google.inject.Injector
import java.io.File

internal class WC3ConnectNotificationBot {
    fun run() {
        validateConfig()

        val injector = createInjector()

        // Run database migrations
        injector.getInstance<MigrationsManager>().runMigrations()

        val moduleManager = injector.getInstance<ModuleManager>()

        moduleManager.initializeModules(injector)

        startGamesNotifyingJob(injector, moduleManager)

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

    private fun createInjector(): Injector = Guice.createInjector(
        AppModule(),
        ClientModule(),
        ConfigModule(),
        DatabaseModule(),
        NotificationModulesModule()
    )

    private fun startGamesNotifyingJob(injector: Injector, moduleManager: ModuleManager) {
        injector.createChildInjector(GameNotifierModule(moduleManager.getGameNotifiers()))
            .getInstance<NotifyGamesJob>()
            .start()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            WC3ConnectNotificationBot().run()
        }
    }
}