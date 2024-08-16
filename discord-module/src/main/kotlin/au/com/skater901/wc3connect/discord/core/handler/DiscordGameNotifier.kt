package au.com.skater901.wc3connect.discord.core.handler

import au.com.skater901.wc3connect.core.domain.Game
import au.com.skater901.wc3connect.core.domain.exceptions.InvalidNotificationException
import au.com.skater901.wc3connect.core.service.GameNotifier
import au.com.skater901.wc3connect.utils.forEachAsync
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.edit
import jakarta.inject.Inject
import jakarta.inject.Singleton
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Singleton
public class DiscordGameNotifier @Inject constructor(
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
            author {
                name = game.host
            }
            title = game.map
            field {
                name = "Game Name"
                // TODO country flag?
                value = "${game.name} (${game.currentPlayers}/${game.maxPlayers})"
                inline = false
            }
            field {
                name = if (gameRemoved) "Started" else "Created"
                value = if (gameRemoved) "After ${game.uptime} minutes" else "${game.uptime} minutes ago"
                inline = false
            }
        }
    }
}