package au.com.skater901.wc3.discord.api.commands

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.utils.messages.MessageCreateData

internal suspend fun IReplyCallback.replySuspended(content: String) {
    reply(content).await()
}

internal suspend fun IReplyCallback.replySuspended(message: MessageCreateData) {
    reply(message).await()
}