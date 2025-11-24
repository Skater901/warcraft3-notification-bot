package au.com.skater901.wc3.application.bundles

import au.com.skater901.wc3.application.module.ConfigModule
import dev.misfitlabs.kotlinguice4.KotlinModule
import io.dropwizard.configuration.SubstitutingSourceProvider
import io.dropwizard.core.setup.Bootstrap
import io.github.classgraph.ScanResult
import org.apache.commons.text.StringSubstitutor
import java.io.File
import java.util.*

internal class ConfigBundle(
    scanResult: ScanResult,
    private val classLoader: ClassLoader
) : BaseBundle {
    override fun initialize(bootstrap: Bootstrap<*>) {
        val defaultProperties = Properties().apply {
            load(classLoader.getResourceAsStream("config.properties"))
        }

        val configFilePath = System.getProperty("configFile")
            ?: throw IllegalArgumentException("Required system property [ configFile ] has not been set. Please set it, with a path to a config file, using -DconfigFile=/path/to/config/file.properties")

        // check file exists
        if (!File(configFilePath).exists()) throw IllegalArgumentException("Config file [ $configFilePath ] does not exist.")

        val properties = Properties(defaultProperties).apply {
            load(
                File(configFilePath)
                    .inputStream()
            )
        }

        bootstrap.configurationSourceProvider = SubstitutingSourceProvider(
            bootstrap.configurationSourceProvider,
            StringSubstitutor {
                properties.getProperty(it)
            }
        )
    }

    override val module: KotlinModule? = ConfigModule(scanResult)
}