package au.com.skater901.wc3.discord.api.commands

import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3.discord.annotations.DiscordModule
import au.com.skater901.wc3.discord.core.dao.jdbi.JdbiRoleNotificationDAO
import jakarta.inject.Inject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions

internal class StopNotification @Inject constructor(
    @param:DiscordModule
    private val wc3GameNotificationService: WC3GameNotificationService,
    private val roleNotificationDAO: JdbiRoleNotificationDAO
) : Command {
    override val name: String = "stopnotify"
    override val description: String = "Stop all game hosting notifications to this channel."

    override val defaultPermissions: DefaultMemberPermissions = DefaultMemberPermissions.DISABLED

    override suspend fun handleCommand(command: SlashCommandInteractionEvent) {
        coroutineScope {
            command.channelId
                ?.let {
                    launch { wc3GameNotificationService.deleteNotification(it) }
                    launch { roleNotificationDAO.delete(it) }
                }
        }
        command.replySuspended("Notification stopped for channel **${command.channel.name}**")
    }
}