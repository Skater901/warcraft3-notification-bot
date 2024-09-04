package au.com.skater901.wc3.utils

import au.com.skater901.wc3.core.dao.jdbi.updateFromFile
import au.com.skater901.wc3.core.dao.jdbi.usingHandle
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.extension.*
import org.testcontainers.containers.MariaDBContainer
import java.sql.DriverManager

class MariaDBExtension : BeforeAllCallback, AfterAllCallback, AfterEachCallback, ParameterResolver {
    private lateinit var mariadbContainer: MariaDBContainer<*>

    val jdbi: Jdbi by lazy {
        Jdbi.create(
            mariadbContainer.jdbcUrl,
            mariadbContainer.username,
            mariadbContainer.password
        )
    }

    val port: Int
        get() = mariadbContainer.getMappedPort(3306)

    override fun beforeAll(context: ExtensionContext) {
        val configuration: Configuration? = context.requiredTestClass.getAnnotation(Configuration::class.java)

        mariadbContainer = MariaDBContainer("mariadb").withDatabaseName("wc3_bot")
            .configure(configuration)
            .apply { start() }

        if (configuration == null || configuration.migrate) {
            val database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(
                    JdbcConnection(
                        DriverManager.getConnection(
                            mariadbContainer.jdbcUrl,
                            mariadbContainer.username,
                            mariadbContainer.password
                        )
                    )
                )

            Liquibase("migrations.xml", ClassLoaderResourceAccessor(), database).use { it.update() }
        }

        if (configuration?.setupScripts?.isNotEmpty() == true) {
            jdbi.usingHandle { handle ->
                configuration.setupScripts
                    .forEach {
                        handle.updateFromFile(it)
                            .execute()
                    }
            }
        }
    }

    private fun MariaDBContainer<*>.configure(configuration: Configuration?): MariaDBContainer<*> {
        if (configuration == null)
            return this

        return withUsername(configuration.username)
            .withPassword(configuration.password)
    }

    override fun afterEach(context: ExtensionContext) {
        jdbi.usingHandle { handle ->
            handle.createQuery("SHOW tables FROM wc3_bot")
                .mapTo(String::class.java)
                .list()
                .filter { !it.startsWith("DATABASECHANGELOG") }
                .forEach {
                    handle.createUpdate("DELETE FROM $it;")
                        .execute()
                }
        }
    }

    override fun afterAll(context: ExtensionContext) {
        mariadbContainer.stop()
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
        parameterContext.parameter.type == Jdbi::class.java

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any = jdbi

    annotation class Configuration(
        val username: String = "test",

        val password: String = "test",

        val migrate: Boolean = true,

        val setupScripts: Array<String> = []
    )
}