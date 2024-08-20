package au.com.skater901.wc3connect.core.job

import au.com.skater901.wc3connect.api.NotificationModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.LoggerFactory

internal class TaskRunner(
    modules: List<NotificationModule<*>>
) : AutoCloseable {
    private val modulesWithScheduledTasks = modules.filter { it.scheduledTask != null }
    private val taskPool = newFixedThreadPoolContext(modulesWithScheduledTasks.size, "task-pool")

    fun start() {
        modulesWithScheduledTasks.forEach { module ->
            CoroutineScope(taskPool).launch {
                val logger = LoggerFactory.getLogger(module::class.java)

                val semaphore = Semaphore(1)

                val task = module.scheduledTask!!

                val schedule = task.schedule * 1000L

                while (true) {
                    semaphore.withPermit {
                        try {
                            task.task()
                        } catch (t: Throwable) {
                            logger.error("Exception while running task:", t)
                        }
                    }

                    delay(schedule)
                }
            }
        }
    }

    override fun close() {
        taskPool.close()
    }
}