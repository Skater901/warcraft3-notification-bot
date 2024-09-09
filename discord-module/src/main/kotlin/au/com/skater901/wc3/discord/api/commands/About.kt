package au.com.skater901.wc3.discord.api.commands

import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

internal class About : Command {
    override val name: String = "about"
    override val description: String = "Display information about this bot."

    override suspend fun handleCommand(command: SlashCommandInteractionEvent) {
        command.replySuspended(
            MessageCreate {
                embed {
                    color = 0x00FFFF

                    title = "About"

                    field {
                        name = "About This Bot"
                        value =
                            "This bot will send messages to a channel when a Warcraft 3 game is hosted. It will edit the messages to update the game status, and indicate when the game has started or been unhosted."
                        inline = false
                    }

                    field {
                        name = "Who Created This Bot?"
                        value =
                            "This bot was created by [Skater901](https://discord.com/users/20170773821495705). For help with the bot, please contact him. The source code for the bot can be found [here](https://github.com/Skater901/warcraft3-notification-bot). Contributions are always welcome!"
                        inline = false
                    }

                    footer {
                        name = "Version ${System.getProperty("appVersion")}"
                    }
                }
            }
        )
    }
}