package au.com.skater901.wc3.application.database

import jakarta.inject.Inject
import liquibase.Liquibase
import liquibase.database.core.MariaDBDatabase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import javax.sql.DataSource

internal class MigrationsManager @Inject constructor(
    private val dataSource: DataSource
) {
    fun runMigrations() {
        val database = MariaDBDatabase().apply {
            connection = JdbcConnection(dataSource.connection)
        }
        Liquibase("migrations.xml", ClassLoaderResourceAccessor(), database).update()
    }
}