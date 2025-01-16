package au.com.skater901.wc3.test.utilities

import org.testcontainers.containers.MySQLContainer
import kotlin.reflect.KClass

public annotation class MySQLConnection

public class MySQLDBExtension : SQLDBExtension<MySQLContainer<*>>() {
    override val annotationClass: KClass<out Annotation> = MySQLConnection::class

    override fun containerProvider(): MySQLContainer<*> = MySQLContainer("mysql")
}