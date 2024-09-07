package au.com.skater901.wc3.discord.api.commands

import au.com.skater901.wc3.api.core.domain.exceptions.InvalidRegexPatternException
import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import dev.minn.jda.ktx.interactions.commands.option
import jakarta.inject.Inject
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

internal class RegisterNotification @Inject constructor(
    private val wc3GameNotificationService: WC3GameNotificationService
) : Command {
    override val name: String = "notify"
    override val description: String =
        "Set the Warcraft III maps that you want to be announced to this channel when hosted"

    override val options: SlashCommandData.() -> Unit = {
        option<String>(
            "filter",
            "The regex pattern used to filter the maps that will be announced in this channel.",
            required = true
        )
    }

    override val defaultPermissions: DefaultMemberPermissions = DefaultMemberPermissions.DISABLED

    override suspend fun handleCommand(command: SlashCommandInteractionEvent) {
        if (command.channelId == null) {
            // Can this even happen? idk but just in case
            command.replySuspended("Notification requested from outside a channel. Please use a channel.")
            return
        }

        try {
            wc3GameNotificationService.createNotification(command.channelId!!, command.getOption("filter")!!.asString)
            command.replySuspended(
                "Registering a notification for channel **${command.channel.name}** for regex pattern **${
                    command.getOption("filter")?.asString
                }**"
            )
        } catch (e: InvalidRegexPatternException) {
            command.replySuspended("Invalid regex pattern. ${e.message}")
        }
    }
}