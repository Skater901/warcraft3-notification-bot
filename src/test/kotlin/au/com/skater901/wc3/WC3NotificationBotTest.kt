package au.com.skater901.wc3

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class WC3NotificationBotTest {
    @Test
    fun `should check that config property has been set`() {
        System.clearProperty("configFile")

        assertThatThrownBy { WC3NotificationBot.main(emptyArray()) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Required system property [ configFile ] has not been set. Please set it, with a path to a config file, using -DconfigFile=/path/to/config/file.properties")
    }

    @Test
    fun `should check that config file exists`() {
        System.setProperty("configFile", "notarealfile")

        assertThatThrownBy { WC3NotificationBot.main(emptyArray()) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Config file [ notarealfile ] does not exist.")

        System.clearProperty("configFile")
    }
}