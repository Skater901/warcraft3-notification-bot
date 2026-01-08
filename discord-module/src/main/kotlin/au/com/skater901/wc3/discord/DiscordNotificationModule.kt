package au.com.skater901.wc3.discord

import au.com.skater901.wc3.api.NotificationModule
import au.com.skater901.wc3.api.core.service.AdminMessageNotifier
import au.com.skater901.wc3.api.core.service.GameNotifier
import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3.discord.annotations.DiscordModule
import au.com.skater901.wc3.discord.api.commands.*
import au.com.skater901.wc3.discord.core.dao.RoleNotificationDAO
import au.com.skater901.wc3.discord.core.dao.jdbi.JdbiRoleNotificationDAO
import au.com.skater901.wc3.discord.core.handler.DiscordGameNotifier
import au.com.skater901.wc3.discord.core.notifier.AdminNotifier
import com.google.inject.AbstractModule
import com.google.inject.Injector
import com.google.inject.Provides
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.updateCommands
import dev.minn.jda.ktx.jdabuilder.intents
import dev.minn.jda.ktx.jdabuilder.light
import dev.misfitlabs.kotlinguice4.KotlinModule
import dev.misfitlabs.kotlinguice4.getInstance
import jakarta.inject.Inject
import jakarta.inject.Singleton
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import kotlin.reflect.KClass

public class DiscordNotificationModule : NotificationModule<DiscordConfiguration> {
    override val moduleName: String = "discord"
    override val configClass: KClass<DiscordConfiguration> = DiscordConfiguration::class
    override val annotation: KClass<out Annotation> = DiscordModule::class

    override val guiceModule: AbstractModule = object : KotlinModule() {
        override fun configure() {
            bind<RoleNotificationDAO>().to<JdbiRoleNotificationDAO>()
        }

        @Provides
        @Singleton
        @Inject
        fun getJDA(config: DiscordConfiguration): JDA = light(
            config.privateToken,
            enableCoroutines = true
        ) {
            intents -= GatewayIntent.entries // disable all intents, none are needed
        }
    }

    override fun initializeNotificationHandlers(
        config: DiscordConfiguration,
        injector: Injector,
        wc3GameNotificationService: WC3GameNotificationService
    ) {
        val commands = listOf(
            injector.getInstance<RegisterNotification>(),
            injector.getInstance<StopNotification>(),
            injector.getInstance<Help>(),
            injector.getInstance<About>(),
            injector.getInstance<HealthCheck>()
        )

        val jda = injector.getInstance<JDA>()

        commands.forEach {
            jda.listener<SlashCommandInteractionEvent> { event ->
                if (event.name == it.name) {
                    it.handleCommand(event)
                }
            }
        }

        jda.updateCommands {
            commands.forEach { command ->
                slash(command.name, command.description) {
                    command.options(this)

                    defaultPermissions = command.defaultPermissions
                }
            }
        }
            .queue()
    }

    override val gameNotifier: KClass<out GameNotifier> = DiscordGameNotifier::class

    override val adminMessageNotifier: KClass<out AdminMessageNotifier> = AdminNotifier::class
}