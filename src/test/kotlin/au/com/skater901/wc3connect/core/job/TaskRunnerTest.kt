package au.com.skater901.wc3connect.core.job

import au.com.skater901.wc3connect.api.NotificationModule
import au.com.skater901.wc3connect.api.core.service.GameNotifier
import au.com.skater901.wc3connect.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3connect.api.scheduled.ScheduledTask
import com.google.inject.Injector
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

class TaskRunnerTest {
    private var module1Counter = 0
    private var module2Counter = 0

    inner class Module1 : NotificationModule<Any> {
        override val moduleName: String
            get() = TODO("Not yet implemented")
        override val configClass: KClass<Any>
            get() = TODO("Not yet implemented")

        override fun initializeNotificationHandlers(
            config: Any,
            injector: Injector,
            wc3GameNotificationService: WC3GameNotificationService
        ) {
            TODO("Not yet implemented")
        }

        override val gameNotifier: KClass<out GameNotifier>
            get() = TODO("Not yet implemented")

        override val scheduledTask: ScheduledTask = object : ScheduledTask {
            override val schedule: Int = 1

            override suspend fun task() {
                Thread.sleep(5000)

                module1Counter++
            }
        }
    }

    inner class Module2 : NotificationModule<Any> {
        override val moduleName: String
            get() = TODO("Not yet implemented")
        override val configClass: KClass<Any>
            get() = TODO("Not yet implemented")

        override fun initializeNotificationHandlers(
            config: Any,
            injector: Injector,
            wc3GameNotificationService: WC3GameNotificationService
        ) {
            TODO("Not yet implemented")
        }

        override val gameNotifier: KClass<out GameNotifier>
            get() = TODO("Not yet implemented")

        override val scheduledTask: ScheduledTask = object : ScheduledTask {
            override val schedule: Int = 1

            override suspend fun task() {
                module2Counter++
            }
        }
    }

    private val moduleWithoutTask = object : NotificationModule<Any> {
        override val moduleName: String
            get() = TODO("Not yet implemented")
        override val configClass: KClass<Any>
            get() = TODO("Not yet implemented")

        override fun initializeNotificationHandlers(
            config: Any,
            injector: Injector,
            wc3GameNotificationService: WC3GameNotificationService
        ) {
            TODO("Not yet implemented")
        }

        override val gameNotifier: KClass<out GameNotifier>
            get() = TODO("Not yet implemented")
    }

    @Test
    fun `should ignore modules without tasks, run multiple tasks, and not let them block each other`() {
        TaskRunner(listOf(Module1(), Module2(), moduleWithoutTask)).use {
            it.start()

            Thread.sleep(12_000)
        }

        assertThat(module1Counter).isEqualTo(2)
        assertThat(module2Counter).isGreaterThan(10)
    }

    private var brokenModuleCounter = 0

    inner class ModuleWithBrokenTask : NotificationModule<Any> {
        override val moduleName: String
            get() = TODO("Not yet implemented")
        override val configClass: KClass<Any>
            get() = TODO("Not yet implemented")

        override fun initializeNotificationHandlers(
            config: Any,
            injector: Injector,
            wc3GameNotificationService: WC3GameNotificationService
        ) {
            TODO("Not yet implemented")
        }

        override val gameNotifier: KClass<out GameNotifier>
            get() = TODO("Not yet implemented")

        override val scheduledTask: ScheduledTask = object : ScheduledTask {
            override val schedule: Int = 1

            override suspend fun task() {
                brokenModuleCounter++
                throw RuntimeException("oh no!")
            }
        }
    }

    @Test
    fun `should handle exceptions when running scheduled tasks`() {
        TaskRunner(listOf(ModuleWithBrokenTask())).use {
            it.start()

            Thread.sleep(5000)
        }

        assertThat(brokenModuleCounter).isGreaterThanOrEqualTo(4)
    }
}