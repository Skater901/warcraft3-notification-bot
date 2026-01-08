package au.com.skater901.wc3.discord.api.commands

import au.com.skater901.wc3.api.application.HealthChecksProvider
import com.codahale.metrics.health.HealthCheck
import jakarta.ws.rs.ProcessingException
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.net.SocketTimeoutException
import java.util.concurrent.CompletableFuture

class HealthCheckTest {
    @Test
    fun `should report health checks`() {
        val healthChecksProvider = mock<HealthChecksProvider> {
            on { healthChecks() } doReturn mapOf(
                "health check 1" to HealthCheck.Result.healthy(),
                "health check 2" to HealthCheck.Result.unhealthy("No games found."),
                "health check 3" to HealthCheck.Result.unhealthy(ProcessingException(SocketTimeoutException("read timed out")))
            )
        }

        val reply = mock<ReplyCallbackAction> {
            on { submit() } doReturn CompletableFuture.completedFuture(null)
        }

        val command = mock<SlashCommandInteractionEvent> {
            on { reply(any<MessageCreateData>()) } doReturn reply
        }

        runBlocking { HealthCheck(healthChecksProvider).handleCommand(command) }

        verify(command) {
            1 * {
                reply(argThat<MessageCreateData> {
                    embeds.size == 1 &&
                            embeds.first()
                                .run {
                                    title == "Health Checks" &&
                                            color?.red == 0 &&
                                            color?.green == 255 &&
                                            color?.blue == 255 &&
                                            fields.size == 3 &&
                                            fields[0].run {
                                                name == "health check 1" &&
                                                        value == "Healthy"
                                            } &&
                                            fields[1].run {
                                                name == "health check 2" &&
                                                        value == "Unhealthy: No games found."
                                            } &&
                                            fields[2].run {
                                                name == "health check 3" &&
                                                        value == "Unhealthy: jakarta.ws.rs.ProcessingException: java.net.SocketTimeoutException: read timed out\nCaused by: java.net.SocketTimeoutException: read timed out"
                                            }

                                }
                })
            }
        }
    }
}