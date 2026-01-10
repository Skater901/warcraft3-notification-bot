package au.com.skater901.wc3.application.healthcheck

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.annotation.Async
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import jakarta.inject.Inject

@Async(initialDelay = 10, period = 60)
internal class CircuitBreakersHealthCheck @Inject constructor(
    private val circuitBreakers: CircuitBreakerRegistry
) : HealthCheck() {
    override fun check(): Result = circuitBreakers.allCircuitBreakers
        .filter { it.state != CircuitBreaker.State.DISABLED && it.state != CircuitBreaker.State.CLOSED }
        .map { "${it.name}=${it.state}" }
        .let {
            if (it.isEmpty())
                Result.healthy()
            else
                Result.unhealthy(it.joinToString())
        }
}