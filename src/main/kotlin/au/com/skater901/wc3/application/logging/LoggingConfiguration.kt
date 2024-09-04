package au.com.skater901.wc3.application.logging

import au.com.skater901.wc3.application.config.LogConfiguration
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.classic.spi.Configurator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.OutputStreamAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.spi.ContextAwareBase
import jakarta.inject.Inject
import java.util.*

internal class LoggingConfiguration : Configurator, ContextAwareBase() {
    override fun configure(context: LoggerContext): Configurator.ExecutionStatus {
        context.stop()
        context.getRootLogger().detachAndStopAllAppenders()

        // Pretty much all of this code is copied from DefaultLoggingFactory and FileAppenderFactory classes in dropwizard-logging library.
        if (consoleLogLevel != Level.OFF) {
            val consoleAppender = ConsoleAppender<ILoggingEvent>().apply {
                configure(context, consoleLogLevel)
                start()
            }

            context.getRootLogger().addAppender(consoleAppender)
        }

        if (fileLogLevel != Level.OFF) {
            val logPath = "$logsDirectory/wc3-notification-bot/wc3-notification-bot"

            val fileAppender = RollingFileAppender<ILoggingEvent>().apply {
                configure(context, fileLogLevel)
                file = "$logPath.log"
            }

            val rollingPolicy = TimeBasedRollingPolicy<ILoggingEvent>().apply {
                setContext(context)
                fileNamePattern = "$logPath-%d.tar.gz"
                maxHistory = logFileArchiveCount
            }

            DefaultTimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent>().apply {
                setContext(context)
                setTimeBasedRollingPolicy(rollingPolicy)
                fileAppender.triggeringPolicy = this
            }

            fileAppender.rollingPolicy = rollingPolicy

            rollingPolicy.setParent(fileAppender)
            rollingPolicy.start()

            context.getRootLogger().addAppender(fileAppender)
            fileAppender.start()
        }

        return Configurator.ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY
    }

    private fun LoggerContext.getRootLogger(): Logger = getLogger(Logger.ROOT_LOGGER_NAME)

    private fun OutputStreamAppender<ILoggingEvent>.configure(context: LoggerContext, level: Level) {
        val loggingPattern = PatternLayout().apply {
            pattern = "%-5p [%d{ISO8601," + TimeZone.getDefault().id + "}] %c: %m%n"
            setContext(context)
            start()
        }

        val encoder = LayoutWrappingEncoder<ILoggingEvent>().apply { layout = loggingPattern }

        addFilter(ThresholdFilter().apply { setLevel(level.levelStr); start() })
        setContext(context)
        setEncoder(encoder)
    }

    companion object {
        private var consoleLogLevel = Level.OFF
        private var fileLogLevel = Level.INFO
        private var logsDirectory = "/local/logs"
        private var logFileArchiveCount = 7

        @Inject
        @JvmStatic
        fun configure(configuration: LogConfiguration) {
            configuration.consoleLoggingLevel?.also { consoleLogLevel = Level.toLevel(it, Level.OFF) }
            configuration.fileLoggingLevel?.also { fileLogLevel = Level.toLevel(it, Level.INFO) }
            configuration.logFileDirectory?.also { logsDirectory = it }
            configuration.logFileArchiveCount?.also { logFileArchiveCount = it }
        }
    }
}