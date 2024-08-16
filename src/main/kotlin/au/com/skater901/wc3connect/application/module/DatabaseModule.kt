package au.com.skater901.wc3connect.application.module

import au.com.skater901.wc3connect.application.config.DatabaseConfig
import au.com.skater901.wc3connect.core.dao.ChannelNotificationDAO
import au.com.skater901.wc3connect.core.dao.jdbi.JdbiChannelNotificationDAO
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.zaxxer.hikari.HikariDataSource
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import javax.sql.DataSource

internal class DatabaseModule : AbstractModule() {
    override fun configure() {
        bind(ChannelNotificationDAO::class.java).to(JdbiChannelNotificationDAO::class.java)
    }

    @Provides
    @Inject
    @Singleton
    fun provideDataSource(databaseConfig: DatabaseConfig): DataSource {
        return HikariDataSource().apply {
            jdbcUrl = "jdbc:mysql://${databaseConfig.host}:${databaseConfig.port}/w3c_bot"
            driverClassName = "com.mysql.cj.jdbc.Driver"
            username = databaseConfig.username
            password = databaseConfig.password
        }
    }

    @Provides
    @Inject
    @Singleton
    fun provideJdbi(dataSource: DataSource): Jdbi = Jdbi.create(dataSource)
}