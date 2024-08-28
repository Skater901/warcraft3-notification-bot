package au.com.skater901.wc3connect.application.managed

import au.com.skater901.wc3connect.api.NotificationModule
import au.com.skater901.wc3connect.api.core.service.GameNotifier
import au.com.skater901.wc3connect.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3connect.api.scheduled.ScheduledTask
import au.com.skater901.wc3connect.core.job.TaskRunner
import com.google.inject.AbstractModule
import com.google.inject.Injector
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import kotlin.reflect.KClass

class ModuleManagerTest {
    @Test
    fun `should not start multiple times`() {
        val guiceModule = mock<AbstractModule>()
        val module = mock<NotificationModule<Any, *, *>> {
            on { moduleName } doReturn "module1"
            on { gameNotifierClass } doReturn GameNotifier::class
            on { configClass } doReturn Any::class
            on { guiceModule() } doReturn guiceModule
        }
        val taskRunner = mock<TaskRunner>()
        val moduleManager = ModuleManager(listOf(module), taskRunner)

        val wc3NotificationService = mock<WC3GameNotificationService>()

        val notifier = mock<GameNotifier>()

        val childInjector = mock<Injector> {
            on { getInstance(Any::class.java) } doReturn "config class"
            on { getInstance(WC3GameNotificationService::class.java) } doReturn wc3NotificationService
            on { getInstance(GameNotifier::class.java) } doReturn notifier
        }

        val injector = mock<Injector> {
            on { createChildInjector(any(), eq(guiceModule)) } doReturn childInjector
        }

        repeat(10) {
            moduleManager.initializeModules(injector)
        }

        verify(module) {
            1 * { initializeNotificationHandlers("config class", childInjector, wc3NotificationService) }
        }
    }

    class DuplicateModule1 : NotificationModule<Any, GameNotifier, ScheduledTask> {
        override val moduleName: String = "module2"
        override val configClass: KClass<Any> = Any::class

        override fun initializeNotificationHandlers(
            config: Any,
            injector: Injector,
            wc3GameNotificationService: WC3GameNotificationService
        ) {
        }
    }

    class DuplicateModule2 : NotificationModule<Any, GameNotifier, ScheduledTask> {
        override val moduleName: String = "module2"
        override val configClass: KClass<Any> = Any::class

        override fun initializeNotificationHandlers(
            config: Any,
            injector: Injector,
            wc3GameNotificationService: WC3GameNotificationService
        ) {
        }
    }

    class DuplicateModule3 : NotificationModule<Any, GameNotifier, ScheduledTask> {
        override val moduleName: String = "module3"
        override val configClass: KClass<Any> = Any::class

        override fun initializeNotificationHandlers(
            config: Any,
            injector: Injector,
            wc3GameNotificationService: WC3GameNotificationService
        ) {
        }
    }

    class DuplicateModule4 : NotificationModule<Any, GameNotifier, ScheduledTask> {
        override val moduleName: String = "module3"
        override val configClass: KClass<Any> = Any::class

        override fun initializeNotificationHandlers(
            config: Any,
            injector: Injector,
            wc3GameNotificationService: WC3GameNotificationService
        ) {
        }
    }

