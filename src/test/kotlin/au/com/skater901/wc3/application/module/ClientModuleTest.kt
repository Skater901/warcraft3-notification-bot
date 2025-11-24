package au.com.skater901.wc3.application.module

import com.google.inject.Guice
import com.google.inject.Provides
import dev.misfitlabs.kotlinguice4.KotlinModule
import dev.misfitlabs.kotlinguice4.getInstance
import io.dropwizard.core.setup.Environment
import io.github.classgraph.ClassInfoList
import jakarta.inject.Named
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.net.http.HttpClient
import java.util.*

class ClientModuleTest {
    @Test
    fun `httpclient should be singleton`() {
        val injector = Guice.createInjector(
            ClientModule(mock { on { allClasses } doReturn ClassInfoList.emptyList() }),
            object : KotlinModule() {
                @Provides
                @Named("configProperties")
                fun properties(): Properties = Properties()

                @Provides
                fun environment(): Environment = mock()
            }
        )

        injector.getInstance<HttpClient>()
            .use { client ->
                assertThat(client === injector.getInstance<HttpClient>()).isTrue()
            }
    }
}