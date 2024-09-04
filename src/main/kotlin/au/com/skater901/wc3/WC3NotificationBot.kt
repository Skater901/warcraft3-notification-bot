package au.com.skater901.wc3

import au.com.skater901.wc3.application.database.MigrationsManager
import au.com.skater901.wc3.application.managed.ModuleManager
import au.com.skater901.wc3.application.module.*
import au.com.skater901.wc3.core.job.NotifyGamesJob
import au.com.skater901.wc3.utils.getInstance
import com.google.inject.Guice
import com.google.inject.Injector
import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import java.io.File

internal class WC3NotificationBot {
    fun run() {
        validateConfig()

        val injector = ClassGraph().acceptPackages(this.javaClass.packageName)
            .enableAnnotationInfo()
            .scan()
            .use { createInjector(it) }

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

    private fun createInjector(scanResult: ScanResult): Injector = Guice.createInjector(
        AppModule(scanResult),
        ClientModule(),
        ConfigModule(scanResult),
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
            WC3NotificationBot().run()
        }
    }
}