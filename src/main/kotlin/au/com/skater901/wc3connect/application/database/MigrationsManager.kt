package au.com.skater901.wc3connect.application.database

import jakarta.inject.Inject
import liquibase.Liquibase
import liquibase.database.core.MySQLDatabase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import javax.sql.DataSource

internal class MigrationsManager @Inject constructor(
    private val dataSource: DataSource
) {
    fun runMigrations() {
        val database = MySQLDatabase().apply {
            connection = JdbcConnection(dataSource.connection)
        }
        Liquibase("migrations.xml", ClassLoaderResourceAccessor(), database).update()
    }
}