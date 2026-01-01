package au.com.skater901.wc3.discord.core.notifier

import au.com.skater901.wc3.api.core.service.AdminMessageNotifier
import au.com.skater901.wc3.utilities.collections.forEachAsync
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.MessageCreate
import jakarta.inject.Inject
import net.dv8tion.jda.api.JDA

internal class AdminNotifier @Inject constructor(private val jda: JDA) : AdminMessageNotifier {
    override suspend fun sendAdminMessage(message: String, notifications: List<String>) {
        notifications.forEachAsync {
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
        }
    }
}