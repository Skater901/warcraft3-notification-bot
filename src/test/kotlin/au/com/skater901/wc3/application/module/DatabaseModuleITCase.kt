package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.application.config.DatabaseConfig
import au.com.skater901.wc3.test.utilities.MariaDBExtension
import au.com.skater901.wc3.test.utilities.MySQLDBExtension
import au.com.skater901.wc3.test.utilities.SQLDBExtension
import au.com.skater901.wc3.test.utilities.SQLDBExtension.Configuration
import au.com.skater901.wc3.utilities.database.wHandle
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Provides
import com.zaxxer.hikari.HikariDataSource
import dev.misfitlabs.kotlinguice4.getInstance
import jakarta.inject.Singleton
import org.assertj.core.api.Assertions.assertThat
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import javax.sql.DataSource

@Configuration(
    username = "wc3_notification_bot",
    password = "mypassword",
    module = false
)
internal class DatabaseModuleITCase {
    companion object {
        @RegisterExtension
        @JvmStatic
        val mariaDB = MariaDBExtension()

        @RegisterExtension
        @JvmStatic
        val mySQL = MySQLDBExtension()

        fun runTest(test: SQLDBExtension<*>.(DatabaseConfig.DatabaseType) -> Unit) {
            test(mariaDB, DatabaseConfig.DatabaseType.MariaDB)
            test(mySQL, DatabaseConfig.DatabaseType.MySQL)
        }
    }

    @Test
    fun `should create singleton connection pool and singleton jdbi`() {
        runTest { type ->
            val databaseConfig = DatabaseConfig(
                type,
                "localhost",
                port,
                username = "wc3_notification_bot",
                password = "mypassword"
            )

            val injector = Guice.createInjector(
                DatabaseModule(),
                object : AbstractModule() {
                    @Provides
                    @Singleton
                    fun getDatabaseConfig(): DatabaseConfig = databaseConfig
                }
            )

            val connectionPool = injector.getInstance<DataSource>()

            assertThat(connectionPool === injector.getInstance<DataSource>()).isTrue()

            val jdbi = injector.getInstance<Jdbi>()

            assertThat(jdbi === injector.getInstance<Jdbi>()).isTrue()

            val tables = jdbi.wHandle {
                it.createQuery("SHOW TABLES FROM wc3_bot;")
                    .mapTo(String::class.java)
                    .list()
            }

            assertThat(tables).hasSize(4)
                .contains("DATABASECHANGELOG", "DATABASECHANGELOGLOCK", "notification", "discord_notification_role")

            (connectionPool as HikariDataSource).close()
        }
    }
}