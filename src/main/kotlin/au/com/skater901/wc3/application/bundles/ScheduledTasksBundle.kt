package au.com.skater901.wc3.application.bundles

import au.com.skater901.wc3.WC3NotificationBotConfiguration
import au.com.skater901.wc3.application.module.ScheduledTasksModule
import au.com.skater901.wc3.core.job.TaskRunner
import com.google.inject.Injector
import dev.misfitlabs.kotlinguice4.getInstance
import io.dropwizard.core.setup.Environment

internal class ScheduledTasksBundle(private val injectorProvider: () -> Injector) : BaseBundle {
    override fun run(configuration: WC3NotificationBotConfiguration, environment: Environment) {
        environment.lifecycle().manage(
            injectorProvider().createChildInjector(ScheduledTasksModule(injectorProvider))
                .getInstance<TaskRunner>()
        )
    }
}