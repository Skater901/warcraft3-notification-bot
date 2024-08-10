package au.com.skater901.w3cconnect.api.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class StopNotification : Command {
    override val name: String = "stopnotify"
    override val description: String = "Stop all game hosting notifications to this channel."

    override suspend fun handleCommand(command: SlashCommandInteractionEvent) {
        command.replySuspended("Notification stopped")
    }
}