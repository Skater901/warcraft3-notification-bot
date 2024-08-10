package au.com.skater901.w3cconnect.api.commands

import au.com.skater901.w3cconnect.core.service.NotificationService
import jakarta.inject.Inject
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class StopNotification @Inject constructor(
    private val notificationService: NotificationService
) : Command {
    override val name: String = "stopnotify"
    override val description: String = "Stop all game hosting notifications to this channel."

    override suspend fun handleCommand(command: SlashCommandInteractionEvent) {
        notificationService.deleteNotification(command.channelIdLong)
        command.replySuspended("Notification stopped for channel [ ${command.channel.name} (${command.channelIdLong}) ]")
    }
}