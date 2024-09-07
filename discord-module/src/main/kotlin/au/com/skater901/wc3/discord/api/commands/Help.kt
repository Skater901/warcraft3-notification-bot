package au.com.skater901.wc3.discord.api.commands

import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

internal class Help : Command {
    override val name: String = "help"
    override val description: String = "Get help on how to use this bot."

    override suspend fun handleCommand(command: SlashCommandInteractionEvent) {
        command.replySuspended(
            MessageCreate {
                embed {
                    color = 0x00FFFF

                    title = "Help"

                    field {
                        name = "Basic Usage"
                        value =
                            "Use /notify to set up a notification in the current channel. Use /stopnotify to cancel notifications for the current channel. Please note, these commands are only available to server admins."
                    }

                    field {
                        name = "What Is Regex?"
                        value =
                            "Regex stands for Regular Expression, and it's a special language for matching words or phrases. For a basic pattern to match the start of a map name, use `^map_name`. For example: `^DotA`. For more complicated expressions, use https://regex101.com/."
                    }

                    field {
                        name = "It's not working!"
                        value =
                            "If the bot stops working, first check if it's online. It should be online, or you won't be able to see this message. Second, if Battle.Net games are not showing up, check [WC3Stats' Live  Game List](https://wc3stats.com/gamelist). This bot uses that list as the source of data for what games are currently hosted, so if it stops updating, this bot stops working for Battle.Net games. For any other problem, please either contact @Skater901 on Discord, or raise an issue [here](https://github.com/Skater901/warcraft3-notification-bot/issues)."
                    }
                }
            }
        )
    }
}