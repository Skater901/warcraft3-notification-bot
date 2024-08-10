package au.com.skater901.w3cconnect.application.module

import au.com.skater901.w3cconnect.WC3ConnectDiscordNotificationBotConfiguration
import au.com.skater901.w3cconnect.core.dao.ChannelNotificationDAO
import au.com.skater901.w3cconnect.core.dao.jdbi.JdbiChannelNotificationDAO
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.zaxxer.hikari.HikariDataSource
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import javax.sql.DataSource

class DatabaseModule : AbstractModule() {
    override fun configure() {
        bind(ChannelNotificationDAO::class.java).to(JdbiChannelNotificationDAO::class.java)
    }

    @Provides
    @Inject
    @Singleton
    fun provideDataSource(configuration: WC3ConnectDiscordNotificationBotConfiguration): DataSource {
        val databaseConfig = configuration.database
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