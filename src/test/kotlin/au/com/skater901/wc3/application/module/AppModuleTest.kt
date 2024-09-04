package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.application.defaultUnitOfWork
import au.com.skater901.wc3.core.gameProvider.GameProvider
import au.com.skater901.wc3.core.gameProvider.WC3ConnectGameProvider
import au.com.skater901.wc3.core.gameProvider.WC3StatsGameProvider
import au.com.skater901.wc3.utils.getInstance
import au.com.skater901.wc3.utils.scanResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Guice
import com.google.inject.Key
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.RetryRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AppModuleTest {
    @Test
    fun `should configure registries and object mapper as singletons, and inject UnitOfWork`() {
        val injector = scanResult { Guice.createInjector(AppModule(it)) }

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

        assertThat(injector.getInstance(object : Key<Set<@JvmSuppressWildcards GameProvider>>() {})).hasSize(2)
            .anyMatch { it is WC3ConnectGameProvider }
            .anyMatch { it is WC3StatsGameProvider }
    }
}