    @Test
    fun `should validate module names are unique`() {
        val guiceModule = mock<AbstractModule>()
        val module = mock<NotificationModule<Any, *, *>> {
            on { moduleName } doReturn "module1"
            on { gameNotifierClass } doReturn GameNotifier::class
            on { configClass } doReturn Any::class
            on { guiceModule() } doReturn guiceModule
        }
        val taskRunner = mock<TaskRunner>()
        val moduleManager = ModuleManager(
            listOf(
                module,
                DuplicateModule1(),
                DuplicateModule2(),
                DuplicateModule3(),
                DuplicateModule4()
            ),
            taskRunner
        )

        val wc3NotificationService = mock<WC3GameNotificationService>()

        val notifier = mock<GameNotifier>()

        val childInjector = mock<Injector> {
            on { getInstance(Any::class.java) } doReturn "config class"
            on { getInstance(WC3GameNotificationService::class.java) } doReturn wc3NotificationService
            on { getInstance(GameNotifier::class.java) } doReturn notifier
        }

        val injector = mock<Injector> {
            on { createChildInjector(any(), eq(guiceModule)) } doReturn childInjector
        }

        assertThatThrownBy { moduleManager.initializeModules(injector) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Multiple modules registered with name [ module2 ]: au.com.skater901.wc3connect.application.managed.ModuleManagerTest.DuplicateModule1, au.com.skater901.wc3connect.application.managed.ModuleManagerTest.DuplicateModule2. Multiple modules registered with name [ module3 ]: au.com.skater901.wc3connect.application.managed.ModuleManagerTest.DuplicateModule3, au.com.skater901.wc3connect.application.managed.ModuleManagerTest.DuplicateModule4.")
    }

    @Test
    fun `should validate all modules provide a GameNotifier`() {
        val guiceModule = mock<AbstractModule>()
        val module = mock<NotificationModule<Any, *, *>> {
            on { moduleName } doReturn "module1"
            on { gameNotifierClass } doReturn GameNotifier::class
            on { configClass } doReturn Any::class
            on { guiceModule() } doReturn guiceModule
        }
        val gameNotifier = mock<GameNotifier>()
        val moduleWithGameNotifierInstance = mock<NotificationModule<Any, *, *>> {
            on { moduleName } doReturn "module2"
            on { gameNotifier() } doReturn gameNotifier
            on { configClass } doReturn Any::class
            on { guiceModule() } doReturn guiceModule
        }
        val moduleWithoutGameNotifier = mock<NotificationModule<Any, *, *>> {
            on { moduleName } doReturn "module3"
            on { configClass } doReturn Any::class
            on { guiceModule() } doReturn guiceModule
        }
        val moduleWithoutGameNotifier2 = mock<NotificationModule<Any, *, *>> {
            on { moduleName } doReturn "module4"
            on { configClass } doReturn Any::class
            on { guiceModule() } doReturn guiceModule
        }
        val taskRunner = mock<TaskRunner>()
        val moduleManager = ModuleManager(
            listOf(
                module,
                moduleWithGameNotifierInstance,
                moduleWithoutGameNotifier,
                moduleWithoutGameNotifier2
            ),
            taskRunner
        )

        val wc3NotificationService = mock<WC3GameNotificationService>()

        val notifier = mock<GameNotifier>()

        val childInjector = mock<Injector> {
            on { getInstance(Any::class.java) } doReturn "config class"
            on { getInstance(WC3GameNotificationService::class.java) } doReturn wc3NotificationService
            on { getInstance(GameNotifier::class.java) } doReturn notifier
        }

        val injector = mock<Injector> {
            on { createChildInjector(any(), eq(guiceModule)) } doReturn childInjector
        }

        assertThatThrownBy { moduleManager.initializeModules(injector) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Module [ module3 ] does not provide a GameNotifier. Module [ module4 ] does not provide a GameNotifier.")
    }

    @Test
    fun `should validate all modules provide a GameNotifier instance or class, not both`() {
        val guiceModule = mock<AbstractModule>()
        val module = mock<NotificationModule<Any, *, *>> {
            on { moduleName } doReturn "module1"
            on { gameNotifierClass } doReturn GameNotifier::class
            on { configClass } doReturn Any::class
            on { guiceModule() } doReturn guiceModule
        }
        val gameNotifier = mock<GameNotifier>()
        val moduleWithGameNotifierInstance = mock<NotificationModule<Any, *, *>> {
            on { moduleName } doReturn "module2"
            on { gameNotifier() } doReturn gameNotifier
            on { configClass } doReturn Any::class
            on { guiceModule() } doReturn guiceModule
        }
        val moduleWithGameNotifierInstanceAndClass1 = mock<NotificationModule<Any, *, *>> {
            on { moduleName } doReturn "module3"
            on { configClass } doReturn Any::class
            on { guiceModule() } doReturn guiceModule
            on { gameNotifier() } doReturn gameNotifier
            on { gameNotifierClass } doReturn GameNotifier::class
        }
        val moduleWithGameNotifierInstanceAndClass2 = mock<NotificationModule<Any, *, *>> {
            on { moduleName } doReturn "module4"
            on { configClass } doReturn Any::class
            on { guiceModule() } doReturn guiceModule
            on { gameNotifier() } doReturn gameNotifier
            on { gameNotifierClass } doReturn GameNotifier::class
        }
        val taskRunner = mock<TaskRunner>()
        val moduleManager = ModuleManager(
            listOf(
                module,
                moduleWithGameNotifierInstance,
                moduleWithGameNotifierInstanceAndClass1,
                moduleWithGameNotifierInstanceAndClass2
            ),
            taskRunner
        )

        val wc3NotificationService = mock<WC3GameNotificationService>()

        val notifier = mock<GameNotifier>()

        val childInjector = mock<Injector> {
            on { getInstance(Any::class.java) } doReturn "config class"
            on { getInstance(WC3GameNotificationService::class.java) } doReturn wc3NotificationService
            on { getInstance(GameNotifier::class.java) } doReturn notifier
        }

        val injector = mock<Injector> {
            on { createChildInjector(any(), eq(guiceModule)) } doReturn childInjector
        }

        assertThatThrownBy { moduleManager.initializeModules(injector) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Module [ module3 ] provides both a GameNotifier instance and GameNotifier class. Please provide one or the other, not both. Module [ module4 ] provides both a GameNotifier instance and GameNotifier class. Please provide one or the other, not both.")
    }

    @Test
    fun `should validate no modules provide a ScheduledTask instance and class`() {
        val guiceModule = mock<AbstractModule>()
        val scheduledTask = mock<ScheduledTask>()
        val module = mock<NotificationModule<Any, *, *>> {
            on { moduleName } doReturn "module1"
            on { gameNotifierClass } doReturn GameNotifier::class
            on { configClass } doReturn Any::class
            on { guiceModule() } doReturn guiceModule
            on { scheduledTask() } doReturn scheduledTask
        }
        val gameNotifier = mock<GameNotifier>()
        val moduleWithScheduledTaskClass = mock<NotificationModule<Any, *, *>> {
            on { moduleName } doReturn "module2"
            on { gameNotifier() } doReturn gameNotifier
            on { configClass } doReturn Any::class
            on { guiceModule() } doReturn guiceModule
            on { scheduledTaskClass } doReturn ScheduledTask::class
        }
        val moduleWithScheduledTaskClassAndInstance1 = mock<NotificationModule<Any, *, *>> {
            on { moduleName } doReturn "module3"
            on { configClass } doReturn Any::class
            on { guiceModule() } doReturn guiceModule
            on { gameNotifierClass } doReturn GameNotifier::class
            on { scheduledTask() } doReturn scheduledTask
            on { scheduledTaskClass } doReturn ScheduledTask::class
        }
        val moduleWithScheduledTaskClassAndInstance2 = mock<NotificationModule<Any, *, *>> {
            on { moduleName } doReturn "module4"
            on { configClass } doReturn Any::class
            on { guiceModule() } doReturn guiceModule
            on { gameNotifier() } doReturn gameNotifier
            on { scheduledTask() } doReturn scheduledTask
            on { scheduledTaskClass } doReturn ScheduledTask::class
        }
        val taskRunner = mock<TaskRunner>()
        val moduleManager = ModuleManager(
            listOf(
                module,
                moduleWithScheduledTaskClass,
                moduleWithScheduledTaskClassAndInstance1,
                moduleWithScheduledTaskClassAndInstance2
            ),
            taskRunner
        )

        val wc3NotificationService = mock<WC3GameNotificationService>()

        val notifier = mock<GameNotifier>()

        val childInjector = mock<Injector> {
            on { getInstance(Any::class.java) } doReturn "config class"
            on { getInstance(WC3GameNotificationService::class.java) } doReturn wc3NotificationService
            on { getInstance(GameNotifier::class.java) } doReturn notifier
        }

        val injector = mock<Injector> {
            on { createChildInjector(any(), eq(guiceModule)) } doReturn childInjector
        }

        assertThatThrownBy { moduleManager.initializeModules(injector) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Module [ module3 ] provides both a scheduled task instance and scheduled task class for dependency injection. Please provide one or the other, not both. Module [ module4 ] provides both a scheduled task instance and scheduled task class for dependency injection. Please provide one or the other, not both.")
    }

    @Test
    fun `should run scheduled tasks`() {
        val guiceModule = mock<AbstractModule>()
        val scheduledTask = mock<ScheduledTask>()
        val moduleWithScheduledTaskInstance = mock<NotificationModule<Any, *, *>> {
            on { moduleName } doReturn "module1"
            on { gameNotifierClass } doReturn GameNotifier::class
            on { configClass } doReturn Any::class
            on { guiceModule() } doReturn guiceModule
            on { scheduledTask() } doReturn scheduledTask
        }
        val gameNotifier = mock<GameNotifier>()
        val moduleWithScheduledTaskClass = mock<NotificationModule<Any, *, *>> {
            on { moduleName } doReturn "module2"
            on { gameNotifier() } doReturn gameNotifier
            on { configClass } doReturn Any::class
            on { guiceModule() } doReturn guiceModule
            on { scheduledTaskClass } doReturn ScheduledTask::class
        }
        val taskRunner = mock<TaskRunner>()
        val moduleManager = ModuleManager(
            listOf(moduleWithScheduledTaskInstance, moduleWithScheduledTaskClass),
            taskRunner
        )

        val wc3NotificationService = mock<WC3GameNotificationService>()

        val notifier = mock<GameNotifier>()

        val childInjector = mock<Injector> {
            on { getInstance(Any::class.java) } doReturn "config class"
            on { getInstance(WC3GameNotificationService::class.java) } doReturn wc3NotificationService
            on { getInstance(GameNotifier::class.java) } doReturn notifier
        }

        val injector = mock<Injector> {
            on { createChildInjector(any(), eq(guiceModule)) } doReturn childInjector
        }

        moduleManager.initializeModules(injector)

        verify(taskRunner) {
            1 * { runTask(moduleWithScheduledTaskInstance, injector) }
            1 * { runTask(moduleWithScheduledTaskClass, injector) }
        }
    }

    @Test
    fun `should provide Map of GameNotifier instances`() {
        val guiceModule = mock<AbstractModule>()
        val module1 = mock<NotificationModule<Any, *, *>> {
            on { moduleName } doReturn "module1"
            on { gameNotifierClass } doReturn GameNotifier::class
            on { configClass } doReturn Any::class
            on { guiceModule() } doReturn guiceModule
        }
        val gameNotifier = mock<GameNotifier>()
        val module2 = mock<NotificationModule<Any, *, *>> {
            on { moduleName } doReturn "module2"
            on { gameNotifier() } doReturn gameNotifier
            on { configClass } doReturn Any::class
            on { guiceModule() } doReturn guiceModule
        }
        val taskRunner = mock<TaskRunner>()
        val moduleManager = ModuleManager(listOf(module1, module2), taskRunner)

        val wc3NotificationService = mock<WC3GameNotificationService>()

        val notifier = mock<GameNotifier>()

        val childInjector = mock<Injector> {
            on { getInstance(Any::class.java) } doReturn "config class"
            on { getInstance(WC3GameNotificationService::class.java) } doReturn wc3NotificationService
            on { getInstance(GameNotifier::class.java) } doReturn notifier
        }

        val injector = mock<Injector> {
            on { createChildInjector(any(), eq(guiceModule)) } doReturn childInjector
        }

        moduleManager.initializeModules(injector)

        val gameNotifiers = moduleManager.getGameNotifiers()

        assertThat(gameNotifiers).hasSize(2)
            .containsEntry("module1", notifier)
            .containsEntry("module2", gameNotifier)
    }
}