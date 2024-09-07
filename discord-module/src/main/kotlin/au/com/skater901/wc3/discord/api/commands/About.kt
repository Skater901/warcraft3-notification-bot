package au.com.skater901.wc3.discord.api.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

internal class About : Command {
    override val name: String = "about"
    override val description: String = "Display information about this bot."

    override suspend fun handleCommand(command: SlashCommandInteractionEvent) {
        // TODO how the HECK do you link to a profile on Discord????????????
        command.replySuspended("This bot was created by @Skater901. For help with the bot, please contact him. The source code for the bot can be found [here](https://github.com/Skater901/warcraft3-notification-bot), if you are curious.")
    }
}