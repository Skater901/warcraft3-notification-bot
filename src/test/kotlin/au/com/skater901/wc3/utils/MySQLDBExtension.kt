package au.com.skater901.wc3.utils

import org.testcontainers.containers.MySQLContainer
import kotlin.reflect.KClass

annotation class MySQLConnection

class MySQLDBExtension : SQLDBExtension<MySQLContainer<*>>() {
    override val annotationClass: KClass<out Annotation> = MySQLConnection::class

    override fun containerProvider(): MySQLContainer<*> = MySQLContainer("mysql")
}