package au.com.skater901.wc3connect.application.module

import au.com.skater901.wc3connect.application.UnitOfWork
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.inject.AbstractModule
import com.google.inject.Provides
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.RetryRegistry
import jakarta.inject.Singleton

internal class AppModule : AbstractModule() {
    override fun configure() {
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
}