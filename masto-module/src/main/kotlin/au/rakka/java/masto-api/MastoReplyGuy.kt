package au.rakka.java.mastoapi

import au.com.skater901.wc3.api.core.domain.exceptions.InvalidRegexPatternException
import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3.api.scheduled.ScheduledTask
import au.com.skater901.wc3.extras.annotation.ClientFor
import au.com.skater901.wc3.utilities.coroutines.await
import au.rakka.java.`masto-api`.MastodonModule
import au.rakka.java.`masto-api`.MastodonPost
import com.fasterxml.jackson.databind.ObjectMapper
// Used to get my config class in here
import jakarta.inject.Inject
import jakarta.ws.rs.client.Client
import jakarta.ws.rs.client.Entity.json
import jakarta.ws.rs.client.Invocation
import jakarta.ws.rs.core.GenericType
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import org.slf4j.LoggerFactory

public class MastoReplyGuy @Inject constructor(
    private val conf: MastoConfig,
    @param:MastodonModule
    private val wc3GameNotificationService: WC3GameNotificationService,
    @param:ClientFor("mastodon-reply") // TODO fix this when classgraph is fixed
    private val client: Client,
    private val mapper: ObjectMapper
) : ScheduledTask {
    private companion object {
        private val logger = LoggerFactory.getLogger(MastoReplyGuy::class.java)
    }

    private fun Invocation.Builder.authorization(): Invocation.Builder =
        header(HttpHeaders.AUTHORIZATION, "Bearer ${conf.token}")

    private val baseurl = "https://${conf.instance}/api/v1/" // Well it was half useful
    private val notifurl = "${baseurl}notifications"
    private val clearurl = "${baseurl}notifications/clear"
    private val statusurl = "${baseurl}statuses"

    private var is_pleroma: Boolean? = null

    override val schedule: Int = 30

    override suspend fun task() {
        logger.debug("Polling for masto notifs")
        var finished = false
        var max_id = ""
        while (!finished) {
            val posts = client.target(notifurl + max_id)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .authorization()
                .async()
                .get(object : GenericType<List<MastodonPost>>() {})
                .await()
            posts.forEach { process_post(it) }
            logger.debug(posts.size.toString())

            // If node size isn't 20 I don't need to update max_id at all. Mainly avoids the issue where node size is 0.
            finished = posts.size < 20
            if (!finished) {
                max_id = "?max_id=" + posts[19].id
            }
        }
        client.target(clearurl)
            .request(MediaType.APPLICATION_JSON)
            .authorization()
            .async()
            .post(null, String::class.java)
            .await()
        logger.debug("Cleared notifications (hopefully)")
    }

    private suspend fun process_post(post: MastodonPost) {
        try {
            logger.debug("Processing notif {}", post.id)
            if (post.type != "mention") {
                logger.debug("Was not a mention")
                return
            }
            if (is_pleroma == null) {
                is_pleroma = post.status.pleroma != null
            }
            val text = if (is_pleroma!!) {
                post.status.pleroma!!.content.text
            } else {
                post.status.content.replace(Regex("<.*?>"), "")
            }
            val (tag, regex) = process_post_contents(text)
            if (tag == "") {
                logger.debug("tag: {} was null", tag)
                return
            }
            if (regex == "") {
                logger.debug("Deleting {}", tag)
                wc3GameNotificationService.deleteNotification(tag) // lol hope you meant it
                client.target(statusurl)
                    .request(MediaType.APPLICATION_JSON)
                    .authorization()
                    .async()
                    .post(
                        json(
                            mapOf(
                                "status" to "Unregistered $tag. In future this will hopefully be able to tell you what it contained.",
                                "in_reply_to_id" to post.status.id
                            )
                        ),
                        String::class.java
                    )
                    .await()
            } else {
                logger.debug("Adding {} with {}", tag, regex)
                try {
                    wc3GameNotificationService.createNotification(tag, regex)
                } catch (_: InvalidRegexPatternException) {
                    logger.debug("InvalidRegexPatternException")
                    client.target(statusurl)
                        .request(MediaType.APPLICATION_JSON)
                        .authorization()
                        .async()
                        .post(
                            json(
                                mapOf(
                                    "status" to "${
                                        regex.replace(
                                            "\\",
                                            "\\\\"
                                        )
                                    } was invalid. Note: Spaces are not supported, use \\\\s instead and hope that there aren't two different maps with the same name differing only by the type of whitespace.",
                                    "in_reply_to_id" to post.status.id
                                )
                            ),
                            String::class.java
                        )
                        .await()
                    logger.debug("Reported InvalidRegexPatternException")
                    return // Don't say it succeeded
                }
                client.target(statusurl)
                    .request(MediaType.APPLICATION_JSON)
                    .authorization()
                    .async()
                    .post(
                        json(
                            mapOf(
                                "status" to "Registered $tag with pattern ${regex.replace("\\", "\\\\")}",
                                "in_reply_to_id" to post.status.id
                            )
                        ),
                        String::class.java
                    )
                    .await() // .get("id") returns a string with quotes: Deleted 400 "AotNh4gQIua2IPvPN2", so don't need to put new quotes on it.
            }
        } catch (ex: Exception) {
            logger.error("Exception while processing post: ", ex) // Something happened.
            // The purpose of this catch is to prevent the effects of any one broken notif from reaching any further; so it can continue processing the rest of the notifications and then clear them.
        }
    }

    private fun process_post_contents(contents: String): Pair<String, String> {
        logger.debug("Processing post contents")
        var tag = ""
        var regex = ""
        for (word in contents.split(' ')) {
            logger.debug("Testing word {}", word)
            if (word[0] == '@') {
                continue
            }
            if (word[0] == '#' && tag == "") {
                tag = word
                continue
            }
            if (regex == "") {
                regex = word
            }
        }
        logger.debug("Reporting {} and {}", tag, regex)
        return tag to regex
    }
}