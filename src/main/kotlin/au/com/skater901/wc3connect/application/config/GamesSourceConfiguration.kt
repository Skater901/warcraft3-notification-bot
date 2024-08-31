package au.com.skater901.wc3connect.application.config

import java.net.URI

internal class GamesSourceConfiguration(
    val url: URI = URI("https://host.entgaming.net/allgames"),
    val refreshInterval: Long = 30_000
)