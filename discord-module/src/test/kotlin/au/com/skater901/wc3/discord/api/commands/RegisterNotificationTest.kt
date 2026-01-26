package au.com.skater901.wc3.discord.api.commands

import au.com.skater901.wc3.api.core.domain.exceptions.InvalidRegexPatternException
import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import au.com.skater901.wc3.discord.core.dao.RoleNotificationDAO
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Role
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
        val wc3GameNotificationService = mock<WC3GameNotificationService>()
        val roleNotificationDAO = mock<RoleNotificationDAO>()

        val restAction = mock<ReplyCallbackAction> {
            on { submit() } doReturn CompletableFuture.completedFuture(null)
        }

        val option = mock<OptionMapping> {
            on { asString } doReturn "regex"
        }

        val command = mock<SlashCommandInteractionEvent> {
            on { channelIdLong } doReturn 0
            on { getOption("filter") } doReturn option
            on { getOption("notified-role") } doReturn null
            on { reply(any<String>()) } doReturn restAction
        }

        runBlocking {
            RegisterNotification(wc3GameNotificationService, roleNotificationDAO).handleCommand(command)
        }

        verifyNoInteractions(wc3GameNotificationService, roleNotificationDAO)

        verify(command) {
            1 * { reply("Notification requested from outside a channel. Please use a channel.") }
        }
    }

    @Test
    fun `should handle invalid regex`() {
        val channelId = "12345"
        val mapPattern = "bad regex"

        val wc3GameNotificationService = mock<WC3GameNotificationService> {
            on { createNotification(channelId, mapPattern) } doThrow InvalidRegexPatternException(mapPattern)
        }
        val roleNotificationDAO = mock<RoleNotificationDAO>()

        val restAction = mock<ReplyCallbackAction> {
            on { submit() } doReturn CompletableFuture.completedFuture(null)
        }

        val option = mock<OptionMapping> {
            on { asString } doReturn mapPattern
        }

        val command = mock<SlashCommandInteractionEvent> {
            on { this.channelId } doReturn channelId
            on { getOption("filter") } doReturn option
            on { getOption("notified-role") } doReturn null
            on { reply(any<String>()) } doReturn restAction
        }

        runBlocking {
            RegisterNotification(wc3GameNotificationService, roleNotificationDAO).handleCommand(command)
        }

        verify(command) {
            1 * { reply("Invalid regex pattern. $mapPattern") }
        }

        verifyNoInteractions(roleNotificationDAO)
    }

    @Test
    fun `should create new notification`() {
        val channelId = "12345"
        val mapPattern = "my cool regex pattern"

        val wc3GameNotificationService = mock<WC3GameNotificationService>()
        val roleNotificationDAO = mock<RoleNotificationDAO>()

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
            on { getOption("notified-role") } doReturn null
            on { reply(any<String>()) } doReturn restAction
        }

        runBlocking {
            RegisterNotification(wc3GameNotificationService, roleNotificationDAO).handleCommand(command)
        }

        verify(wc3GameNotificationService) {
            1 * { runBlocking { createNotification(channelId, mapPattern) } }
        }

        verify(command) {
            1 * { reply("Registering a notification for channel **cool channel** for regex pattern **$mapPattern**") }
        }

        verifyNoInteractions(roleNotificationDAO)
    }

    @Test
    fun `should create new notification with notified role`() {
        val channelId = "12345"
        val mapPattern = "my cool regex pattern"

        val wc3GameNotificationService = mock<WC3GameNotificationService>()
        val roleNotificationDAO = mock<RoleNotificationDAO>()

        val restAction = mock<ReplyCallbackAction> {
            on { submit() } doReturn CompletableFuture.completedFuture(null)
        }

        val filterOption = mock<OptionMapping> {
            on { asString } doReturn mapPattern
        }

        val notifiedRoleId = "11111"
        val notifiedRoleName = "test role"
        val notifiedRole = mock<Role> {
            on { id } doReturn notifiedRoleId
            on { name } doReturn notifiedRoleName
        }

        val notifiedRoleOption = mock<OptionMapping> {
            on { asRole } doReturn notifiedRole
        }

        val channel = mock<MessageChannelUnion> {
            on { name } doReturn "cool channel"
        }

        val command = mock<SlashCommandInteractionEvent> {
            on { this.channelId } doReturn channelId
            on { this.channel } doReturn channel
            on { getOption("filter") } doReturn filterOption
            on { getOption("notified-role") } doReturn notifiedRoleOption
            on { reply(any<String>()) } doReturn restAction
        }

        runBlocking {
            RegisterNotification(wc3GameNotificationService, roleNotificationDAO).handleCommand(command)
        }

        verify(wc3GameNotificationService) {
            1 * { runBlocking { createNotification(channelId, mapPattern) } }
        }

        verify(command) {
            1 * { reply("Registering a notification for channel **cool channel** for regex pattern **$mapPattern** and role **$notifiedRoleName**") }
        }

        verify(roleNotificationDAO) {
            1 * { runBlocking { save(channelId, notifiedRoleId) } }
        }
    }
}