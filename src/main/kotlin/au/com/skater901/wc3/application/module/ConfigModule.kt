package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.application.annotation.ConfigClass
import au.com.skater901.wc3.application.config.ConfigParser
import au.com.skater901.wc3.application.logging.LoggingConfiguration
import com.google.inject.AbstractModule
import com.google.inject.Key
import com.google.inject.Provides
import com.google.inject.Scopes
import com.google.inject.name.Names.named
import io.github.classgraph.ScanResult
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.io.File
import java.util.*

internal class ConfigModule(
    private val scanResult: ScanResult
) : AbstractModule() {
    override fun configure() {
        val configProperties = getProvider(Key.get(Properties::class.java).withAnnotation(named("configProperties")))

        scanResult.allClasses
            .filter { it.hasAnnotation(ConfigClass::class.java) }
            .forEach {
                val klass = it.loadClass() as Class<Any>
                bind(klass).toProvider(
                    ConfigParser(
                        configProperties,
                        klass.annotations.filterIsInstance<ConfigClass>().first().prefix,
                        klass.kotlin
                    )
                )
                    .`in`(Scopes.SINGLETON)
            }

        requestStaticInjection(LoggingConfiguration::class.java)
    }

    @Provides
    @Singleton
    @Named("configProperties")
    fun provideConfig(): Properties = Properties().apply {
        File(System.getProperty("configFile")).inputStream().use { load(it) }
    }
}