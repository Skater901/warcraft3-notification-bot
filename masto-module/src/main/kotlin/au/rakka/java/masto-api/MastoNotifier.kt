package au.rakka.java.mastoapi

import au.com.skater901.wc3.api.core.domain.Game
import au.com.skater901.wc3.api.core.service.GameNotifier
import au.com.skater901.wc3.extras.annotation.ClientFor
import au.com.skater901.wc3.utilities.coroutines.await
import au.rakka.java.`masto-api`.PostResponse
import com.fasterxml.jackson.databind.ObjectMapper
// Used to get my config class in here
import jakarta.inject.Inject
import jakarta.ws.rs.client.Client
import jakarta.ws.rs.client.Entity.json
import jakarta.ws.rs.client.Invocation
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

public class MastoNotifier @Inject constructor(
    private val conf: MastoConfig,
    @param:ClientFor("mastodon-notifier") // TODO fix this when classgraph is fixed
    private val client: Client,
    private val mapper: ObjectMapper
) : GameNotifier {
    private val hostedGames: ConcurrentMap<Int, GameMessage> = ConcurrentHashMap()

    private companion object {
        private val logger = LoggerFactory.getLogger(MastoNotifier::class.java)
    }

    private fun Invocation.Builder.authorization(): Invocation.Builder =
        header(HttpHeaders.AUTHORIZATION, "Bearer ${conf.token}")

    private val baseurl = "https://${conf.instance}/api/v1/" // Might want this for other endpoints later maybe
    private val statusurl = "${baseurl}statuses"

    override suspend fun notifyNewGame(notificationId: String, game: Game) {
        logger.debug("Notifying new game")
        client.target(statusurl)
            .request(MediaType.APPLICATION_JSON)
            .authorization()
            .async()
            .post(json(game_to_string(game, notificationId)), PostResponse::class.java)
            .await()
            .let { response ->
                logger.debug(response.toString())
                hostedGames[game.id] = GameMessage(
                    response.id,
                    notificationId,
                    game.name,
                    game.currentPlayers
                )
                logger.debug("Saved ID {}: {}", game.id, hostedGames[game.id]!!.message_id)
            }
    }

    override suspend fun updateExistingGame(game: Game) {
        logger.debug("Update function called")
        val gm = hostedGames[game.id]!!
        logger.debug("Might update {}", gm.message_id)
        if (!gm.update(game)) {
            return
        }
        logger.debug("Updating {}", gm.message_id)
        client.target("$statusurl/${gm.message_id}")
            .request(MediaType.APPLICATION_JSON)
            .authorization()
            .async()
            .put(json(game_to_string(game, gm.game_tag)), String::class.java)
            .await()
    }

    override suspend fun closeExpiredGame(game: Game) {
        val gm = hostedGames[game.id]!!
        client.target("$statusurl/${gm.message_id}")
            .request(MediaType.APPLICATION_JSON)
            .authorization()
            .async()
            .put(json(game_to_string(game, gm.game_tag, true)), String::class.java)
            .await()
            .let { response ->
                logger.debug(response)
            }
        hostedGames.remove(game.id)
    }

    // Copied from discord module. It makes a lot of sense.
    private fun game_to_string(game: Game, tag: String, gameRemoved: Boolean = false): Map<String, String> {
        val body = "$tag lobby's " + (if (!gameRemoved) "up" else "down") +
                "\nName: " + game.name +
                "\nMap: " + game.map +
                "\nHosted by: " + game.host +
                "\nOn: " + game.gameSource +
                "\nPlayers: " + game.currentPlayers + "/" + game.maxPlayers +
                "\nCreated at: " + game.created
        logger.info(body)
        return mapOf("status" to body)
    }
}

private class GameMessage(val message_id: String, val game_tag: String, var game_name: String, var player_count: Int) {
    fun has_changed(game: Game): Boolean {
        return !(game_name == game.name && player_count == game.currentPlayers)
    }

    fun update(game: Game): Boolean {
        val changed = has_changed(game)
        game_name = game.name
        player_count = game.currentPlayers
        return changed
    }
}