package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.application.config.*
import au.com.skater901.wc3.utils.getInstance
import au.com.skater901.wc3.utils.scanResult
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

            setProperty("application.refreshInterval", "60")

            setProperty("logging.consoleLoggingLevel", "DEBUG")
            setProperty("logging.fileLoggingLevel", "WARN")
            setProperty("logging.logFileDirectory", "build/logs")
            setProperty("logging.logFileArchiveCount", "5")

            setProperty("wc3connect.url", "http://localhost:1234")
            setProperty("wc3stats.url", "http://localhost:4321")
        }

        File(configFileName).outputStream()
            .use { configProperties.store(it, null) }

        val injector = scanResult { Guice.createInjector(ConfigModule(it)) }

        val databaseConfig = injector.getInstance<DatabaseConfig>()

        assertThat(databaseConfig === injector.getInstance<DatabaseConfig>()).isTrue()

        assertThat(databaseConfig.host).isEqualTo("localhost")
        assertThat(databaseConfig.port).isEqualTo(3306)
        assertThat(databaseConfig.username).isEqualTo("myuser")
        assertThat(databaseConfig.password).isEqualTo("mypassword")

        val applicationConfiguration = injector.getInstance<ApplicationConfiguration>()

        assertThat(applicationConfiguration === injector.getInstance<ApplicationConfiguration>()).isTrue()

        assertThat(applicationConfiguration.refreshInterval).isEqualTo(60)

        val logConfiguration = injector.getInstance<LogConfiguration>()

        assertThat(logConfiguration === injector.getInstance<LogConfiguration>()).isTrue()

        assertThat(logConfiguration.consoleLoggingLevel).isEqualTo("DEBUG")
        assertThat(logConfiguration.fileLoggingLevel).isEqualTo("WARN")
        assertThat(logConfiguration.logFileDirectory).isEqualTo("build/logs")
        assertThat(logConfiguration.logFileArchiveCount).isEqualTo(5)

        // TODO validate LoggingConfiguration was injected?

        val wc3ConnectConfiguration = injector.getInstance<WC3ConnectConfig>()

        assertThat(wc3ConnectConfiguration === injector.getInstance<WC3ConnectConfig>()).isTrue()
        assertThat(wc3ConnectConfiguration.url.toString()).isEqualTo("http://localhost:1234")

        val wc3StatsConfiguration = injector.getInstance<WC3StatsConfig>()

        assertThat(wc3StatsConfiguration === injector.getInstance<WC3StatsConfig>()).isTrue()
        assertThat(wc3StatsConfiguration.url.toString()).isEqualTo("http://localhost:4321")
    }
}