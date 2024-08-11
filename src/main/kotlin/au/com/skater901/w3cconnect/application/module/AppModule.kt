package au.com.skater901.w3cconnect.application.module

import au.com.skater901.w3cconnect.application.UnitOfWork
import com.google.inject.AbstractModule
import com.google.inject.Provides
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.RetryRegistry
import jakarta.inject.Singleton

class AppModule : AbstractModule() {
    override fun configure() {
        requestStaticInjection(UnitOfWork::class.java)
    }

    @Provides
    @Singleton
    fun getRetryRegistry(): RetryRegistry = RetryRegistry.ofDefaults()

    @Provides
    @Singleton
    fun getCircuitBreakerRegistry(): CircuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults()
}