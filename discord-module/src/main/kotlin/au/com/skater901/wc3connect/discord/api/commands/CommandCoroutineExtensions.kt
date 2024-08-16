package au.com.skater901.wc3connect.discord.api.commands

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback

internal suspend fun IReplyCallback.replySuspended(content: String) {
    reply(content).await()
}