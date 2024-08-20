package au.com.skater901.wc3connect.api.commands

import au.com.skater901.wc3connect.api.core.domain.exceptions.InvalidRegexPatternException
import au.com.skater901.wc3connect.api.core.service.NotificationService
import au.com.skater901.wc3connect.discord.api.commands.RegisterNotification
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.concurrent.CompletableFuture

class RegisterNotificationTest {
    @Test
    fun `should reply with error if no valid channel included`() {
        val notificationService = mock<NotificationService>()

        val restAction = mock<ReplyCallbackAction> {
            on { submit() } doReturn CompletableFuture.completedFuture(null)
        }

        val option = mock<OptionMapping> {
            on { asString } doReturn "regex"
        }

        val command = mock<SlashCommandInteractionEvent> {
            on { channelIdLong } doReturn 0
            on { getOption("filter") } doReturn option
            on { reply(any<String>()) } doReturn restAction
        }

        runBlocking {
            RegisterNotification(notificationService).handleCommand(command)
        }

        verifyNoInteractions(notificationService)

        verify(command) {
            1 * { reply("Notification requested from outside a channel. Please use a channel.") }
        }
    }

    @Test
    fun `should handle invalid regex`() {
        val channelId = "12345"
        val mapPattern = "bad regex"

        val notificationService = mock<NotificationService> {
            onBlocking { createNotification(channelId, mapPattern) } doThrow InvalidRegexPatternException(mapPattern)
        }

        val restAction = mock<ReplyCallbackAction> {
            on { submit() } doReturn CompletableFuture.completedFuture(null)
        }

        val option = mock<OptionMapping> {
            on { asString } doReturn mapPattern
        }

        val command = mock<SlashCommandInteractionEvent> {
            on { this.channelId } doReturn channelId
            on { getOption("filter") } doReturn option
            on { reply(any<String>()) } doReturn restAction
        }

        runBlocking {
            RegisterNotification(notificationService).handleCommand(command)
        }

        verify(command) {
            1 * { reply("Invalid regex pattern. $mapPattern") }
        }
    }

    @Test
    fun `should create new notification`() {
        val channelId = "12345"
        val mapPattern = "my cool regex pattern"

        val notificationService = mock<NotificationService>()

        val restAction = mock<ReplyCallbackAction> {
            on { submit() } doReturn CompletableFuture.completedFuture(null)
        }

        val option = mock<OptionMapping> {
            on { asString } doReturn mapPattern
        }

        val channel = mock<MessageChannelUnion> {
            on { name } doReturn "cool channel"
        }

        val command = mock<SlashCommandInteractionEvent> {
            on { this.channelId } doReturn channelId
            on { this.channel } doReturn channel
            on { getOption("filter") } doReturn option
            on { reply(any<String>()) } doReturn restAction
        }

        runBlocking {
            RegisterNotification(notificationService).handleCommand(command)
        }

        verify(notificationService) {
            1 * { runBlocking { createNotification(channelId, mapPattern) } }
        }

        verify(command) {
            1 * { reply("Registering a notification for channel [ cool channel ($channelId) ] for regex pattern [ $mapPattern ]") }
        }
    }
}