package au.com.skater901.w3cconnect.application.config

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ConfigParserKtTest {
    @Test
    fun `should raise error if config file not specified`() {
        assertThatThrownBy {
            parseConfig()
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Required system property [ configFile ] has not been set. Please set it, with a path to a config file, using -DconfigFile=/path/to/config/file.properties")
    }

    @Test
    fun `should raise error if config file does not exist`() {
        System.setProperty("configFile", "notARealFile")

        assertThatThrownBy {
            parseConfig()
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Config file [ notARealFile ] does not exist.")
    }
}