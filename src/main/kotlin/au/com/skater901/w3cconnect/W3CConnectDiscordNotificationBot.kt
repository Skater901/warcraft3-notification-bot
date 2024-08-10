package au.com.skater901.w3cconnect

import au.com.skater901.w3cconnect.api.commands.Command
import au.com.skater901.w3cconnect.api.commands.Help
import au.com.skater901.w3cconnect.api.commands.RegisterNotification
import au.com.skater901.w3cconnect.api.commands.StopNotification
import au.com.skater901.w3cconnect.application.database.MigrationsManager
import au.com.skater901.w3cconnect.application.module.AppModule
import au.com.skater901.w3cconnect.application.module.ConfigModule
import au.com.skater901.w3cconnect.application.module.DatabaseModule
import com.google.inject.Guice
import com.google.inject.Injector
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.updateCommands
import dev.minn.jda.ktx.jdabuilder.intents
import dev.minn.jda.ktx.jdabuilder.light
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.GatewayIntent

fun main() {
    val injector = Guice.createInjector(AppModule(), ConfigModule(), DatabaseModule())

    // Run database migrations
    injector.getInstance(MigrationsManager::class.java).runMigrations()

    val config = injector.getInstance(WC3ConnectDiscordNotificationBotConfiguration::class.java)

    // Register Discord bot commands
    val commands = listOf(
        injector.getCommand<RegisterNotification>(),
        injector.getCommand<StopNotification>(),
        injector.getCommand<Help>()
    )

    light(
        config.discordConfiguration.privateToken,
        enableCoroutines = true
    ) {
        intents += GatewayIntent.GUILD_MESSAGES
    }
        .registerCommands(commands)
}

private inline fun <reified T : Command> Injector.getCommand(): T = getInstance(T::class.java)

private fun JDA.registerCommands(commands: List<Command>) {
    commands.forEach {
        listener<SlashCommandInteractionEvent> { event ->
            if (event.name == it.name) {
                it.handleCommand(event)
            }
        }
    }

    updateCommands {
        commands.forEach { command ->
            slash(command.name, command.description) {
                command.options(this)
            }
        }
    }
        .queue()
}