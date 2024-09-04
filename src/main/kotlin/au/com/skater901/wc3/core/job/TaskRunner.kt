package au.com.skater901.wc3.core.job

import au.com.skater901.wc3.api.NotificationModule
import com.google.inject.Injector
import jakarta.inject.Singleton
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

@Singleton
internal class TaskRunner : AutoCloseable {
    private val tasks = mutableListOf<ExecutorCoroutineDispatcher>()

    private var running = true

    fun runTask(module: NotificationModule<Any, *, *>, injector: Injector) {
        if (module.scheduledTask() == null && module.scheduledTaskClass == null) return

        val dispatcher = newSingleThreadContext("${module.moduleName}-task").also { tasks.add(it) }

        CoroutineScope(dispatcher).launch {
            val logger = LoggerFactory.getLogger(module::class.java)

            val task = module.scheduledTaskClass?.let { injector.getInstance(it.java) }
                ?: module.scheduledTask()!!

            val schedule = task.schedule * 1000L

            while (running) {
                try {
                    task.task()
                } catch (t: Throwable) {
                    logger.error("Exception while running task:", t)
                }

                delay(schedule)
            }
        }
    }

    override fun close() {
        running = false
        tasks.forEach { it.close() }
    }
}