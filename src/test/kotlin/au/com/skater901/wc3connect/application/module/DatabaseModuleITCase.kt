package au.com.skater901.wc3connect.application.module

import au.com.skater901.wc3connect.application.config.DatabaseConfig
import au.com.skater901.wc3connect.core.dao.jdbi.wHandle
import au.com.skater901.wc3connect.utils.MySQLExtension
import au.com.skater901.wc3connect.utils.MySQLExtension.Configuration
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Provides
import com.zaxxer.hikari.HikariDataSource
import jakarta.inject.Singleton
import org.assertj.core.api.Assertions.assertThat
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import javax.sql.DataSource

@Configuration(
    username = "wc3connect",
    password = "mypassword"
)
class DatabaseModuleITCase {
    companion object {
        @RegisterExtension
        @JvmStatic
        val mySQL = MySQLExtension()
    }

    @Test
    fun `should create singleton connection pool and singleton jdbi`() {
        val databaseConfig = DatabaseConfig(
            "localhost",
            mySQL.port,
            "wc3connect",
            "mypassword"
        )

        val injector = Guice.createInjector(
            DatabaseModule(),
            object : AbstractModule() {
                @Provides
                @Singleton
                fun getDatabaseConfig(): DatabaseConfig = databaseConfig
            }
        )

        val connectionPool = injector.getInstance(DataSource::class.java)

        assertThat(connectionPool === injector.getInstance(DataSource::class.java)).isTrue()

        val jdbi = injector.getInstance(Jdbi::class.java)

        assertThat(jdbi === injector.getInstance(Jdbi::class.java)).isTrue()

        val tables = jdbi.wHandle {
            it.createQuery("SHOW TABLES FROM wc3_bot;")
                .mapTo(String::class.java)
                .list()
        }

        assertThat(tables).hasSize(3)
            .contains("DATABASECHANGELOG", "DATABASECHANGELOGLOCK", "notification")

        (connectionPool as HikariDataSource).close()
    }
}