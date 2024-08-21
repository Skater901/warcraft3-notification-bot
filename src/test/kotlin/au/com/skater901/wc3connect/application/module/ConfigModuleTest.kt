package au.com.skater901.wc3connect.application.module

import au.com.skater901.wc3connect.api.NotificationModule
import au.com.skater901.wc3connect.api.core.service.GameNotifier
import au.com.skater901.wc3connect.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3connect.application.config.DatabaseConfig
import au.com.skater901.wc3connect.application.config.GamesConfiguration
import au.com.skater901.wc3connect.application.config.LogConfiguration
import com.google.inject.Guice
import com.google.inject.Injector
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*
import kotlin.reflect.KClass

class ConfigModuleTest {
    class MyModuleConfig(val myProperty: String)

    @Test
    fun `should load config and bind config classes as singletons`() {
        val configFileName = "build/test-config.properties"

        System.setProperty("configFile", configFileName)

        val configProperties = Properties().apply {
            setProperty("database.host", "localhost")
            setProperty("database.port", "3306")
            setProperty("database.username", "myuser")
            setProperty("database.password", "mypassword")

            setProperty("gamesConfiguration.gamesURL", "http://localhost")
            setProperty("gamesConfiguration.refreshInterval", "30")

            setProperty("logging.consoleLoggingLevel", "DEBUG")
            setProperty("logging.fileLoggingLevel", "WARN")
            setProperty("logging.logFileDirectory", "build/logs")
            setProperty("logging.logFileArchiveCount", "5")

            setProperty("myModule.myProperty", "Hello World")
        }

        File(configFileName).outputStream()
            .use { configProperties.store(it, null) }

        val myModule = object : NotificationModule<MyModuleConfig> {
            override val moduleName: String = "myModule"
            override val configClass: KClass<MyModuleConfig> = MyModuleConfig::class

            override fun initializeNotificationHandlers(
                config: MyModuleConfig,
                injector: Injector,
                wc3GameNotificationService: WC3GameNotificationService
            ) {

            }

            override val gameNotifier: KClass<out GameNotifier>
                get() = TODO("Not yet implemented")
        } as NotificationModule<Any>

        val injector = Guice.createInjector(ConfigModule(listOf(myModule)))

        val databaseConfig = injector.getInstance(DatabaseConfig::class.java)

        assertThat(databaseConfig === injector.getInstance(DatabaseConfig::class.java)).isTrue()

        assertThat(databaseConfig.host).isEqualTo("localhost")
        assertThat(databaseConfig.port).isEqualTo(3306)
        assertThat(databaseConfig.username).isEqualTo("myuser")
        assertThat(databaseConfig.password).isEqualTo("mypassword")

        val gamesConfiguration = injector.getInstance(GamesConfiguration::class.java)

        assertThat(gamesConfiguration === injector.getInstance(GamesConfiguration::class.java)).isTrue()

        assertThat(gamesConfiguration.gamesURL.toString()).isEqualTo("http://localhost")
        assertThat(gamesConfiguration.refreshInterval).isEqualTo(30)

        val logConfiguration = injector.getInstance(LogConfiguration::class.java)

        assertThat(logConfiguration === injector.getInstance(LogConfiguration::class.java)).isTrue()

        assertThat(logConfiguration.consoleLoggingLevel).isEqualTo("DEBUG")
        assertThat(logConfiguration.fileLoggingLevel).isEqualTo("WARN")
        assertThat(logConfiguration.logFileDirectory).isEqualTo("build/logs")
        assertThat(logConfiguration.logFileArchiveCount).isEqualTo(5)

        // TODO validate LoggingConfiguration was injected?

        val myModuleConfig = injector.getInstance(MyModuleConfig::class.java)

        assertThat(myModuleConfig === injector.getInstance(MyModuleConfig::class.java))

        assertThat(myModuleConfig.myProperty).isEqualTo("Hello World")
    }
}