package au.com.skater901.wc3connect.application.module

import au.com.skater901.wc3connect.application.config.GamesSourceConfiguration
import com.google.inject.AbstractModule
import com.google.inject.Provides
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.net.http.HttpClient

internal class ClientModule : AbstractModule() {
    @Provides
    @Singleton
    fun getClient(): HttpClient = HttpClient.newHttpClient()

    @Provides
    @Named("refreshInterval")
    @Inject
    fun getRefreshInterval(config: GamesSourceConfiguration): Long = config.refreshInterval
}