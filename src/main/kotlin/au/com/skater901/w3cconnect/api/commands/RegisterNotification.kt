package au.com.skater901.w3cconnect.api.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class RegisterNotification : Command {
    override val name: String = "notify"
    override val description: String =
        "Set the Warcraft III maps that you want to be announced to this channel when hosted on W3CConnect"

    override suspend fun handleCommand(command: SlashCommandInteractionEvent) {
        command.replySuspended("TODO")
    }
}