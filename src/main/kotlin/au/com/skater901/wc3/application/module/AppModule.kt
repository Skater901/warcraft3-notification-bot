package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.core.gameProvider.GameProvider
import au.com.skater901.wc3.utilities.UnitOfWork
import com.codahale.metrics.MetricRegistry
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Provides
import dev.misfitlabs.kotlinguice4.KotlinModule
import dev.misfitlabs.kotlinguice4.multibindings.KotlinMultibinder.Companion.newSetBinder
import io.dropwizard.core.setup.Environment
import io.github.classgraph.ScanResult
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.RetryRegistry
import jakarta.inject.Inject
import jakarta.inject.Singleton

internal class AppModule(scanResult: ScanResult) : KotlinModule() {
    private val gameProviders = scanResult.allClasses
        .filter { it.implementsInterface(GameProvider::class.java) }
        .map { it.loadClass() as Class<out GameProvider> }

    override fun configure() {
        val binder = newSetBinder<GameProvider>(binder())

        gameProviders.forEach { binder.addBinding().to(it) }

        requestStaticInjection<UnitOfWork>()
    }

    @Provides
    @Inject
    fun provideObjectMapper(environment: Environment): ObjectMapper = environment.objectMapper

    @Provides
    @Inject
    fun provideMetricsRegistry(environment: Environment): MetricRegistry = environment.metrics()

    @Provides
    @Singleton
    fun getRetryRegistry(): RetryRegistry = RetryRegistry.ofDefaults()

    @Provides
    @Singleton
    fun getCircuitBreakerRegistry(): CircuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults()
}