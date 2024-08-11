package au.com.skater901.w3cconnect.application

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.kotlin.circuitbreaker.decorateSuspendFunction
import io.github.resilience4j.kotlin.retry.decorateSuspendFunction
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KCallable

class UnitOfWork(private val javaClass: Class<*>, private val name: String) {
    companion object {
        private val RETRY_REGISTRY = AtomicReference(RetryRegistry.ofDefaults())
        private val CIRCUIT_BREAKER_REGISTRY = AtomicReference(CircuitBreakerRegistry.ofDefaults())

        private val retries = ConcurrentHashMap<String, Retry>()
        private val circuitBreakers = ConcurrentHashMap<String, CircuitBreaker>()

        @JvmStatic
        @Inject
        fun setRetries(retries: RetryRegistry) {
            RETRY_REGISTRY.set(retries)
        }

        @JvmStatic
        @Inject
        fun setCircuitBreakers(circuitBreakers: CircuitBreakerRegistry) {
            CIRCUIT_BREAKER_REGISTRY.set(circuitBreakers)
        }
    }

    // resilience
    private var retry: Retry? = null
    private var circuitBreaker: CircuitBreaker? = null

    var dispatcher: CoroutineDispatcher? = null
        private set

    private fun name(type: String): String = "${javaClass.packageName}.$name.$type"

    fun retry(config: RetryConfig.() -> Unit = {}): UnitOfWork {
        retry = retries.computeIfAbsent(name("retry")) { name ->
            RETRY_REGISTRY.get().retry(name, RetryConfig.ofDefaults().also(config))
        }
        return this
    }

    fun circuitBreaker(config: CircuitBreakerConfig.() -> Unit = {}): UnitOfWork {
        circuitBreaker = circuitBreakers.computeIfAbsent(name("circuitBreaker")) { name ->
            CIRCUIT_BREAKER_REGISTRY.get().circuitBreaker(
                name,
                CircuitBreakerConfig.ofDefaults().also(config)
            )
        }
        return this
    }

    fun withDispatcher(dispatcher: CoroutineDispatcher): UnitOfWork {
        this.dispatcher = dispatcher
        return this
    }

    suspend fun <T> execute(block: suspend () -> T): T = decorate(block)
        .let {
            val d = dispatcher
            if (d != null) {
                withContext(d) { it() }
            } else
                it()
        }

    suspend operator fun <T> invoke(block: suspend () -> T): T = execute(block = block)

    private fun <T> decorate(block: suspend () -> T): suspend () -> T {
        val resilience = retry?.decorateSuspendFunction(block) ?: block
        return circuitBreaker?.decorateSuspendFunction(resilience) ?: resilience
    }
}

inline fun <reified T : Any> T.unitOfWork(name: String) = UnitOfWork(this::class.java, name)

inline fun <reified T : Any> T.defaultUnitOfWork(function: KCallable<*>): UnitOfWork = defaultUnitOfWork(function.name)

inline fun <reified T : Any> T.defaultUnitOfWork(name: String) = unitOfWork(name)
    .retry()
    .circuitBreaker()