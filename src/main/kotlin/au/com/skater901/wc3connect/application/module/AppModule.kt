package au.com.skater901.wc3connect.application.module

import au.com.skater901.wc3connect.application.UnitOfWork
import au.com.skater901.wc3connect.core.gameProvider.GameProvider
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.multibindings.Multibinder
import io.github.classgraph.ScanResult
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.RetryRegistry
import jakarta.inject.Singleton

internal class AppModule(
    private val scanResult: ScanResult
) : AbstractModule() {
    override fun configure() {
        val binder = Multibinder.newSetBinder(binder(), GameProvider::class.java)

        scanResult.allClasses
            .filter { it.implementsInterface(GameProvider::class.java) }
            .forEach { binder.addBinding().to(it.loadClass() as Class<out GameProvider>) }

        requestStaticInjection(UnitOfWork::class.java)
    }

    @Provides
    @Singleton
    fun getRetryRegistry(): RetryRegistry = RetryRegistry.ofDefaults()

    @Provides
    @Singleton
    fun getCircuitBreakerRegistry(): CircuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults()

    @Provides
    @Singleton
    fun getObjectMapper(): ObjectMapper = ObjectMapper().registerKotlinModule()
        .registerModule(JavaTimeModule())
}