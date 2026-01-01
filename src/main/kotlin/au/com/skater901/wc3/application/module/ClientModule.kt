package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.WC3NotificationBotConfiguration
import au.com.skater901.wc3.api.annotation.ClientFor
import au.com.skater901.wc3.application.provider.ClientProvider
import com.google.inject.Key
import com.google.inject.Provides
import com.google.inject.name.Names.named
import dev.misfitlabs.kotlinguice4.KotlinModule
import dev.misfitlabs.kotlinguice4.typeLiteral
import io.dropwizard.client.JerseyClientBuilder
import io.dropwizard.client.JerseyClientConfiguration
import io.dropwizard.core.setup.Environment
import io.github.classgraph.ClassRefTypeSignature
import io.github.classgraph.ScanResult
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton
import jakarta.ws.rs.client.Client
import java.net.URI
import java.net.http.HttpClient
import java.time.Duration
import java.util.*

internal class ClientModule(scanResult: ScanResult) : KotlinModule() {
    companion object {
        val defaultClientConfiguration = JerseyClientConfiguration().apply {
            maxThreads = 5
        }
    }

    private val clientAnnotations = scanResult.allClasses
        .asSequence()
        .flatMap { it.constructorInfo }
        .filter { it.hasAnnotation(Inject::class.java) }
        .flatMap { it.parameterInfo.toList() }
        .filter {
            it.hasAnnotation(ClientFor::class.java) &&
                    (it.typeDescriptor as? ClassRefTypeSignature)?.fullyQualifiedClassName == Client::class.java.name
        }
        .map { it.getAnnotationInfo(ClientFor::class.java).loadClassAndInstantiate() }
        .filterIsInstance<ClientFor>()
        .toList()

    override fun configure() {
        val environment = getProvider<Environment>()
        val clientConfiguration = getProvider(
            Key.get(
                typeLiteral<Map<String, @JvmSuppressWildcards URI>>(),
                named("clientConfiguration")
            )
        )
        val configProperties = getProvider(Key.get(Properties::class.java, named("configProperties")))

        clientAnnotations.forEach {
            bind<Client>().annotatedWith(it)
                .toProvider(ClientProvider(environment, it, clientConfiguration, configProperties))
        }
    }

    @Provides
    @Singleton
    fun getClient(): HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    @Inject
    @Provides
    @Singleton
    fun getJerseyClient(environment: Environment): Client =
        JerseyClientBuilder(environment).using(defaultClientConfiguration)
            .build("default-client")

    @Provides
    @Named("refreshInterval")
    @Inject
    fun getRefreshInterval(config: WC3NotificationBotConfiguration): Long = config.refreshInterval!!

    @Provides
    @Named("clientConfiguration")
    @Inject
    fun getClientConfiguration(config: WC3NotificationBotConfiguration): Map<String, URI> = config.clients!!
}