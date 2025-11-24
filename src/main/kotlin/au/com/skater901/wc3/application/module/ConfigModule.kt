package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.application.annotation.ConfigClass
import au.com.skater901.wc3.application.config.ConfigParser
import com.google.inject.Key
import com.google.inject.Provides
import com.google.inject.Scopes
import com.google.inject.name.Names.named
import dev.misfitlabs.kotlinguice4.KotlinModule
import io.github.classgraph.ScanResult
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.io.File
import java.util.*

internal class ConfigModule(scanResult: ScanResult) : KotlinModule() {
    private val configClasses = scanResult.allClasses
        .filter { it.hasAnnotation(ConfigClass::class.java) }
        .map { it.loadClass() as Class<Any> }

    override fun configure() {
        val configProperties = getProvider(Key.get(Properties::class.java).withAnnotation(named("configProperties")))

        configClasses.forEach {
            bind(it).toProvider(
                ConfigParser(
                    configProperties,
                    it.annotations.filterIsInstance<ConfigClass>().first().prefix,
                    it.kotlin
                )
            )
                .`in`(Scopes.SINGLETON)
        }
    }

    @Provides
    @Singleton
    @Named("configProperties")
    fun provideConfig(): Properties = Properties().apply {
        File(System.getProperty("configFile")).inputStream().use { load(it) }
    }
}