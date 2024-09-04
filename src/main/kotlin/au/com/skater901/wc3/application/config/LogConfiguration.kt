package au.com.skater901.wc3.application.config

import au.com.skater901.wc3.application.annotation.ConfigClass

@ConfigClass("logging")
internal class LogConfiguration(
    val consoleLoggingLevel: String?,
    val fileLoggingLevel: String?,
    val logFileDirectory: String?,
    val logFileArchiveCount: Int?
)