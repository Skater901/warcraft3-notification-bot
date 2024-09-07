package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.application.config.ApplicationConfiguration
import com.google.inject.AbstractModule
import com.google.inject.Provides
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.net.http.HttpClient
import java.time.Duration

internal class ClientModule : AbstractModule() {
    @Provides
    @Singleton
    fun getClient(): HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    @Provides
    @Named("refreshInterval")
    @Inject
    fun getRefreshInterval(config: ApplicationConfiguration): Long = config.refreshInterval
}