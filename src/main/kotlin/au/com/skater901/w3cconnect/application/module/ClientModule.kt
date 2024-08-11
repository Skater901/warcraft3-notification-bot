package au.com.skater901.w3cconnect.application.module

import au.com.skater901.w3cconnect.WC3ConnectDiscordNotificationBotConfiguration
import com.google.inject.AbstractModule
import com.google.inject.Provides
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.net.URI
import java.net.http.HttpClient

class ClientModule : AbstractModule() {
    @Provides
    @Singleton
    fun getClient(): HttpClient = HttpClient.newHttpClient()

    @Provides
    @Named("gamesURL")
    @Inject
    fun getGamesURL(config: WC3ConnectDiscordNotificationBotConfiguration): URI = config.gamesConfiguration.gamesURL

    @Provides
    @Named("refreshInterval")
    @Inject
    fun getRefreshInterval(config: WC3ConnectDiscordNotificationBotConfiguration): Long = config.gamesConfiguration
        .refreshInterval
}