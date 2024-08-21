package au.com.skater901.wc3connect.core.job

import au.com.skater901.wc3connect.api.NotificationModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import org.slf4j.LoggerFactory

internal class TaskRunner(
    modules: List<NotificationModule<*>>
) : AutoCloseable {
    private val modulesWithScheduledTasks = modules.filter { it.scheduledTask != null }
    private val taskPool = newFixedThreadPoolContext(modulesWithScheduledTasks.size, "task-pool")

    private var running = false

    fun start() {
        running = true
        modulesWithScheduledTasks.forEach { module ->
            CoroutineScope(taskPool).launch {
                val logger = LoggerFactory.getLogger(module::class.java)

                val task = module.scheduledTask!!

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

    override fun close() {
        running = false
        taskPool.close()
    }
}