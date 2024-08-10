package au.com.skater901.w3cconnect.application.module

import au.com.skater901.w3cconnect.WC3ConnectDiscordNotificationBotConfiguration
import au.com.skater901.w3cconnect.application.config.parseConfig
import au.com.skater901.w3cconnect.application.logging.LoggingConfiguration
import com.google.inject.AbstractModule

class ConfigModule : AbstractModule() {
    override fun configure() {
        bind(WC3ConnectDiscordNotificationBotConfiguration::class.java).toInstance(parseConfig())

        requestStaticInjection(LoggingConfiguration::class.java)
    }
}