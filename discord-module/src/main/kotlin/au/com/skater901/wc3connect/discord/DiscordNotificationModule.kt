package au.com.skater901.wc3connect.discord

import au.com.skater901.wc3connect.api.NotificationModule
import au.com.skater901.wc3connect.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3connect.api.scheduled.ScheduledTask
import au.com.skater901.wc3connect.discord.api.commands.*
import au.com.skater901.wc3connect.discord.core.handler.DiscordGameNotifier
import com.google.inject.AbstractModule
import com.google.inject.Injector
import com.google.inject.Provides
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.updateCommands
import dev.minn.jda.ktx.jdabuilder.intents
import dev.minn.jda.ktx.jdabuilder.light
import jakarta.inject.Inject
import jakarta.inject.Singleton
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import kotlin.reflect.KClass

public class DiscordNotificationModule : NotificationModule<DiscordConfiguration, DiscordGameNotifier, ScheduledTask> {
    override val moduleName: String = "discord"
    override val configClass: KClass<DiscordConfiguration> = DiscordConfiguration::class

    override fun guiceModule(): AbstractModule = object : AbstractModule() {
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
            injector.getCommand<RegisterNotification>(),
            injector.getCommand<StopNotification>(),
            injector.getCommand<Help>(),
            injector.getCommand<About>()
        )

        val jda = injector.getInstance(JDA::class.java)

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

    private inline fun <reified T : Command> Injector.getCommand(): T = getInstance(T::class.java)

    override val gameNotifierClass: KClass<DiscordGameNotifier> = DiscordGameNotifier::class
}