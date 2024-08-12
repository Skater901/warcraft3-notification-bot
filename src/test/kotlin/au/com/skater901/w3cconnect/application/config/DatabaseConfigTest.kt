package au.com.skater901.w3cconnect.application.config

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.util.*

class DatabaseConfigTest {
    @Test
    fun `should raise error if database port property is not an int`() {
        val properties = Properties().apply {
            setProperty("database.host", "localhost")
            setProperty("database.port", "not a number")
            setProperty("database.username", "myuser")
            setProperty("database.password", "mypassword")
        }

        assertThatThrownBy {
            DatabaseConfig.parse("database", properties)
        }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("Database port property [ database.port ] must be an integer, was [ not a number ]")
    }

    @Test
    fun `should raise error if property is not present`() {
        listOf(
            Properties().apply {
                setProperty("database.port", "3306")
                setProperty("database.username", "myuser")
                setProperty("database.password", "mypassword")
            } to "database.host",
            Properties().apply {
                setProperty("database.host", "localhost")
                setProperty("database.username", "myuser")
                setProperty("database.password", "mypassword")
            } to "database.port",
            Properties().apply {
                setProperty("database.host", "localhost")
                setProperty("database.port", "3306")
                setProperty("database.password", "mypassword")
            } to "database.username",
            Properties().apply {
                setProperty("database.host", "localhost")
                setProperty("database.port", "3306")
                setProperty("database.username", "myuser")
            } to "database.password"
        )
            .forEach { (properties, missingProperty) ->
                assertThatThrownBy {
                    DatabaseConfig.parse("database", properties)
                }
                    .isInstanceOf(IllegalStateException::class.java)
                    .hasMessage("No config property provided for [ $missingProperty ]")
            }
    }

    @Test
    fun `should parse database config`() {
        val properties = Properties().apply {
            setProperty("database.host", "localhost")
            setProperty("database.port", "3306")
            setProperty("database.username", "myuser")
            setProperty("database.password", "mypassword")
        }

        val databaseConfig = DatabaseConfig.parse("database", properties)
        assertThat(databaseConfig.host).isEqualTo("localhost")
        assertThat(databaseConfig.port).isEqualTo(3306)
        assertThat(databaseConfig.username).isEqualTo("myuser")
        assertThat(databaseConfig.password).isEqualTo("mypassword")
    }
}