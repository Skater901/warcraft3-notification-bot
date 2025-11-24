package au.com.skater901.wc3.core.job

import au.com.skater901.wc3.api.NotificationModule
import au.com.skater901.wc3.api.core.service.GameNotifier
import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3.api.scheduled.ScheduledTask
import com.google.inject.Injector
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

class TaskRunnerTest {
    private var module1Counter = 0
    private val module2Counter = AtomicInteger(0)

    inner class Module1 : NotificationModule<Any, GameNotifier, ScheduledTask> {
        override val moduleName: String = "module1"
        override val configClass: KClass<Any> = Any::class

        override fun initializeNotificationHandlers(
            config: Any,
            injector: Injector,
            wc3GameNotificationService: WC3GameNotificationService
        ) {
        }

        override fun scheduledTask(): ScheduledTask = object : ScheduledTask {
            override val schedule: Int = 1

            override suspend fun task() {
                Thread.sleep(5000)

                module1Counter++
            }
        }
    }

    class ScheduledTask2 @Inject constructor(
        private val module2Counter: AtomicInteger
    ) : ScheduledTask {
        override val schedule: Int = 1

        override suspend fun task() {
            module2Counter.incrementAndGet()
        }
    }

    class Module2 : NotificationModule<Any, GameNotifier, ScheduledTask2> {
        override val moduleName: String = "module2"
        override val configClass: KClass<Any> = Any::class

        override fun initializeNotificationHandlers(
            config: Any,
            injector: Injector,
            wc3GameNotificationService: WC3GameNotificationService
        ) {
        }

        override val scheduledTaskClass = ScheduledTask2::class
    }

    @Test
    fun `should run multiple tasks and not let them block each other`() {
        val taskRunner = TaskRunner(
            setOf(
                Module1().scheduledTask() to Module1(),
                ScheduledTask2(module2Counter) to Module2()
            )
        )

        taskRunner.start()

        Thread.sleep(12_000)

        taskRunner.stop()

        assertThat(module1Counter).isEqualTo(2)
        assertThat(module2Counter.get()).isGreaterThan(10)
    }

    private var brokenModuleCounter = 0

    inner class ModuleWithBrokenTask : NotificationModule<Any, GameNotifier, ScheduledTask> {
        override val moduleName: String = "moduleWithBrokenTask"
        override val configClass: KClass<Any> = Any::class

        override fun initializeNotificationHandlers(
            config: Any,
            injector: Injector,
            wc3GameNotificationService: WC3GameNotificationService
        ) {
        }

        override fun scheduledTask(): ScheduledTask = object : ScheduledTask {
            override val schedule: Int = 1

            override suspend fun task() {
                brokenModuleCounter++
                throw RuntimeException("oh no!")
            }
        }
    }

    @Test
    fun `should handle exceptions when running scheduled tasks`() {
        val taskRunner = TaskRunner(setOf(ModuleWithBrokenTask().scheduledTask() to ModuleWithBrokenTask()))

        taskRunner.start()

        Thread.sleep(5000)

        taskRunner.stop()

        assertThat(brokenModuleCounter).isGreaterThanOrEqualTo(4)
    }
}