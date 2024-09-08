package au.com.skater901.wc3.utils

import org.testcontainers.containers.MariaDBContainer
import kotlin.reflect.KClass

annotation class MariaDBConnection

class MariaDBExtension : SQLDBExtension<MariaDBContainer<*>>() {
    override val annotationClass: KClass<out Annotation> = MariaDBConnection::class

    override fun containerProvider(): MariaDBContainer<*> = MariaDBContainer("mariadb")
}