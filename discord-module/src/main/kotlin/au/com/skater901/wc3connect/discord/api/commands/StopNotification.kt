package au.com.skater901.wc3connect.discord.api.commands

import au.com.skater901.wc3connect.api.core.service.WC3GameNotificationService
import jakarta.inject.Inject
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions

internal class StopNotification @Inject constructor(
    private val wc3GameNotificationService: WC3GameNotificationService
) : Command {
    override val name: String = "stopnotify"
    override val description: String = "Stop all game hosting notifications to this channel."

    override val defaultPermissions: DefaultMemberPermissions = DefaultMemberPermissions.DISABLED

    override suspend fun handleCommand(command: SlashCommandInteractionEvent) {
        wc3GameNotificationService.deleteNotification(command.channelId ?: "")
        command.replySuspended("Notification stopped for channel **${command.channel.name}**")
    }
}