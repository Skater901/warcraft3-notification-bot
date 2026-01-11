package au.com.skater901.wc3.application.provider

import au.com.skater901.wc3.extras.annotation.ExecutorFor
import io.dropwizard.core.setup.Environment
import jakarta.inject.Provider
import java.util.*
import java.util.concurrent.ExecutorService

internal class ExecutorProvider(
    private val environment: Provider<Environment>,
    private val configProperties: Provider<Properties>,
    private val annotation: ExecutorFor
) : Provider<ExecutorService> {
    private lateinit var executor: ExecutorService

    override fun get(): ExecutorService {
        synchronized(this) {
            if (!::executor.isInitialized) {
                val numberOfThreadsPropertyName = "${annotation.value}.threads"

                val numberOfThreads = configProperties.get()
                    .getProperty(numberOfThreadsPropertyName)
                    ?.let {
                        it.toIntOrNull()
                            ?: throw IllegalArgumentException("[ $numberOfThreadsPropertyName ] value [ $it ] is not a valid number.")
                    }
                    ?: throw IllegalArgumentException("Cannot find config property [ $numberOfThreadsPropertyName ]")

                executor = environment.get()
                    .lifecycle()
                    .executorService(annotation.value)
                    .minThreads(numberOfThreads)
                    .maxThreads(numberOfThreads)
                    .build()
            }
        }

        return executor
    }
}