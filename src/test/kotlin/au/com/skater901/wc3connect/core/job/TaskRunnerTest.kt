package au.com.skater901.wc3connect.core.job

import au.com.skater901.wc3connect.api.NotificationModule
import au.com.skater901.wc3connect.api.core.service.GameNotifier
import au.com.skater901.wc3connect.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3connect.api.scheduled.ScheduledTask
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Provides
import jakarta.inject.Inject
import jakarta.inject.Singleton
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

    private val moduleWithoutTask = object : NotificationModule<Any, GameNotifier, ScheduledTask> {
        override val moduleName: String = "moduleWithoutTask"
        override val configClass: KClass<Any> = Any::class

        override fun initializeNotificationHandlers(
            config: Any,
            injector: Injector,
            wc3GameNotificationService: WC3GameNotificationService
        ) {
        }
    }

    @Test
    fun `should ignore modules without tasks, run multiple tasks, and not let them block each other`() {
        val injector = Guice.createInjector(
            object : AbstractModule() {
                @Provides
                @Singleton
                fun getCounter(): AtomicInteger = module2Counter
            }
        )
        TaskRunner().use {
            it.runTask(Module1(), injector)
            it.runTask(Module2(), injector)
            it.runTask(moduleWithoutTask, injector)

            Thread.sleep(12_000)
        }

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
        val injector = Guice.createInjector()
        TaskRunner().use {
            it.runTask(ModuleWithBrokenTask(), injector)

            Thread.sleep(5000)
        }

        assertThat(brokenModuleCounter).isGreaterThanOrEqualTo(4)
    }
}