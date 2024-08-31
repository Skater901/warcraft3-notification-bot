package au.com.skater901.wc3connect.application.config

import java.net.URI

internal class GamesConfiguration(
    val gamesURL: URI,
    val refreshInterval: Long = 30_000
)