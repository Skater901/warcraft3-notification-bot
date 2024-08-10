package au.com.skater901.w3cconnect

import au.com.skater901.w3cconnect.api.commands.RegisterNotification
import au.com.skater901.w3cconnect.application.module.AppModule
import au.com.skater901.w3cconnect.application.module.ConfigModule
import com.google.inject.Guice
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

fun main() {
    val injector = Guice.createInjector(AppModule(), ConfigModule())

    val config = injector.getInstance(WC3ConnectDiscordNotificationBotConfiguration::class.java)

    JDABuilder.createLight(
        config.discordConfiguration.privateToken,
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