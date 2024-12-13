package au.rakka.java.mastoapi

import au.com.skater901.wc3.api.scheduled.ScheduledTask
import au.com.skater901.wc3.api.core.service.WC3GameNotificationService

import java.net.http.HttpClient // This should be significantly less hassle.
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI

// Used to get my config class in here
import jakarta.inject.Inject

import kotlinx.coroutines.future.await
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode

import org.slf4j.LoggerFactory

public class MastoReplyGuy @Inject constructor(private val conf:MastoConfig, private val wc3GameNotificationService: WC3GameNotificationService) : ScheduledTask
{
	private val logger = LoggerFactory.getLogger(MastoReplyGuy::class.java)
	private val mapper = ObjectMapper()
	
	private val userAgent = "WC3 Notification Bot ${System.getProperty("appVersion")} - Java-http-client/${System.getProperty("java.version")}"
	private val client: HttpClient = HttpClient.newHttpClient()
	private val builder: HttpRequest.Builder = HttpRequest.newBuilder().setHeader("Authorization","Bearer ${conf.token}").setHeader("Content-Type","application/json").setHeader("User-Agent",userAgent)
	
	private val baseurl = "https://${conf.instance}/api/v1/" // Well it was half useful
	private val notifurl = "${baseurl}notifications"
	private val clearurl = "${baseurl}notifications/clear"
	private val statusurl = "${baseurl}statuses"
	
	private var is_pleroma:Boolean? = null
	
	override val schedule: Int = 30
	override suspend fun task()
	{
		logger.debug("Polling for masto notifs")
		var finished=false
		var max_id=""
		while (!finished)
		{
			val response = client.sendAsync(builder.uri(URI.create(notifurl+max_id)).GET().build(),HttpResponse.BodyHandlers.ofInputStream()).await()
			logger.debug(response.statusCode().toString())
			val node=mapper.readTree(response.body())
			node.forEach{ process_post(it) }
			logger.debug(node.size().toString())
			finished=node.size()<20 // If node size isn't 20 I don't need to update max_id at all. Mainly avoids the issue where node size is 0.
			if (!finished) {max_id=node.get(19).get("id").asText()}
		}
		val response = client.sendAsync(builder.uri(URI.create(clearurl)).POST(HttpRequest.BodyPublishers.ofString("")).build(),HttpResponse.BodyHandlers.ofInputStream()).await()
		logger.debug("Cleared notifications (hopefully) {}",response.statusCode().toString())
	}
	private suspend fun process_post(post:JsonNode)
	{
		logger.debug("Processing notif {}",post.get("id"))
		if (post.get("type").asText()!="mention") {logger.debug("Was not a mention");return}
		if (is_pleroma==null)
			{is_pleroma=post.get("status").get("pleroma")!=null}
		val text=if (is_pleroma!!)
		{
			post.get("status").get("pleroma").get("content").get("text/plain").asText()
		}
		else
		{
			post.get("status").get("content").asText().replace(Regex("<.*?>"),"")
		}
		val (tag,regex)=process_post_contents(text)
		if (tag=="") {logger.debug("tag: {} was null",tag);return}
		if (regex=="")
		{
			logger.debug("Deleting {}",tag)
			wc3GameNotificationService.deleteNotification(tag) // lol hope you meant it
			val response=client.sendAsync(builder.uri(URI.create(statusurl)).POST(HttpRequest.BodyPublishers.ofString("""{"status":"Unregistered ${tag}. In future this will hopefully be able to tell you what it contained.","in_reply_to_id":${post.get("status").get("id")}}""")).build(),HttpResponse.BodyHandlers.ofInputStream()).await()
			logger.debug("Deleted {} {}",response.statusCode().toString(),post.get("status").get("id"))
		}
		else
		{
			logger.debug("Adding {} with {}",tag,regex)
			wc3GameNotificationService.createNotification(tag,regex)
			val response=client.sendAsync(builder.uri(URI.create(statusurl)).POST(HttpRequest.BodyPublishers.ofString("""{"status":"Registered ${tag} with pattern ${regex.replace("\\","\\\\")}.","in_reply_to_id":${post.get("status").get("id")}}""")).build(),HttpResponse.BodyHandlers.ofInputStream()).await() // .get("id") returns a string with quotes: Deleted 400 "AotNh4gQIua2IPvPN2", so don't need to put new quotes on it.
			// Why do I have to replace \ with \\? I have no idea. Pleromer gets mad about a lone backslash in post contents apparently. Does mastodon? Dunno.
			logger.debug("Created {} {}",response.statusCode().toString(),post.get("status").get("id"))
		}
	}
	private fun process_post_contents(contents:String) : Pair<String,String>
	{
		logger.debug("Processing post contents")
		var tag=""
		var regex=""
		val words=contents.split(' ')
		for (word in words)
		{
			logger.debug("Testing word {}",word)
			if (word[0]=='@') {continue}
			if (word[0]=='#' && tag=="") {tag=word; continue}
			if (regex=="") {regex=word}
		}
		logger.debug("Reporting {} and {}",tag,regex)
		return Pair(tag,regex)
	}
}