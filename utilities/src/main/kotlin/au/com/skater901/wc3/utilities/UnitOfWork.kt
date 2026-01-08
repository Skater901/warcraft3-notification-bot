package au.com.skater901.wc3.utilities

import com.codahale.metrics.*
import com.codahale.metrics.MetricRegistry.name
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.kotlin.circuitbreaker.decorateSuspendFunction
import io.github.resilience4j.kotlin.retry.decorateSuspendFunction
import io.github.resilience4j.metrics.CircuitBreakerMetrics
import io.github.resilience4j.metrics.RetryMetrics
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KFunction

public class UnitOfWork(private val javaClass: Class<*>, private val name: String) {
    public companion object {
        private val METRIC_REGISTRY = AtomicReference(MetricRegistry())

        private val RETRY_REGISTRY = AtomicReference(RetryRegistry.ofDefaults())
        private val CIRCUIT_BREAKER_REGISTRY = AtomicReference(CircuitBreakerRegistry.ofDefaults())

        @JvmStatic
        @Inject
        public fun setMetricRegistry(metricRegistry: MetricRegistry) {
            METRIC_REGISTRY.set(metricRegistry)
        }

        @JvmStatic
        @Inject
        public fun setRetries(retries: RetryRegistry) {
            RETRY_REGISTRY.set(retries)
        }

        @JvmStatic
        @Inject
        public fun setCircuitBreakers(circuitBreakers: CircuitBreakerRegistry) {
            CIRCUIT_BREAKER_REGISTRY.set(circuitBreakers)
        }
    }

    // metrics
    private var timer: Timer? = null
    private var meter: Meter? = null
    private var exceptionMeter: Meter? = null
    private var activeCounter: Counter? = null
    private var totalCounter: Counter? = null

    // resilience
    private var retry: Retry? = null
    private var circuitBreaker: CircuitBreaker? = null

    private var dispatcher: CoroutineDispatcher? = null

    private fun name(vararg type: String) = name(javaClass, name, *type)

    public fun timed(): UnitOfWork {
        timer = METRIC_REGISTRY.get().timer(name("timer"))
        return this
    }

    public fun metered(): UnitOfWork {
        meter = METRIC_REGISTRY.get().meter(name("meter"))
        return this
    }

    public fun exceptionMetered(): UnitOfWork {
        exceptionMeter = METRIC_REGISTRY.get().meter(name("exceptions", "meter"))
        return this
    }

    public fun activeCounted(): UnitOfWork {
        activeCounter = METRIC_REGISTRY.get().counter(name("activeCount"))
        return this
    }

    public fun totalCounted(): UnitOfWork {
        totalCounter = METRIC_REGISTRY.get().counter(name("totalCount"))
        return this
    }

    public fun retry(config: RetryConfig.() -> Unit = {}): UnitOfWork {
        val name = name("retry")
        retry = RETRY_REGISTRY.get()
            .retry(name, RetryConfig.ofDefaults().also(config))
            .also { METRIC_REGISTRY.get().tryRegister(name, RetryMetrics.ofRetry(it)) }
        return this
    }

    public fun circuitBreaker(config: CircuitBreakerConfig.() -> Unit = {}): UnitOfWork {
        val name = name("circuitBreaker")
        circuitBreaker = CIRCUIT_BREAKER_REGISTRY.get()
            .circuitBreaker(
                name,
                CircuitBreakerConfig.ofDefaults().also(config)
            )
            .also { METRIC_REGISTRY.get().tryRegister(name, CircuitBreakerMetrics.ofCircuitBreaker(it)) }
        return this
    }

    public fun withDispatcher(dispatcher: CoroutineDispatcher): UnitOfWork {
        this.dispatcher = dispatcher
        return this
    }

    private suspend fun <T> execute(block: suspend () -> T): T = decorate(block)
        .let {
            val d = dispatcher
            if (d != null) {
                withContext(d) { it() }
            } else
                it()
        }

    public suspend operator fun <T> invoke(block: suspend () -> T): T = execute(block = block)

    private fun <T> decorate(block: suspend () -> T): suspend () -> T {
        val resilience = retry?.decorateSuspendFunction(block) ?: block
        return circuitBreaker?.decorateSuspendFunction(resilience) ?: resilience
    }

    private fun MetricRegistry.tryRegister(name: String, metrics: MetricSet) {
        if (this.metrics.keys.none { it.startsWith(name) }) {
            registerAll(name, metrics)
        }
    }
}

public inline fun <reified T : Any> T.unitOfWork(name: String): UnitOfWork = UnitOfWork(this::class.java, name)

public inline fun <reified T : Any> T.metricsWork(function: KFunction<*>): UnitOfWork = metricsWork(function.name)

public inline fun <reified T : Any> T.metricsWork(name: String): UnitOfWork = unitOfWork(name)
    .timed()
    .exceptionMetered()

public inline fun <reified T : Any> T.defaultUnitOfWork(function: KFunction<*>): UnitOfWork =
    defaultUnitOfWork(function.name)

public inline fun <reified T : Any> T.defaultUnitOfWork(name: String): UnitOfWork = unitOfWork(name)
    .timed()
    .exceptionMetered()
    .retry()
    .circuitBreaker()