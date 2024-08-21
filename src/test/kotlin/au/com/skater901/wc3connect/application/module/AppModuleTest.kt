package au.com.skater901.wc3connect.application.module

import au.com.skater901.wc3connect.application.defaultUnitOfWork
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Guice
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.RetryRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AppModuleTest {
    @Test
    fun `should configure registries and object mapper as singletons, and inject UnitOfWork`() {
        val injector = Guice.createInjector(AppModule())

        val circuitBreakerRegistry = injector.getInstance(CircuitBreakerRegistry::class.java)

        assertThat(circuitBreakerRegistry === injector.getInstance(CircuitBreakerRegistry::class.java)).isTrue()

        val retryRegistry = injector.getInstance(RetryRegistry::class.java)

        assertThat(retryRegistry === injector.getInstance(RetryRegistry::class.java)).isTrue()

        val mapper = injector.getInstance(ObjectMapper::class.java)

        assertThat(mapper === injector.getInstance(ObjectMapper::class.java)).isTrue()

        assertThat(mapper.registeredModuleIds).contains("com.fasterxml.jackson.module.kotlin.KotlinModule")

        assertThat(circuitBreakerRegistry.allCircuitBreakers).isEmpty()
        assertThat(retryRegistry.allRetries).isEmpty()

        defaultUnitOfWork("testing")

        assertThat(circuitBreakerRegistry.allCircuitBreakers).hasSize(1)
        assertThat(retryRegistry.allRetries).hasSize(1)
    }
}