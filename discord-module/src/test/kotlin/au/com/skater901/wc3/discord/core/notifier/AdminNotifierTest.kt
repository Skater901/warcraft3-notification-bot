package au.com.skater901.wc3.discord.core.notifier

import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
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

        val wC3GameNotificationService = mock<WC3GameNotificationService>()

        runBlocking { AdminNotifier(jda, wC3GameNotificationService).sendAdminMessage(adminMessage, notifications) }

        verify(channel1) {
            1 * { sendMessage(any<MessageCreateData>()) }
        }
        verify(messageCreateAction) {
            1 * { submit() }
        }
        verify(wC3GameNotificationService) {
            1 * { runBlocking { deleteNotification("channel2") } }
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

        val wC3GameNotificationService = mock<WC3GameNotificationService>()

        runBlocking {
            AdminNotifier(jda, wC3GameNotificationService).sendAdminMessage("", listOf(goodChannelId, badChannelId))
        }

        assertThat(goodChannelSucceeded).isTrue()

        verifyNoInteractions(wC3GameNotificationService)
    }

    @Test
    fun `should remove channel if unable to send message`() {
        val notifications = listOf("channel1")
        val adminMessage = "test"

        val guild = mock<Guild> {
            on { idLong } doReturn 1L
        }
        val exception = InsufficientPermissionException(guild, Permission.MESSAGE_SEND)
        val messageCreateAction = mock<MessageCreateAction> {
            on { submit() } doReturn CompletableFuture.failedFuture(exception)
        }
        val channel1 = mock<TextChannel> {
            on { sendMessage(any<MessageCreateData>()) } doReturn messageCreateAction
        }
        val jda = mock<JDA> {
            on { getTextChannelById("channel1") } doReturn channel1
        }

        val wC3GameNotificationService = mock<WC3GameNotificationService>()

        runBlocking { AdminNotifier(jda, wC3GameNotificationService).sendAdminMessage(adminMessage, notifications) }

        verify(channel1) {
            1 * { sendMessage(any<MessageCreateData>()) }
        }
        verify(messageCreateAction) {
            1 * { submit() }
        }
        verify(wC3GameNotificationService) {
            1 * { runBlocking { deleteNotification("channel1") } }
        }
    }
}