package au.com.skater901.w3cconnect.api.commands

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback

suspend fun IReplyCallback.replySuspended(content: String) {
    reply(content).await()
}