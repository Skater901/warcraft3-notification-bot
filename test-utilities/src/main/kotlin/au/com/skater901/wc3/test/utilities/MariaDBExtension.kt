package au.com.skater901.wc3.test.utilities

import org.testcontainers.mariadb.MariaDBContainer
import kotlin.reflect.KClass

public annotation class MariaDBConnection

public class MariaDBExtension : SQLDBExtension<MariaDBContainer>() {
    override val annotationClass: KClass<out Annotation> = MariaDBConnection::class

    override fun containerProvider(): MariaDBContainer = MariaDBContainer("mariadb")
}