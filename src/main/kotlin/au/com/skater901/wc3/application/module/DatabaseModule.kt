package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.WC3NotificationBotConfiguration
import au.com.skater901.wc3.application.config.DatabaseConfig
import au.com.skater901.wc3.core.dao.NotificationDAO
import au.com.skater901.wc3.core.dao.jdbi.JdbiNotificationDAO
import com.google.inject.Provides
import com.zaxxer.hikari.HikariDataSource
import dev.misfitlabs.kotlinguice4.KotlinModule
import io.dropwizard.core.setup.Environment
import io.dropwizard.db.DataSourceFactory
import io.dropwizard.db.ManagedDataSource
import io.dropwizard.jdbi3.JdbiFactory
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jdbi.v3.core.Jdbi
import java.io.PrintWriter
import java.sql.Connection
import java.util.logging.Logger
import javax.sql.DataSource

internal class DatabaseModule : KotlinModule() {
    override fun configure() {
        bind<NotificationDAO>().to<JdbiNotificationDAO>()
    }

    @Provides
    @Inject
    fun provideDatabaseConfig(config: WC3NotificationBotConfiguration): DatabaseConfig = config.database!!

    @Provides
    @Inject
    @Singleton
    fun provideDataSource(databaseConfig: DatabaseConfig, environment: Environment): DataSource =
        HikariDataSource().apply {
            when (databaseConfig.type) {
                DatabaseConfig.DatabaseType.MySQL -> {
                    jdbcUrl = "jdbc:mysql://${databaseConfig.host}:${databaseConfig.port}/${databaseConfig.schema}"
                    driverClassName = com.mysql.cj.jdbc.Driver::class.qualifiedName
                }

                DatabaseConfig.DatabaseType.MariaDB -> {
                    jdbcUrl = "jdbc:mariadb://${databaseConfig.host}:${databaseConfig.port}/${databaseConfig.schema}"
                    driverClassName = org.mariadb.jdbc.Driver::class.qualifiedName
                }
            }

            poolName = "database"

            username = databaseConfig.username
            password = databaseConfig.password

            metricRegistry = environment.metrics()
            healthCheckRegistry = environment.healthChecks()
        }

    @Provides
    @Inject
    @Singleton
    fun provideJdbi(dataSource: DataSource, environment: Environment): Jdbi = JdbiFactory().build(
        environment,
        DataSourceFactory(),
        DelegatedManagedDataSource(dataSource),
        "database"
    )

    private class DelegatedManagedDataSource(private val delegate: DataSource) : ManagedDataSource {
        override fun getConnection(): Connection = delegate.connection

        override fun getConnection(username: String, password: String): Connection =
            delegate.getConnection(username, password)

        override fun getLogWriter(): PrintWriter = delegate.logWriter

        override fun setLogWriter(out: PrintWriter) {
            delegate.logWriter = out
        }

        override fun setLoginTimeout(seconds: Int) {
            delegate.loginTimeout = seconds
        }

        override fun getLoginTimeout(): Int = delegate.loginTimeout

        override fun getParentLogger(): Logger = delegate.parentLogger

        override fun <T> unwrap(iface: Class<T?>): T? = delegate.unwrap(iface)

        override fun isWrapperFor(iface: Class<*>): Boolean = delegate.isWrapperFor(iface)

        override fun stop() {
            (delegate as HikariDataSource).close()
        }
    }
}