package au.com.skater901.wc3.application.bundles

import au.com.skater901.wc3.WC3NotificationBotConfiguration
import au.com.skater901.wc3.application.database.MigrationsManager
import au.com.skater901.wc3.application.module.DatabaseModule
import com.google.inject.Injector
import dev.misfitlabs.kotlinguice4.KotlinModule
import dev.misfitlabs.kotlinguice4.getInstance
import io.dropwizard.core.setup.Environment

internal class DatabaseBundle(private val injectorProvider: () -> Injector) : BaseBundle {
    override val module: KotlinModule = DatabaseModule()

    override fun run(configuration: WC3NotificationBotConfiguration, environment: Environment) {
        // Run database migrations
        injectorProvider().getInstance<MigrationsManager>()
            .runMigrations()
    }
}