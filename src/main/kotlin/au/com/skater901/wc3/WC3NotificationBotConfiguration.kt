package au.com.skater901.wc3

import au.com.skater901.wc3.application.config.DatabaseConfig
import au.com.skater901.wc3.application.config.ServerConfiguration
import ch.qos.logback.classic.Level
import ch.qos.logback.core.spi.DeferredProcessingAware
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.dropwizard.core.Configuration
import io.dropwizard.core.server.DefaultServerFactory
import io.dropwizard.core.server.ServerFactory
import io.dropwizard.jetty.ConnectorFactory
import io.dropwizard.jetty.HttpConnectorFactory
import io.dropwizard.logging.common.ConsoleAppenderFactory
import io.dropwizard.logging.common.DefaultLoggingFactory
import io.dropwizard.logging.common.FileAppenderFactory
import io.dropwizard.logging.common.LoggingFactory
import io.dropwizard.request.logging.LogbackAccessRequestLogFactory
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.net.URI

@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
internal class WC3NotificationBotConfiguration : Configuration() {
    @JsonProperty
    @NotNull
    @Valid
    val app: ServerConfiguration? = null

    @JsonProperty
    @NotNull
    @Valid
    val database: DatabaseConfig? = null

    @JsonProperty
    @NotNull
    @Min(1)
    val refreshInterval: Long? = null

    @JsonProperty
    @NotNull
    val clients: Map<String, URI>? = null

    private val factory by lazy {
        DefaultServerFactory().apply {
            applicationConnectors = listOf(httpFactory(app!!.port!!))
            adminConnectors = listOf(httpFactory(app.adminPort!!))
            requestLogFactory = LogbackAccessRequestLogFactory().apply {
                appenders = listOfNotNull(consoleLogger(), fileLogger(true))
            }

            registerDefaultExceptionMappers = false
            minThreads = app.requestThreads!!
            maxThreads = app.requestThreads
        }
    }

    override fun getServerFactory(): ServerFactory = factory

    private val logging by lazy {
        DefaultLoggingFactory().apply {
            appenders = listOfNotNull(consoleLogger(), fileLogger(false))
        }
    }

    override fun getLoggingFactory(): LoggingFactory = logging

    private fun httpFactory(port: Int): ConnectorFactory = HttpConnectorFactory().apply {
        this.port = port
        acceptQueueSize = app!!.maxQueuedRequests!!
    }

    private fun <T : DeferredProcessingAware> consoleLogger(): ConsoleAppenderFactory<T>? {
        if (app!!.logging!!.consoleLoggingLevel!!.toLoggingLevel() == Level.OFF)
            return null

        return ConsoleAppenderFactory<T>().apply {
            threshold = app.logging.consoleLoggingLevel
        }
    }

    private fun <T : DeferredProcessingAware> fileLogger(access: Boolean): FileAppenderFactory<T>? {
        if (app!!.logging!!.fileLoggingLevel!!.toLoggingLevel() == Level.OFF)
            return null

        return FileAppenderFactory<T>().apply {
            threshold = app.logging.fileLoggingLevel
            val logFilePath =
                "${app.logging.logFileDirectory}/wc3-notification-bot/wc3-notification-bot${(if (access) "-access" else "")}"
            currentLogFilename = "$logFilePath.log"
            archivedLogFilenamePattern = "$logFilePath-%d.log.gz"
            archivedFileCount = app.logging.logFileArchiveCount!!
        }
    }

    private fun String.toLoggingLevel(): Level = when (this) {
        "false" -> Level.OFF
        else -> Level.toLevel(this, Level.INFO)
    }
}