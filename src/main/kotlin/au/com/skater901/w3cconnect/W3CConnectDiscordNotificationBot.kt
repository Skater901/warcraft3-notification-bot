package au.com.skater901.w3cconnect

import au.com.skater901.w3cconnect.api.commands.RegisterNotification
import au.com.skater901.w3cconnect.application.config.parseConfig
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

fun main() {
    // parse config
    val config = parseConfig("")

    JDABuilder.createLight(
        config.discordConfiguration!!.privateToken,
        GatewayIntent.GUILD_MESSAGES
    )
        .build()
        .updateCommands()
        .apply {
            // register commands
            addCommands(
                RegisterNotification()
            )

            queue()

            // register event handlers
        }
}