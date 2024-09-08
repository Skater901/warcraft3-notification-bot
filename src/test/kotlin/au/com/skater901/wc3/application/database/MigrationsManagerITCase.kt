package au.com.skater901.wc3.application.database

import au.com.skater901.wc3.application.config.DatabaseConfig
import au.com.skater901.wc3.utils.MariaDBExtension
import au.com.skater901.wc3.utils.MySQLDBExtension
import com.zaxxer.hikari.HikariDataSource
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import javax.sql.DataSource

class MigrationsManagerITCase {
    companion object {
        @RegisterExtension
        @JvmField
        val mariaDB = MariaDBExtension()

        @RegisterExtension
        @JvmField
        val mySQL = MySQLDBExtension()
    }

    @Test
    fun `should migrate for all databases`() {
        fun testMigration(dataSource: DataSource, databaseType: DatabaseConfig.DatabaseType) {
            val config = mock<DatabaseConfig> {
                on { type } doReturn databaseType
            }

            MigrationsManager(dataSource, config).runMigrations()
        }

        testMigration(
            HikariDataSource().apply {
                jdbcUrl = "jdbc:mariadb://localhost:${mariaDB.port}/wc3_bot"
                driverClassName = org.mariadb.jdbc.Driver::class.qualifiedName
                username = mariaDB.username
                password = mariaDB.password
            },
            DatabaseConfig.DatabaseType.MariaDB
        )
        testMigration(
            HikariDataSource().apply {
                jdbcUrl = "jdbc:mysql://localhost:${mySQL.port}/wc3_bot"
                driverClassName = com.mysql.cj.jdbc.Driver::class.qualifiedName
                username = mySQL.username
                password = mySQL.password
            },
            DatabaseConfig.DatabaseType.MySQL
        )
    }
}