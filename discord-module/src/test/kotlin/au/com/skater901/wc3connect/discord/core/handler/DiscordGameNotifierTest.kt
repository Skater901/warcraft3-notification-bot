package au.com.skater901.wc3connect.discord.core.handler

import au.com.skater901.wc3connect.api.core.domain.Game
import au.com.skater901.wc3connect.api.core.domain.exceptions.InvalidNotificationException
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.requests.restaction.MessageEditAction
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.concurrent.CompletableFuture

class DiscordGameNotifierTest {
    @Test
    fun `should notify new game, update it, and close it`() {
        val notificationId = "123456789"
        val messageId = "123"

        val messageEditAction = mock<MessageEditAction>()
        val messageChannel = mock<MessageChannelUnion> {
            on { editMessageById(eq(messageId), any<MessageEditData>()) } doReturn messageEditAction
        }
        val message = mock<Message> {
            on { id } doReturn messageId
            on { channel } doReturn messageChannel
        }
        messageEditAction.stub {
            on { submit() } doReturn CompletableFuture.completedFuture(message)
        }
        val messageCreateAction = mock<MessageCreateAction> {
            on { submit() } doReturn CompletableFuture.completedFuture(message)
        }
        val channel = mock<TextChannel> {
            on { sendMessage(any<MessageCreateData>()) } doReturn messageCreateAction
        }
        val jda = mock<JDA> {
            on { getTextChannelById(notificationId) } doReturn channel
        }

        val notifier = DiscordGameNotifier(jda)

        val game = mock<Game> {
            on { id } doReturn 1
            on { name } doReturn "DotA All Stars join fast!!!!!"
            on { map } doReturn "DotA All Stars 6.0x"
            on { host } doReturn "IceFrog"
            on { currentPlayers } doReturn 3
            on { maxPlayers } doReturn 10
            on { uptime } doReturn "1"
        }

        val updatedGame = mock<Game> {
            on { id } doReturn 1
            on { name } doReturn "DotA All Stars join fast!!!!!"
            on { map } doReturn "DotA All Stars 6.0x"
            on { host } doReturn "IceFrog"
            on { currentPlayers } doReturn 5
            on { maxPlayers } doReturn 10
            on { uptime } doReturn "2"
        }

        val startedGame = mock<Game> {
            on { id } doReturn 1
            on { name } doReturn "DotA All Stars join fast!!!!!"
            on { map } doReturn "DotA All Stars 6.0x"
            on { host } doReturn "IceFrog"
            on { currentPlayers } doReturn 8
            on { maxPlayers } doReturn 10
            on { uptime } doReturn "4"
        }

        runBlocking {
            notifier.notifyNewGame(notificationId, game)

            notifier.updateExistingGame(updatedGame)

            notifier.closeExpiredGame(startedGame)
        }

        verify(channel) {
            1 * {
                sendMessage(argThat<MessageCreateData> {
                    embeds.size == 1 &&
                            embeds.first().run {
                                color?.red == 34 &&
                                        color?.green == 255 &&
                                        color?.blue == 0 &&

                                        author?.name == "IceFrog" &&

                                        title == "DotA All Stars 6.0x" &&
                                        fields.size == 2 &&
                                        fields.any { field ->
                                            field.name == "Game Name" &&
                                                    field.value == "DotA All Stars join fast!!!!! (3/10)" &&
                                                    !field.isInline
                                        } &&
                                        fields.any { field ->
                                            field.name == "Created" &&
                                                    field.value == "1 minutes ago" &&
                                                    !field.isInline
                                        }
                            }
                })
            }
        }
        verify(messageChannel) {
            1 * {
                editMessageById(eq("123"), argThat<MessageEditData> {
                    embeds.size == 1 &&
                            embeds.first().run {
                                color?.red == 34 &&
                                        color?.green == 255 &&
                                        color?.blue == 0 &&

                                        author?.name == "IceFrog" &&

                                        title == "DotA All Stars 6.0x" &&
                                        fields.size == 2 &&
                                        fields.any { field ->
                                            field.name == "Game Name" &&
                                                    field.value == "DotA All Stars join fast!!!!! (5/10)" &&
                                                    !field.isInline
                                        } &&
                                        fields.any { field ->
                                            field.name == "Created" &&
                                                    field.value == "2 minutes ago" &&
                                                    !field.isInline
                                        }
                            }
                })
            }
            1 * {
                editMessageById(eq("123"), argThat<MessageEditData> {
                    embeds.size == 1 &&
                            embeds.first().run {
                                color?.red == 30 &&
                                        color?.green == 31 &&
                                        color?.blue == 34 &&

                                        author?.name == "IceFrog" &&

                                        title == "DotA All Stars 6.0x" &&
                                        fields.size == 2 &&
                                        fields.any { field ->
                                            field.name == "Game Name" &&
                                                    field.value == "DotA All Stars join fast!!!!! (8/10)" &&
                                                    !field.isInline
                                        } &&
                                        fields.any { field ->
                                            field.name == "Started" &&
                                                    field.value == "After 4 minutes" &&
                                                    !field.isInline
                                        }
                            }
                })
            }
        }
    }

    @Test
    fun `should throw InvalidNotificationException if channel can't be found`() {
        val jda = mock<JDA> {
            on { getTextChannelById(any<String>()) } doReturn null
        }

        assertThatThrownBy {
            runBlocking { DiscordGameNotifier(jda).notifyNewGame("1", mock()) }
        }
            .isInstanceOf(InvalidNotificationException::class.java)
    }

    @Test
    fun `should throw InvalidNotificationException if channelId is not a number`() {
        val jda = mock<JDA> {
            on { getTextChannelById(any<String>()) } doThrow NumberFormatException()
        }

        assertThatThrownBy {
            runBlocking { DiscordGameNotifier(jda).notifyNewGame("1", mock()) }
        }
            .isInstanceOf(InvalidNotificationException::class.java)
    }

    @Test
    fun `should handle updating or closing unknown game`() {
        val notifier = DiscordGameNotifier(mock())

        val game = mock<Game> {
            on { id } doReturn 1
        }

        runBlocking {
            notifier.updateExistingGame(game)
            notifier.closeExpiredGame(game)
        }
    }
}