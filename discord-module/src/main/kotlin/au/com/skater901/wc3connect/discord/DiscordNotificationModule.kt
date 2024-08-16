package au.com.skater901.wc3connect.discord

import au.com.skater901.wc3connect.NotificationModule
import au.com.skater901.wc3connect.core.service.GameNotifier
import au.com.skater901.wc3connect.core.service.NotificationService
import au.com.skater901.wc3connect.discord.api.commands.Command
import au.com.skater901.wc3connect.discord.api.commands.Help
import au.com.skater901.wc3connect.discord.api.commands.RegisterNotification
import au.com.skater901.wc3connect.discord.api.commands.StopNotification
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

public class DiscordNotificationModule : NotificationModule<JDA, DiscordConfiguration> {
    override val moduleName: String = "discord"
    override val mainSystemClass: KClass<JDA> = JDA::class
    override val configClass: KClass<DiscordConfiguration> = DiscordConfiguration::class

    override fun guiceModule(): AbstractModule = object : AbstractModule() {
        @Provides
        @Singleton
        @Inject
        fun getJDA(config: DiscordConfiguration): JDA = light(
            config.privateToken,
            enableCoroutines = true
        ) {
            intents += GatewayIntent.GUILD_MESSAGES
        }
    }

    override fun initializeNotificationHandlers(
        mainClass: JDA,
        config: DiscordConfiguration,
        injector: Injector,
        notificationService: NotificationService
    ) {
        val commands = listOf(
            injector.getCommand<RegisterNotification>(),
            injector.getCommand<StopNotification>(),
            injector.getCommand<Help>()
        )

        commands.forEach {
            mainClass.listener<SlashCommandInteractionEvent> { event ->
                if (event.name == it.name) {
                    it.handleCommand(event)
                }
            }
        }

        mainClass.updateCommands {
            commands.forEach { command ->
                slash(command.name, command.description) {
                    command.options(this)
                }
            }
        }
            .queue()
    }

    private inline fun <reified T : Command> Injector.getCommand(): T = getInstance(T::class.java)

    override val gameNotifier: KClass<out GameNotifier> = DiscordGameNotifier::class
}