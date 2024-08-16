package au.com.skater901.wc3connect.discord.api.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

internal interface Command {
    val name: String
    val description: String
    val options: SlashCommandData.() -> Unit
        get() = {}

    suspend fun handleCommand(command: SlashCommandInteractionEvent)
}