package au.com.skater901.w3cconnect.application.config

import java.util.*

class LogConfiguration(
    val consoleLoggingLevel: String,
    val fileLoggingLevel: String,
    val logFileDirectory: String,
    val logFileArchiveCount: Int
) {
    companion object {
        fun parse(prefix: String, properties: Properties): LogConfiguration = LogConfiguration(
            properties.getPropertyOrThrow(prefix, LogConfiguration::consoleLoggingLevel),
            properties.getPropertyOrThrow(prefix, LogConfiguration::fileLoggingLevel),
            properties.getPropertyOrThrow(prefix, LogConfiguration::logFileDirectory),
            properties.getPropertyOrThrow(prefix, LogConfiguration::logFileArchiveCount)
                .let {
                    try {
                        it.toInt()
                    } catch (e: NumberFormatException) {
                        throw IllegalStateException(
                            "Logging property [ $prefix.${LogConfiguration::logFileArchiveCount.name} ] was not a number, was [ ${
                                properties.getPropertyOrThrow(
                                    prefix,
                                    LogConfiguration::logFileArchiveCount
                                )
                            } ]"
                        )
                    }
                }
        )
    }
}