package au.com.skater901.wc3connect.discord.api.commands

import au.com.skater901.wc3connect.api.core.domain.exceptions.InvalidRegexPatternException
import au.com.skater901.wc3connect.api.core.service.NotificationService
import dev.minn.jda.ktx.interactions.commands.option
import jakarta.inject.Inject
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

internal class RegisterNotification @Inject constructor(
    private val notificationService: NotificationService
) : Command {
    override val name: String = "notify"
    override val description: String =
        "Set the Warcraft III maps that you want to be announced to this channel when hosted on W3CConnect"

    override val options: SlashCommandData.() -> Unit = {
        option<String>(
            "filter",
            "The regex pattern used to filter the maps that will be announced in this channel.",
            required = true
        )
    }

    override suspend fun handleCommand(command: SlashCommandInteractionEvent) {
        // TODO permissions - who can use this command?
        if (command.channelId == null) {
            // Can this even happen? idk but just in case
            command.replySuspended("Notification requested from outside a channel. Please use a channel.")
            return
        }

        try {
            notificationService.createNotification(command.channelId!!, command.getOption("filter")!!.asString)
            command.replySuspended(
                "Registering a notification for channel [ ${command.channel.name} (${command.channelId}) ] for regex pattern [ ${
                    command.getOption(
                        "filter"
                    )?.asString
                } ]"
            )
        } catch (e: InvalidRegexPatternException) {
            command.replySuspended("Invalid regex pattern. ${e.message}")
        }
    }
}