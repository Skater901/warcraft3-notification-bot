package au.com.skater901.wc3.discord.core.notifier

import au.com.skater901.wc3.api.core.service.AdminMessageNotifier
import au.com.skater901.wc3.utilities.collections.forEachAsync
import au.com.skater901.wc3.utilities.metricsWork
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.MessageCreate
import jakarta.inject.Inject
import net.dv8tion.jda.api.JDA
import org.slf4j.LoggerFactory

internal class AdminNotifier @Inject constructor(private val jda: JDA) : AdminMessageNotifier {
    companion object {
        private val logger = LoggerFactory.getLogger(AdminNotifier::class.java)
    }

    private val sendAdminMessageWork = metricsWork(::sendAdminMessage)
    override suspend fun sendAdminMessage(message: String, notifications: List<String>) {
        sendAdminMessageWork {
            notifications.forEachAsync {
                try {
                    jda.getTextChannelById(it)
                        ?.sendMessage(
                            MessageCreate {
                                embed {
                                    title = "Admin Message"
                                    field {
                                        value = message
                                    }
                                }
                            }
                        )
                        ?.await()
                } catch (e: Exception) {
                    logger.error("Exception when sending admin message to channel [ {} ]:", it, e)
                }
            }
        }
    }
}