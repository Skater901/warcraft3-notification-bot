package au.com.skater901.w3cconnect

import au.com.skater901.w3cconnect.api.commands.Command
import au.com.skater901.w3cconnect.api.commands.RegisterNotification
import au.com.skater901.w3cconnect.application.module.AppModule
import au.com.skater901.w3cconnect.application.module.ConfigModule
import com.google.inject.Guice
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.jdabuilder.intents
import dev.minn.jda.ktx.jdabuilder.light
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.GatewayIntent

fun main() {
    val injector = Guice.createInjector(AppModule(), ConfigModule())

    val config = injector.getInstance(WC3ConnectDiscordNotificationBotConfiguration::class.java)

    val commands = listOf(injector.getInstance(RegisterNotification::class.java))

    light(
        config.discordConfiguration.privateToken,
        enableCoroutines = true
    ) {
        intents += GatewayIntent.GUILD_MESSAGES
    }
        .registerCommands(commands)
}

private fun JDA.registerCommands(commands: List<Command>) {
    commands.forEach {
        listener<SlashCommandInteractionEvent> { event ->
            if (event.name == it.name) {
                it.handleCommand(event)
            }
        }
    }

    updateCommands().apply {
        commands.fold(this) { commands, command -> commands.slash(command.name, command.description) }

        queue()
    }
}