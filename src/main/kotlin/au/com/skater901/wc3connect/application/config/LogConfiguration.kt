package au.com.skater901.wc3connect.application.config

internal class LogConfiguration(
    val consoleLoggingLevel: String?,
    val fileLoggingLevel: String?,
    val logFileDirectory: String?,
    val logFileArchiveCount: Int?
)