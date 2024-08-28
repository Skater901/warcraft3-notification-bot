package au.com.skater901.wc3connect.application.module

import au.com.skater901.wc3connect.application.defaultUnitOfWork
import au.com.skater901.wc3connect.utils.getInstance
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

        val circuitBreakerRegistry = injector.getInstance<CircuitBreakerRegistry>()

        assertThat(circuitBreakerRegistry === injector.getInstance<CircuitBreakerRegistry>()).isTrue()

        val retryRegistry = injector.getInstance<RetryRegistry>()

        assertThat(retryRegistry === injector.getInstance<RetryRegistry>()).isTrue()

        val mapper = injector.getInstance<ObjectMapper>()

        assertThat(mapper === injector.getInstance<ObjectMapper>()).isTrue()

        assertThat(mapper.registeredModuleIds).contains("com.fasterxml.jackson.module.kotlin.KotlinModule")

        assertThat(circuitBreakerRegistry.allCircuitBreakers).isEmpty()
        assertThat(retryRegistry.allRetries).isEmpty()

        defaultUnitOfWork("testing")

        assertThat(circuitBreakerRegistry.allCircuitBreakers).hasSize(1)
        assertThat(retryRegistry.allRetries).hasSize(1)
    }
}