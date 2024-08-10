package au.com.skater901.w3cconnect.api.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface Command {
    val name: String
    val description: String
    val options: SlashCommandData.() -> Unit
        get() = {}

    suspend fun handleCommand(command: SlashCommandInteractionEvent)
}