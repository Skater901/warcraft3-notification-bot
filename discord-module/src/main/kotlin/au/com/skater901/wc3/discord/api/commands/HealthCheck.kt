package au.com.skater901.wc3.discord.api.commands

import au.com.skater901.wc3.api.application.HealthChecksProvider
import au.com.skater901.wc3.utilities.metricsWork
import dev.minn.jda.ktx.messages.MessageCreate
import jakarta.inject.Inject
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

internal class HealthCheck @Inject constructor(
    private val healthChecksProvider: HealthChecksProvider
) : Command {
    override val name: String = "healthcheck"
    override val description: String = "Show the health checks of the bot."

    private val handleCommandWork = metricsWork(::handleCommand)
    override suspend fun handleCommand(command: SlashCommandInteractionEvent) {
        handleCommandWork {
            command.replySuspended(
                MessageCreate {
                    embed {
                        title = "Health Checks"

                        color = 0x00FFFF

                        healthChecksProvider.healthChecks()
                            .forEach { (name, result) ->
                                field {
                                    this.name = name

                                    this.value = when {
                                        result.isHealthy -> "Healthy"
                                        result.error != null -> "Unhealthy: ${exceptionToString(result.error)}"
                                        else -> "Unhealthy: ${result.message}"
                                    }

                                    inline = false
                                }
                            }
                    }
                }
            )
        }
    }

    private fun exceptionToString(exception: Throwable, alreadySeenExceptions: Set<Throwable> = emptySet()): String =
        "${if (alreadySeenExceptions.isEmpty()) "" else "Caused by: "}${exception::class.qualifiedName}: ${exception.message ?: ""}${
            if (exception.cause != null && exception.cause !in alreadySeenExceptions)
                "\n" + exceptionToString(exception.cause!!, alreadySeenExceptions + exception)
            else
                ""
        }"
}