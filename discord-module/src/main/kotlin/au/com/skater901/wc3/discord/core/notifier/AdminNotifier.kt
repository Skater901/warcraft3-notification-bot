package au.com.skater901.wc3.discord.core.notifier

import au.com.skater901.wc3.api.core.service.AdminMessageNotifier
import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3.discord.annotations.DiscordModule
import au.com.skater901.wc3.utilities.collections.forEachAsync
import au.com.skater901.wc3.utilities.metricsWork
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.MessageCreate
import jakarta.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import org.slf4j.LoggerFactory

internal class AdminNotifier @Inject constructor(
    private val jda: JDA,
    @DiscordModule
    private val wC3GameNotificationService: WC3GameNotificationService
) : AdminMessageNotifier {
    companion object {
        private val logger = LoggerFactory.getLogger(AdminNotifier::class.java)
    }

    private val sendAdminMessageWork = metricsWork(::sendAdminMessage)
    override suspend fun sendAdminMessage(message: String, notifications: List<String>) {
        sendAdminMessageWork {
            notifications.forEachAsync {
                try {
                    val channel = jda.getTextChannelById(it)

                    if (channel == null) {
                        logger.warn("Channel [ {} ] does not exist, removing notification.", it)
                        wC3GameNotificationService.deleteNotification(it)
                    } else {
                        channel.sendMessage(
                            MessageCreate {
                                embed {
                                    title = "Admin Message"
                                    field {
                                        value = message
                                    }
                                }
                            }
                        )
                            .await()
                    }
                } catch (_: InsufficientPermissionException) {
                    logger.warn("Removing channel [ {} ] as the bot does not have permission to post there.", it)
                    wC3GameNotificationService.deleteNotification(it)
                } catch (e: Exception) {
                    if (e is CancellationException) {
                        currentCoroutineContext().ensureActive()
                    }
                    logger.error("Exception when sending admin message to channel [ {} ]:", it, e)
                }
            }
        }
    }
}