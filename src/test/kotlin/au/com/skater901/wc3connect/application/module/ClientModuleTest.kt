package au.com.skater901.wc3connect.application.module

import au.com.skater901.wc3connect.application.config.GamesConfiguration
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Provides
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpClient

class ClientModuleTest {
    @Test
    fun `httpclient should be singleton`() {
        val injector = Guice.createInjector(
            ClientModule(),
            object : AbstractModule() {
                @Provides
                fun getConfig() = GamesConfiguration(URI("http://localhost"), 5)
            }
        )

        val client = injector.getInstance(HttpClient::class.java)

        assertThat(client === injector.getInstance(HttpClient::class.java)).isTrue()

        client.close()
    }
}