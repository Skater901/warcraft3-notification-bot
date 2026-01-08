package au.com.skater901.wc3.application.provider

import au.com.skater901.wc3.api.annotation.ClientFor
import au.com.skater901.wc3.application.module.ClientModule.Companion.defaultClientConfiguration
import io.dropwizard.client.JerseyClientBuilder
import io.dropwizard.core.setup.Environment
import jakarta.inject.Provider
import jakarta.ws.rs.client.Client
import jakarta.ws.rs.client.ClientRequestFilter
import java.net.URI
import java.util.*

internal class ClientProvider(
    private val environment: Provider<Environment>,
    private val clientFor: ClientFor,
    private val clientConfiguration: Provider<Map<String, URI>>,
    private val configProperties: Provider<Properties>
) : Provider<Client> {
    override fun get(): Client? = JerseyClientBuilder(environment.get()).using(defaultClientConfiguration)
        .build(clientFor.value)
        .let {
            (
                    clientConfiguration.get()[clientFor.value]
                        ?: configProperties.get()["client.${clientFor.value}"]
                            ?.let { baseUriString -> URI.create(baseUriString.toString()) }
                    )
                ?.let { baseUri ->
                    it.register(ClientRequestFilter { request ->
                        request.uri = URI.create(baseUri.toString() + request.uri.toString())
                    })
                }
                ?: it
        }
}