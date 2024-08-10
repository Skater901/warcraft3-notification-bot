package au.com.skater901.w3cconnect.api.commands

import au.com.skater901.w3cconnect.core.domain.exceptions.InvalidRegexPatternException
import au.com.skater901.w3cconnect.core.service.NotificationService
import dev.minn.jda.ktx.interactions.commands.option
import jakarta.inject.Inject
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class RegisterNotification @Inject constructor(
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
        try {
            notificationService.createNotification(command.channelIdLong, command.getOption("filter")!!.asString)
            command.replySuspended(
                "Registering a notification for channel [ ${command.channel.name} (${command.channelIdLong}) ] for regex pattern [ ${
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