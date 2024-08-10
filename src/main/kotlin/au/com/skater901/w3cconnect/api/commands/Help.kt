package au.com.skater901.w3cconnect.api.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class Help : Command {
    override val name: String = "help"
    override val description: String = "Get help on how to use this bot."

    override suspend fun handleCommand(command: SlashCommandInteractionEvent) {
        // TODO better help message
        command.replySuspended("Use /notify to set up a notification in the current channel. Use /stopnotify to cancel notifications for the current channel.")
    }
}