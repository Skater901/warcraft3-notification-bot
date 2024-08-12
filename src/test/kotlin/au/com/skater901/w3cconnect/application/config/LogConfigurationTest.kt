package au.com.skater901.w3cconnect.application.config

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.util.*

class LogConfigurationTest {
    @Test
    fun `should raise error if log archive count is not a number`() {
        val properties = Properties().apply {
            setProperty("log.consoleLoggingLevel", "OFF")
            setProperty("log.fileLoggingLevel", "OFF")
            setProperty("log.logFileDirectory", "build")
            setProperty("log.logFileArchiveCount", "not a number")
        }

        assertThatThrownBy {
            LogConfiguration.parse("log", properties)
        }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("Logging property [ log.logFileArchiveCount ] was not a number, was [ not a number ]")
    }

    @Test
    fun `should raise error for missing properties`() {
        listOf(
            Properties().apply {
                setProperty("log.fileLoggingLevel", "OFF")
                setProperty("log.logFileDirectory", "build")
                setProperty("log.logFileArchiveCount", "not a number")
            } to "log.consoleLoggingLevel",
            Properties().apply {
                setProperty("log.consoleLoggingLevel", "OFF")
                setProperty("log.logFileDirectory", "build")
                setProperty("log.logFileArchiveCount", "not a number")
            } to "log.fileLoggingLevel",
            Properties().apply {
                setProperty("log.consoleLoggingLevel", "OFF")
                setProperty("log.fileLoggingLevel", "OFF")
                setProperty("log.logFileArchiveCount", "not a number")
            } to "log.logFileDirectory",
            Properties().apply {
                setProperty("log.consoleLoggingLevel", "OFF")
                setProperty("log.fileLoggingLevel", "OFF")
                setProperty("log.logFileDirectory", "build")
            } to "log.logFileArchiveCount"
        )
            .forEach { (properties, missingProperty) ->
                assertThatThrownBy {
                    LogConfiguration.parse("log", properties)
                }
                    .isInstanceOf(IllegalStateException::class.java)
                    .hasMessage("No config property provided for [ $missingProperty ]")
            }
    }

    @Test
    fun `should parse config`() {
        val properties = Properties().apply {
            setProperty("log.consoleLoggingLevel", "OFF")
            setProperty("log.fileLoggingLevel", "OFF")
            setProperty("log.logFileDirectory", "build")
            setProperty("log.logFileArchiveCount", "7")
        }

        val config = LogConfiguration.parse("log", properties)
        assertThat(config.consoleLoggingLevel).isEqualTo("OFF")
        assertThat(config.fileLoggingLevel).isEqualTo("OFF")
        assertThat(config.logFileDirectory).isEqualTo("build")
        assertThat(config.logFileArchiveCount).isEqualTo(7)
    }
}