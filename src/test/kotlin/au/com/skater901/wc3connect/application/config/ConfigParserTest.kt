package au.com.skater901.wc3connect.application.config

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.net.URI
import java.util.*

class ConfigParserTest {
    private class MyInvalidConfigClass {
        constructor(myParameter: String) {
            myProperty = myParameter
        }

        val myProperty: String
    }

    @Test
    fun `should raise error if config class has no primary constructor`() {
        assertThatThrownBy {
            ConfigParser({ Properties() }, "myModule", MyInvalidConfigClass::class).get()
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Config class [ au.com.skater901.wc3connect.application.config.ConfigParserTest.MyInvalidConfigClass ] must have primary constructor")
    }

    private class MyValidConfig(val myValidProperty: String, val myMissingProperty: String)

    @Test
    fun `should raise error for missing property`() {
        val properties = Properties().apply { setProperty("myModule.myValidProperty", "hello") }

        assertThatThrownBy {
            ConfigParser({ properties }, "myModule", MyValidConfig::class).get()
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("No config property provided for [ myModule.myMissingProperty ]")
    }

    private class MyConfigWithInvalidPropertyType(val invalidPropertyType: BigDecimal)

    @Test
    fun `should raise error for unhandled property type`() {
        val properties = Properties().apply { setProperty("myModule.invalidPropertyType", "12345") }

        assertThatThrownBy {
            ConfigParser({ properties }, "myModule", MyConfigWithInvalidPropertyType::class).get()
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Config class [ au.com.skater901.wc3connect.application.config.ConfigParserTest.MyConfigWithInvalidPropertyType ] has parameter [ invalidPropertyType ] of type [ java.math.BigDecimal ] which is not currently supported by the config parser.")
    }

    private class MyIntConfig(val myProperty: Int)
    private class MyLongConfig(val myProperty: Long)
    private class MyURIConfig(val myProperty: URI)

    @Test
    fun `should raise error when provided config value can't be converted to type on config class`() {
        val properties = Properties().apply { setProperty("myModule.myProperty", "ha ha not a number or URI SUCKERS") }

        assertThatThrownBy {
            ConfigParser({ properties }, "myModule", MyIntConfig::class).get()
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Config property [ myModule.myProperty ] could not be converted to [ kotlin.Int ], value was [ ha ha not a number or URI SUCKERS ]")
        assertThatThrownBy {
            ConfigParser({ properties }, "myModule", MyLongConfig::class).get()
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Config property [ myModule.myProperty ] could not be converted to [ kotlin.Long ], value was [ ha ha not a number or URI SUCKERS ]")
        assertThatThrownBy {
            ConfigParser({ properties }, "myModule", MyURIConfig::class).get()
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Config property [ myModule.myProperty ] could not be converted to [ java.net.URI ], value was [ ha ha not a number or URI SUCKERS ]")
    }

    class MyValidConfigWithAllTypes(
        val myStringProperty: String,
        val myIntProperty: Int,
        val myLongProperty: Long,
        val myURIProperty: URI
    )

    @Test
    fun `should parse config successfully`() {
        val properties = Properties().apply {
            setProperty("myModule.myStringProperty", "Hello World")
            setProperty("myModule.myIntProperty", "12345")
            setProperty("myModule.myLongProperty", "111111111111111")
            setProperty("myModule.myURIProperty", "http://localhost")
        }

        val config = ConfigParser({ properties }, "myModule", MyValidConfigWithAllTypes::class).get()

        assertThat(config.myStringProperty).isEqualTo("Hello World")
        assertThat(config.myIntProperty).isEqualTo(12345)
        assertThat(config.myLongProperty).isEqualTo(111111111111111)
        assertThat(config.myURIProperty.toString()).isEqualTo("http://localhost")
    }

    class NullableConfig(val myProperty: String?)

    @Test
    fun `should handle nullable properties`() {
        val properties = Properties().apply {
            setProperty("myModule.myProperty", "Hello World")
        }

        var config = ConfigParser({ properties }, "myModule", NullableConfig::class).get()

        assertThat(config.myProperty).isEqualTo("Hello World")

        properties.clear()

        config = ConfigParser({ properties }, "myModule", NullableConfig::class).get()

        assertThat(config.myProperty).isNull()
    }

    class DefaultConfig(val myProperty: String = "hello")

    @Test
    fun `should handle properties with default values`() {
        val properties = Properties().apply {
            setProperty("myModule.myProperty", "Hello World")
        }

        var config = ConfigParser({ properties }, "myModule", DefaultConfig::class).get()

        assertThat(config.myProperty).isEqualTo("Hello World")

        properties.clear()

        config = ConfigParser({ properties }, "myModule", DefaultConfig::class).get()

        assertThat(config.myProperty).isEqualTo("hello")
    }
}