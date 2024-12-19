package au.rakka.java.mastoapi

import au.com.skater901.wc3.api.core.service.GameNotifier

import java.net.http.HttpClient // This should be significantly less hassle.
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import au.com.skater901.wc3.api.core.domain.Game
import au.com.skater901.wc3.api.core.domain.GameSource
import au.com.skater901.wc3.api.core.domain.Region

// Used to get my config class in here
import jakarta.inject.Inject

import kotlinx.coroutines.future.await
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper

import org.slf4j.LoggerFactory

public class MastoNotifier @Inject constructor(private val conf:MastoConfig) : GameNotifier
{
	private val hostedGames: ConcurrentMap<Int,GameMessage> = ConcurrentHashMap()
	
	private val logger = LoggerFactory.getLogger(MastoNotifier::class.java)
	private val mapper = ObjectMapper()
	
	private val userAgent = "WC3 Notification Bot ${System.getProperty("appVersion")} - Java-http-client/${System.getProperty("java.version")}"
	private val client: HttpClient = HttpClient.newHttpClient()
	private val builder: HttpRequest.Builder = HttpRequest.newBuilder().setHeader("Authorization","Bearer ${conf.token}").setHeader("Content-Type","application/json").setHeader("User-Agent",userAgent)
	
	private val baseurl = "https://${conf.instance}/api/v1/" // Might want this for other endpoints later maybe
	private val statusurl = "${baseurl}statuses"
	
	override suspend fun notifyNewGame(notificationId: String, game: Game)
	{
		logger.debug("Notifying new game")
		val response = client.sendAsync(builder.uri(URI.create(statusurl)).POST(game_to_string(game,notificationId)).build(),HttpResponse.BodyHandlers.ofString()).await()
		logger.debug(response.statusCode().toString())
		val node=mapper.readTree(response.body())
		logger.debug(response.body())
		hostedGames[game.id]=GameMessage(node.get("id").asText(),notificationId,game.name,game.currentPlayers)
		logger.debug("Saved ID {}: {}",game.id,hostedGames[game.id]!!.message_id)
	}
	
	override suspend fun updateExistingGame(game: Game)
	{
		logger.debug("Update function called")
		val gm=hostedGames[game.id]!!
		logger.debug("Might update {}",gm.message_id)
		if (!gm.update(game)) { return }
		logger.debug("Updating {}",gm.message_id)
		val response=client.sendAsync(builder.uri(URI.create(statusurl+"/"+gm.message_id)).PUT(game_to_string(game,gm.game_tag)).build(),HttpResponse.BodyHandlers.ofString()).await()
		logger.debug(response.statusCode().toString())
		logger.debug(response.body())
	}
	
	override suspend fun closeExpiredGame(game: Game)
	{
		val gm=hostedGames[game.id]!!
		val response=client.sendAsync(builder.uri(URI.create(statusurl+"/"+gm.message_id)).PUT(game_to_string(game,gm.game_tag,true)).build(),HttpResponse.BodyHandlers.ofString()).await()
		logger.debug("{} {}",response.statusCode(),response.body())
		hostedGames.remove(game.id)
	}
	
	// Copied from discord module. It makes a lot of sense.
	private fun game_to_string(game:Game, tag:String?, gameRemoved:Boolean=false): HttpRequest.BodyPublisher
	{
		val body="{\"status\":\"" +
		"${tag} lobby's " + if (!gameRemoved) {"up"} else {"down"} +
		"\\nName: "+game.name +
		"\\nMap: "+game.map +
		"\\nHosted by: "+game.host +
		"\\nOn: "+game.gameSource +
		"\\nPlayers: "+game.currentPlayers+"/"+game.maxPlayers +
		"\\nCreated at: "+game.created +
		"\"}"
		logger.info(body)
		return HttpRequest.BodyPublishers.ofString(body)
	}
}

private class GameMessage(val message_id:String, val game_tag:String, var game_name:String, var player_count:Int)
{
	public fun has_changed(game:Game):Boolean
	{
		return !(game_name==game.name && player_count==game.currentPlayers)
	}
	
	public fun update(game:Game):Boolean
	{
		val changed=has_changed(game)
		game_name=game.name
		player_count=game.currentPlayers
		return changed
	}
}