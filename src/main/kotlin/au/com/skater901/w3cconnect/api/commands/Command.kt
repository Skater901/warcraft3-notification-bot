package au.com.skater901.w3cconnect.api.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

interface Command {
    val name: String
    val description: String
        get() = ""
    val options: List<String>
        get() = emptyList()

    suspend fun handleCommand(command: SlashCommandInteractionEvent)
}