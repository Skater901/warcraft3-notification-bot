package au.com.skater901.wc3connect.application.module

import au.com.skater901.wc3connect.application.config.DatabaseConfig
import au.com.skater901.wc3connect.application.config.GamesConfiguration
import au.com.skater901.wc3connect.application.config.LogConfiguration
import au.com.skater901.wc3connect.utils.getInstance
import com.google.inject.Guice
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*

class ConfigModuleTest {
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
        }

        File(configFileName).outputStream()
            .use { configProperties.store(it, null) }

        val injector = Guice.createInjector(ConfigModule())

        val databaseConfig = injector.getInstance<DatabaseConfig>()

        assertThat(databaseConfig === injector.getInstance<DatabaseConfig>()).isTrue()

        assertThat(databaseConfig.host).isEqualTo("localhost")
        assertThat(databaseConfig.port).isEqualTo(3306)
        assertThat(databaseConfig.username).isEqualTo("myuser")
        assertThat(databaseConfig.password).isEqualTo("mypassword")

        val gamesConfiguration = injector.getInstance<GamesConfiguration>()

        assertThat(gamesConfiguration === injector.getInstance<GamesConfiguration>()).isTrue()

        assertThat(gamesConfiguration.gamesURL.toString()).isEqualTo("http://localhost")
        assertThat(gamesConfiguration.refreshInterval).isEqualTo(30)

        val logConfiguration = injector.getInstance<LogConfiguration>()

        assertThat(logConfiguration === injector.getInstance<LogConfiguration>()).isTrue()

        assertThat(logConfiguration.consoleLoggingLevel).isEqualTo("DEBUG")
        assertThat(logConfiguration.fileLoggingLevel).isEqualTo("WARN")
        assertThat(logConfiguration.logFileDirectory).isEqualTo("build/logs")
        assertThat(logConfiguration.logFileArchiveCount).isEqualTo(5)

        // TODO validate LoggingConfiguration was injected?
    }
}