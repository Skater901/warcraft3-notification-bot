package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.application.config.ApplicationConfiguration
import au.com.skater901.wc3.utils.getInstance
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Provides
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.http.HttpClient

class ClientModuleTest {
    @Test
    fun `httpclient should be singleton`() {
        val injector = Guice.createInjector(
            ClientModule(),
            object : AbstractModule() {
                @Provides
                fun getConfig() = ApplicationConfiguration(5)
            }
        )

        val client = injector.getInstance<HttpClient>()

        assertThat(client === injector.getInstance<HttpClient>()).isTrue()

        client.close()
    }
}