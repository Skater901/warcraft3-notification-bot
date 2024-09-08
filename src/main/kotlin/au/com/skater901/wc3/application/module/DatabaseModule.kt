package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.application.config.DatabaseConfig
import au.com.skater901.wc3.core.dao.NotificationDAO
import au.com.skater901.wc3.core.dao.jdbi.JdbiNotificationDAO
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.zaxxer.hikari.HikariDataSource
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import javax.sql.DataSource

internal class DatabaseModule : AbstractModule() {
    override fun configure() {
        bind(NotificationDAO::class.java).to(JdbiNotificationDAO::class.java)
    }

    @Provides
    @Inject
    @Singleton
    fun provideDataSource(databaseConfig: DatabaseConfig): DataSource {
        return HikariDataSource().apply {
            when (databaseConfig.type) {
                DatabaseConfig.DatabaseType.MySQL -> {
                    jdbcUrl = "jdbc:mysql://${databaseConfig.host}:${databaseConfig.port}/wc3_bot"
                    driverClassName = com.mysql.cj.jdbc.Driver::class.qualifiedName
                }

                DatabaseConfig.DatabaseType.MariaDB -> {
                    jdbcUrl = "jdbc:mariadb://${databaseConfig.host}:${databaseConfig.port}/wc3_bot"
                    driverClassName = org.mariadb.jdbc.Driver::class.qualifiedName
                }
            }

            username = databaseConfig.username
            password = databaseConfig.password
        }
    }

    @Provides
    @Inject
    @Singleton
    fun provideJdbi(dataSource: DataSource): Jdbi = Jdbi.create(dataSource)
}