package au.com.skater901.wc3.discord.core.handler

import au.com.skater901.wc3.api.core.domain.Game
import au.com.skater901.wc3.api.core.domain.GameSource
import au.com.skater901.wc3.api.core.domain.Region
import au.com.skater901.wc3.api.core.domain.exceptions.InvalidNotificationException
import au.com.skater901.wc3.discord.utils.game
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.requests.restaction.MessageEditAction
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture
import kotlin.random.Random

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
            on { created } doReturn Instant.now().minusMinutes(1).minusSeconds(5)
            on { region } doReturn Region.US
            on { gameSource } doReturn GameSource.WC3Connect
        }

        val updatedGame = mock<Game> {
            on { id } doReturn 1
            on { name } doReturn "DotA All Stars join fast!!!!!"
            on { map } doReturn "DotA All Stars 6.0x"
            on { host } doReturn "IceFrog"
            on { currentPlayers } doReturn 5
            on { maxPlayers } doReturn 10
            on { created } doReturn Instant.now().minusMinutes(2).minusSeconds(5)
            on { region } doReturn Region.US
            on { gameSource } doReturn GameSource.WC3Connect
        }

        val startedGame = mock<Game> {
            on { id } doReturn 1
            on { name } doReturn "DotA All Stars join fast!!!!!"
            on { map } doReturn "DotA All Stars 6.0x"
            on { host } doReturn "IceFrog"
            on { currentPlayers } doReturn 8
            on { maxPlayers } doReturn 10
            on { created } doReturn Instant.now().minusMinutes(4).minusSeconds(5)
            on { region } doReturn Region.US
            on { gameSource } doReturn GameSource.WC3Connect
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
                                        fields.size == 3 &&
                                        fields.any { field ->
                                            field.name == "Hosted On" &&
                                                    field.value == GameSource.WC3Connect.name &&
                                                    !field.isInline
                                        } &&
                                        fields.any { field ->
                                            field.name == "Game Name" &&
                                                    field.value == ":flag_us: DotA All Stars join fast!!!!! (3/10)" &&
                                                    !field.isInline
                                        } &&
                                        fields.any { field ->
                                            field.name == "Created" &&
                                                    field.value == "1 minute ago" &&
                                                    !field.isInline
                                        } &&
                                        footer?.iconUrl == "https://entgaming.net/favicon.ico" &&
                                        footer?.text == "Powered by https://entgaming.net/"
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
                                        fields.size == 3 &&
                                        fields.any { field ->
                                            field.name == "Hosted On" &&
                                                    field.value == GameSource.WC3Connect.name &&
                                                    !field.isInline
                                        } &&
                                        fields.any { field ->
                                            field.name == "Game Name" &&
                                                    field.value == ":flag_us: DotA All Stars join fast!!!!! (5/10)" &&
                                                    !field.isInline
                                        } &&
                                        fields.any { field ->
                                            field.name == "Created" &&
                                                    field.value == "2 minutes ago" &&
                                                    !field.isInline
                                        } &&
                                        footer?.iconUrl == "https://entgaming.net/favicon.ico" &&
                                        footer?.text == "Powered by https://entgaming.net/"
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
                                        fields.size == 3 &&
                                        fields.any { field ->
                                            field.name == "Hosted On" &&
                                                    field.value == GameSource.WC3Connect.name &&
                                                    !field.isInline
                                        } &&
                                        fields.any { field ->
                                            field.name == "Game Name" &&
                                                    field.value == ":flag_us: DotA All Stars join fast!!!!! (8/10)" &&
                                                    !field.isInline
                                        } &&
                                        fields.any { field ->
                                            field.name == "Started" &&
                                                    field.value == "After 4 minutes" &&
                                                    !field.isInline
                                        } &&
                                        footer?.iconUrl == "https://entgaming.net/favicon.ico" &&
                                        footer?.text == "Powered by https://entgaming.net/"
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

    @Test
    fun `should strip map extension off BattleNet game maps`() {
        verifyMessageSent(game()) {
            title == "Best_Map"
        }
    }

    @Test
    fun `should not modify map name for WC3Connect game maps`() {
        verifyMessageSent(game {
            gameSource = GameSource.WC3Connect
        }) {
            title == "Best_Map.w3x"
        }
    }

    @Test
    fun `should link to map on WC3Maps for BattleNet game`() {
        verifyMessageSent(game()) {
            url == "https://wc3maps.com/maps?query=Best_Map.w3x"
        }
    }

    @Test
    fun `should not have link to map for WC3Connect game`() {
        verifyMessageSent(game {
            gameSource = GameSource.WC3Connect
        }) {
            url == null
        }
    }

    @Test
    fun `should show where game is hosted`() {
        verifyMessageSent(game()) {
            fields.any { field ->
                field.name == "Hosted On" && field.value == "Battle.Net"
            }
        }
        verifyMessageSent(game {
            gameSource = GameSource.WC3Connect
        }) {
            fields.any { field ->
                field.name == "Hosted On" && field.value == GameSource.WC3Connect.name
            }
        }
    }

    @Test
    fun `should display correct flag for region`() {
        verifyMessageSent(game()) {
            fields.any { field ->
                field.name == "Game Name" &&
                        field.value == ":flag_us: My cool game (1/8)"
            }
        }
        verifyMessageSent(game {
            region = Region.EU
        }) {
            fields.any { field ->
                field.name == "Game Name" &&
                        field.value == ":flag_eu: My cool game (1/8)"
            }
        }
        verifyMessageSent(game {
            region = Region.Asia
        }) {
            fields.any { field ->
                field.name == "Game Name" &&
                        field.value == ":flag_kr: My cool game (1/8)"
            }
        }
        verifyMessageSent(game {
            region = Region.Unknown
        }) {
            fields.any { field ->
                field.name == "Game Name" &&
                        field.value == ":earth_americas: My cool game (1/8)"
            }
        }
    }

    @Test
    fun `should display time since game started in seconds if game started less than 1 minute ago`() {
        verifyMessageSent(game {
            created = Instant.now().minusSeconds(Random.nextLong(55))
        }) {
            fields.any { field ->
                field.name == "Created" && field.value?.endsWith(" seconds ago") == true
            }
        }
    }

    @Test
    fun `should use minute for game created less than two minutes ago`() {
        verifyMessageSent(game {
            created = Instant.now().minusMinutes(1)
        }) {
            fields.any { field ->
                field.name == "Created" && field.value == "1 minute ago"
            }
        }
    }

    @Test
    fun `should use minutes for game created two minutes or more ago`() {
        verifyMessageSent(game {
            created = Instant.now().minusMinutes(2)
        }) {
            fields.any { field ->
                field.name == "Created" && field.value == "2 minutes ago"
            }
        }
    }

    @Test
    fun `footer should show powered by WC3Stats for BattleNet game`() {
        verifyMessageSent(game()) {
            footer?.text == "Powered by https://wc3stats.com/" &&
                    footer?.iconUrl == "https://wc3stats.com/assets/favicon.ico"
        }
    }

    @Test
    fun `footer should show powered by WC3Connect for WC3Connect game`() {
        verifyMessageSent(game {
            gameSource = GameSource.WC3Connect
        }) {
            footer?.text == "Powered by https://entgaming.net/" &&
                    footer?.iconUrl == "https://entgaming.net/favicon.ico"
        }
    }

    private fun verifyMessageSent(game: Game, verification: MessageEmbed.() -> Boolean) {
        val notificationId = "123456789"

        val message = mock<Message>()
        val messageCreateAction = mock<MessageCreateAction> {
            on { submit() } doReturn CompletableFuture.completedFuture(message)
        }
        val channel = mock<TextChannel> {
            on { sendMessage(any<MessageCreateData>()) } doReturn messageCreateAction
        }
        val jda = mock<JDA> {
            on { getTextChannelById(notificationId) } doReturn channel
        }

        runBlocking {
            DiscordGameNotifier(jda).notifyNewGame(notificationId, game)
        }

        verify(channel) {
            1 * {
                sendMessage(argThat<MessageCreateData> {
                    embeds.size == 1 &&
                            embeds.first().run(verification)
                })
            }
        }
    }

    private fun Instant.minusMinutes(amount: Long): Instant = minus(amount, ChronoUnit.MINUTES)
}