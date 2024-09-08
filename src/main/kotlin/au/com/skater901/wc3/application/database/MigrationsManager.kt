package au.com.skater901.wc3.application.database

import au.com.skater901.wc3.application.config.DatabaseConfig
import jakarta.inject.Inject
import liquibase.Liquibase
import liquibase.database.core.MariaDBDatabase
import liquibase.database.core.MySQLDatabase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import javax.sql.DataSource

internal class MigrationsManager @Inject constructor(
    private val dataSource: DataSource,
    private val databaseConfig: DatabaseConfig
) {
    fun runMigrations() {
        val database = when (databaseConfig.type) {
            DatabaseConfig.DatabaseType.MariaDB -> MariaDBDatabase()
            DatabaseConfig.DatabaseType.MySQL -> MySQLDatabase()
        }
            .apply { connection = JdbcConnection(dataSource.connection) }

        Liquibase("migrations.xml", ClassLoaderResourceAccessor(), database).update()
    }
}