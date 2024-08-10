package au.com.skater901.w3cconnect.api.commands

import dev.minn.jda.ktx.interactions.commands.option
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class RegisterNotification : Command {
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
        command.replySuspended(
            "Registering a notification for channel [ ${command.channelId} ] for regex pattern [ ${
                command.getOption(
                    "filter"
                )?.asString
            } ]"
        )
    }
}