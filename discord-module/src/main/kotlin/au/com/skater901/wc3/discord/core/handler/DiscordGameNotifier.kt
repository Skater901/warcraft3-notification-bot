package au.com.skater901.wc3.discord.core.handler

import au.com.skater901.wc3.api.core.domain.Game
import au.com.skater901.wc3.api.core.domain.GameSource
import au.com.skater901.wc3.api.core.domain.Region
import au.com.skater901.wc3.api.core.domain.exceptions.InvalidNotificationException
import au.com.skater901.wc3.api.core.service.GameNotifier
import au.com.skater901.wc3.utilities.collections.forEachAsync
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.edit
import jakarta.inject.Inject
import jakarta.inject.Singleton
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Singleton
public class DiscordGameNotifier @Inject internal constructor(
    private val jda: JDA
) : GameNotifier {
    private val hostedGameMessages: ConcurrentMap<Int, MutableSet<Message>> = ConcurrentHashMap()
    override suspend fun notifyNewGame(notificationId: String, game: Game) {
        val channel = try {
            jda.getTextChannelById(notificationId)
                ?: throw InvalidNotificationException()
        } catch (e: NumberFormatException) {
            throw InvalidNotificationException()
        }

        channel.sendMessage(createGameMessage(game, false))
            .await()
            .let {
                hostedGameMessages.computeIfAbsent(game.id) { mutableSetOf() }
                    .add(it)
            }
    }

    override suspend fun updateExistingGame(game: Game) {
        hostedGameMessages[game.id]?.forEachAsync {
            it.edit(embeds = createGameMessage(game, false).embeds)
                .await()
        }
    }

    override suspend fun closeExpiredGame(game: Game) {
        hostedGameMessages[game.id]?.forEachAsync { message ->
            message.edit(embeds = createGameMessage(game, true).embeds)
                .await()
        }

        hostedGameMessages.remove(game.id)
    }

    private fun createGameMessage(game: Game, gameRemoved: Boolean): MessageCreateData = MessageCreate {
        embed {
            color = if (gameRemoved) 0x1e1f22 else 0x22FF00
            author(iconUrl = "https://wow.zamimg.com/uploads/screenshots/normal/875650.jpg") {
                name = game.host
            }
            title = when (game.gameSource) {
                GameSource.BattleNet -> game.map.dropLast(4) // BattleNet games have .w3x at the end of the map name.
                GameSource.WC3Connect -> game.map
            }
            url = when (game.gameSource) {
                GameSource.BattleNet -> battleNetMap(game.map)
                GameSource.WC3Connect -> wc3ConnectMap(game.map)
            }
            field {
                name = "Hosted On"
                value = when (game.gameSource) {
                    GameSource.BattleNet -> "Battle.Net"
                    GameSource.WC3Connect -> game.gameSource.name
                }
                inline = false
            }
            field {
                name = "Game Name"
                value = "${game.region.flag()} ${game.name} (${game.currentPlayers}/${game.maxPlayers})"
                inline = false
            }
            field {
                name = if (gameRemoved) "Started" else "Created"
                val timeSinceGameStarted = Duration.between(game.created, Instant.now())
                val minutesSinceGameStarted = timeSinceGameStarted.toMinutes()
                val timeString = when {
                    minutesSinceGameStarted < 1 -> "${timeSinceGameStarted.seconds} seconds"
                    minutesSinceGameStarted < 2 -> "$minutesSinceGameStarted minute"
                    else -> "$minutesSinceGameStarted minutes"
                }
                value = if (gameRemoved) "After $timeString" else "$timeString ago"
                inline = false
            }
            val (icon, url) = when (game.gameSource) {
                // TODO why don't these icon links work??
                GameSource.BattleNet -> "https://wc3stats.com/assets/favicon.ico" to "https://wc3stats.com/"
                GameSource.WC3Connect -> "https://entgaming.net/favicon.ico" to "https://entgaming.net/"
            }
            footer("Powered by $url", icon)
        }
    }

    private fun Region.flag(): String = when (this) {
        Region.EU -> ":flag_eu:"
        Region.US -> ":flag_us:"
        Region.Asia -> ":flag_kr:"
        Region.Unknown -> ":earth_americas:"
    }

    private fun battleNetMap(mapName: String): String = URI(
        "https",
        "wc3maps.com",
        "/maps",
        "query=$mapName",
        null
    )
        .toString()

    private fun wc3ConnectMap(mapName: String): String = URI(
        "https",
        "entgaming.net",
        "/link/host_add.php",
        "filter=$mapName",
        null
    )
        .toString()
}