package au.com.skater901.wc3.utils

import au.com.skater901.wc3.core.dao.jdbi.updateFromFile
import au.com.skater901.wc3.core.dao.jdbi.usingHandle
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.extension.*
import org.testcontainers.containers.JdbcDatabaseContainer
import java.sql.DriverManager
import kotlin.reflect.KClass

abstract class SQLDBExtension<out CONTAINER : JdbcDatabaseContainer<out CONTAINER>> : BeforeAllCallback,
    AfterAllCallback,
    AfterEachCallback,
    ParameterResolver {
    private lateinit var container: CONTAINER

    private val jdbi: Jdbi by lazy {
        Jdbi.create(
            container.jdbcUrl,
            container.username,
            container.password
        )
    }

    protected abstract val annotationClass: KClass<out Annotation>

    protected abstract fun containerProvider(): CONTAINER

    val port: Int
        get() = container.getMappedPort(3306)

    val username: String
        get() = container.username

    val password: String
        get() = container.password

    override fun beforeAll(context: ExtensionContext) {
        val configuration: Configuration? = context.requiredTestClass.getAnnotation(Configuration::class.java)

        container = containerProvider().withDatabaseName("wc3_bot")
            .configure(configuration)
            .apply { start() }

        if (configuration == null || configuration.migrate) {
            val database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(
                    JdbcConnection(
                        DriverManager.getConnection(
                            container.jdbcUrl,
                            container.username,
                            container.password
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

    private fun CONTAINER.configure(configuration: Configuration?): CONTAINER {
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
        container.stop()
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
        parameterContext.parameter.type == Jdbi::class.java &&
                parameterContext.isAnnotated(annotationClass.java)

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any? =
        if (parameterContext.isAnnotated(annotationClass.java))
            jdbi
        else
            null

    annotation class Configuration(
        val username: String = "test",

        val password: String = "test",

        val migrate: Boolean = true,

        val setupScripts: Array<String> = []
    )
}