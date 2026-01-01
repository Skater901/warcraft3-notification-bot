package au.com.skater901.wc3.core.job

import au.com.skater901.wc3.api.NotificationModule
import au.com.skater901.wc3.api.scheduled.ScheduledTask
import io.dropwizard.lifecycle.Managed
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

@Singleton
internal class TaskRunner @Inject constructor(
    private val scheduledTasks: Set<Pair<ScheduledTask, NotificationModule<*>>>
) : Managed {
    private val tasks = mutableListOf<ExecutorCoroutineDispatcher>()

    private var running = true

    override fun start() {
        scheduledTasks.forEach { (task, module) ->
            val dispatcher = newSingleThreadContext("${module.moduleName}-task").also { tasks.add(it) }

            CoroutineScope(dispatcher).launch {
                val logger = LoggerFactory.getLogger(module::class.java)

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
    }

    override fun stop() {
        running = false
        tasks.forEach { it.close() }
    }
}