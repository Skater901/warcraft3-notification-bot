package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.api.NotificationModule
import au.com.skater901.wc3.api.scheduled.ScheduledTask
import au.com.skater901.wc3.discord.DiscordConfiguration
import au.com.skater901.wc3.discord.DiscordNotificationModule
import au.com.skater901.wc3.discord.core.handler.DiscordGameNotifier
import com.google.inject.Guice
import com.google.inject.Key
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NotificationModulesModuleTest {
    @Test
    fun `should load modules`() {
        val injector = Guice.createInjector(NotificationModulesModule())

        val modules = injector.getInstance(object : Key<List<@JvmSuppressWildcards NotificationModule<Any, *, *>>>() {})

        // This will have to be updated as more modules are added
        assertThat(modules).hasSize(1)
            .anyMatch {
                (it as? NotificationModule<DiscordConfiguration, DiscordGameNotifier, ScheduledTask>) is DiscordNotificationModule
            }
    }
}