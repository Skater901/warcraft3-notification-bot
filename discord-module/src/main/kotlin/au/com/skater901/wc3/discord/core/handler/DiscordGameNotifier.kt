package au.com.skater901.wc3.discord.core.handler

import au.com.skater901.wc3.api.core.domain.Game
import au.com.skater901.wc3.api.core.domain.GameSource
import au.com.skater901.wc3.api.core.domain.Region
import au.com.skater901.wc3.api.core.domain.exceptions.InvalidNotificationException
import au.com.skater901.wc3.api.core.service.GameNotifier
import au.com.skater901.wc3.discord.core.dao.RoleNotificationDAO
import au.com.skater901.wc3.utilities.collections.forEachAsync
import au.com.skater901.wc3.utilities.metricsWork
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.edit
import jakarta.inject.Inject
import jakarta.inject.Singleton
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Singleton
internal class DiscordGameNotifier @Inject constructor(
    private val jda: JDA,
    private val roleNotificationDAO: RoleNotificationDAO
) : GameNotifier {
    private val hostedGameMessages: ConcurrentMap<Int, MutableSet<Pair<Message, String?>>> = ConcurrentHashMap()

    private val notifyNewGameWork = metricsWork(::notifyNewGame)
    override suspend fun notifyNewGame(notificationId: String, game: Game) {
        notifyNewGameWork {
            val channel = try {
                jda.getTextChannelById(notificationId)
                    ?: throw InvalidNotificationException()
            } catch (_: NumberFormatException) {
                throw InvalidNotificationException()
            }

            val roleToNotify = roleNotificationDAO.find(notificationId)

            channel.sendMessage(createGameMessage(game, false, roleToNotify))
                .await()
                .let {
                    hostedGameMessages.computeIfAbsent(game.id) { mutableSetOf() }
                        .add(it to roleToNotify)
                }
        }
    }

    private val updateExistingGameWork = metricsWork(::updateExistingGame)
    override suspend fun updateExistingGame(game: Game) {
        updateExistingGameWork {
            val invalidMessages = mutableSetOf<Pair<Int, String>>()
            hostedGameMessages[game.id]?.forEachAsync { (message, roleToNotify) ->
                try {
                    message.edit(
                        content = "<@&$roleToNotify>",
                        embeds = createGameMessage(game, false, null).embeds
                    )
                        .await()
                } catch (e: ErrorResponseException) {
                    if (e.errorResponse == ErrorResponse.UNKNOWN_MESSAGE) {
                        invalidMessages.add(game.id to message.id)
                    } else {
                        throw e
                    }
                }
            }
            invalidMessages.forEach { (gameId, messageId) ->
                hostedGameMessages[gameId]!!.removeIf { (message, _) -> message.id == messageId }
            }
        }
    }

    private val closeExpiredGameWork = metricsWork(::closeExpiredGame)
    override suspend fun closeExpiredGame(game: Game) {
        closeExpiredGameWork {
            hostedGameMessages[game.id]?.forEachAsync { (message, roleToNotify) ->
                try {
                    message.edit(
                        content = "<@&$roleToNotify>",
                        embeds = createGameMessage(game, true, null).embeds
                    )
                        .await()
                } catch (e: ErrorResponseException) {
                    if (e.errorResponse == ErrorResponse.UNKNOWN_MESSAGE) {
                        // do nothing, we're done with this message and we just want to remove it from the list of messages
                    } else {
                        throw e
                    }
                }
            }

            hostedGameMessages.remove(game.id)
        }
    }

    private fun createGameMessage(game: Game, gameRemoved: Boolean, roleToNotify: String?): MessageCreateData =
        MessageCreate {
            roleToNotify?.also { content = "<@&$it>" }
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
                    GameSource.BattleNet -> "https://raw.githubusercontent.com/Skater901/warcraft3-notification-bot/main/assets/wc3stats_favicon.png" to "https://wc3stats.com/"
                    GameSource.WC3Connect -> "https://raw.githubusercontent.com/Skater901/warcraft3-notification-bot/main/assets/wc3connect_favicon.png" to "https://entgaming.net/"
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