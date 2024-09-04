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
            // TODO support MySQL and MariaDB
            jdbcUrl = "jdbc:mariadb://${databaseConfig.host}:${databaseConfig.port}/wc3_bot"
            driverClassName = "org.mariadb.jdbc.Driver"
            username = databaseConfig.username
            password = databaseConfig.password
        }
    }

    @Provides
    @Inject
    @Singleton
    fun provideJdbi(dataSource: DataSource): Jdbi = Jdbi.create(dataSource)
}