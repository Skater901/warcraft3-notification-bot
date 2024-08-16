package au.com.skater901.wc3connect.utils

import au.com.skater901.wc3connect.core.dao.jdbi.updateFromFile
import au.com.skater901.wc3connect.core.dao.jdbi.usingHandle
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.extension.*
import org.testcontainers.containers.MySQLContainer
import java.sql.DriverManager

class MySQLExtension : BeforeAllCallback, AfterAllCallback, AfterEachCallback, ParameterResolver {
    private lateinit var mysqlContainer: MySQLContainer<*>

    val jdbi: Jdbi by lazy {
        Jdbi.create(
            mysqlContainer.jdbcUrl,
            mysqlContainer.username,
            mysqlContainer.password
        )
    }

    val port: Int
        get() = mysqlContainer.getMappedPort(3306)

    override fun beforeAll(context: ExtensionContext) {
        val configuration: Configuration? = context.requiredTestClass.getAnnotation(Configuration::class.java)

        mysqlContainer = MySQLContainer("mysql").withDatabaseName("w3c_bot")
            .configure(configuration)
            .apply { start() }

        if (configuration == null || configuration.migrate) {
            val database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(
                    JdbcConnection(
                        DriverManager.getConnection(
                            mysqlContainer.jdbcUrl,
                            mysqlContainer.username,
                            mysqlContainer.password
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

    private fun MySQLContainer<*>.configure(configuration: Configuration?): MySQLContainer<*> {
        if (configuration == null)
            return this

        return withUsername(configuration.username)
            .withPassword(configuration.password)
    }

    override fun afterEach(context: ExtensionContext) {
        // TODO figure out how to clear all tables after each test
    }

    override fun afterAll(context: ExtensionContext) {
        mysqlContainer.stop()
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