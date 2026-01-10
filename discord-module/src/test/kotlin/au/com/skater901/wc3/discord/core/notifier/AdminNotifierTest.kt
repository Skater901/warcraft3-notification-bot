package au.com.skater901.wc3.discord.core.notifier

import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.concurrent.CompletableFuture

class AdminNotifierTest {
    @Test
    fun `should send admin messages to valid channels and ignore invalid channels`() {
        val notifications = listOf("channel1", "channel2")
        val adminMessage = "test"

        val message = mock<Message>()
        val messageCreateAction = mock<MessageCreateAction> {
            on { submit() } doReturn CompletableFuture.supplyAsync {
                Thread.sleep(100)
                message
            }
        }
        val channel1 = mock<TextChannel> {
            on { sendMessage(any<MessageCreateData>()) } doReturn messageCreateAction
        }
        val jda = mock<JDA> {
            on { getTextChannelById("channel1") } doReturn channel1
            on { getTextChannelById("channel2") } doReturn null
        }

        runBlocking { AdminNotifier(jda).sendAdminMessage(adminMessage, notifications) }

        verify(channel1) {
            1 * { sendMessage(any<MessageCreateData>()) }
        }
        verify(messageCreateAction) {
            1 * { submit() }
        }
    }

    @Test
    fun `should catch exception from sending admin message to one channel`() {
        val goodChannelId = "good channel"
        val badChannelId = "bad channel"

        var goodChannelSucceeded = false

        val goodChannelResult = mock<MessageCreateAction> {
            on { submit() } doReturn CompletableFuture.supplyAsync {
                Thread.sleep(100)
                goodChannelSucceeded = true
                null
            }
        }
        val goodChannel = mock<TextChannel> {
            on { sendMessage(any<MessageCreateData>()) } doReturn goodChannelResult
        }

        val badChannelResult = mock<MessageCreateAction> {
            on { submit() } doReturn CompletableFuture<Message>().apply { completeExceptionally(RuntimeException("Kaboom!")) }
        }
        val badChannel = mock<TextChannel> {
            on { sendMessage(any<MessageCreateData>()) } doReturn badChannelResult
        }

        val jda = mock<JDA> {
            on { getTextChannelById(goodChannelId) } doReturn goodChannel
            on { getTextChannelById(badChannelId) } doReturn badChannel
        }

        runBlocking {
            AdminNotifier(jda).sendAdminMessage("", listOf(goodChannelId, badChannelId))
        }

        assertThat(goodChannelSucceeded).isTrue()
    }
}