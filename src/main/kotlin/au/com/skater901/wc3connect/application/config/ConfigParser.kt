package au.com.skater901.wc3connect.application.config

import jakarta.inject.Provider
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

internal class ConfigParser<T : Any>(
    private val propertiesProvider: Provider<Properties>,
    private val moduleName: String,
    private val configClass: KClass<out T>
) : Provider<T> {
    override fun get(): T {
        val primaryConstructor = configClass.primaryConstructor
            ?: throw IllegalArgumentException("Config class [ ${configClass.qualifiedName} ] must have primary constructor")

        val properties = propertiesProvider.get()

        val parameters = primaryConstructor.parameters
            .mapNotNull {
                when {
                    it.isOptional -> properties.getProperty(propertyWithModulePrefix(it))?.let { v -> it to v }
                    it.type.isMarkedNullable -> it to properties.getProperty(propertyWithModulePrefix(it))
                    else -> it to properties.getPropertyOrThrow(it)
                }
            }
            .associate { (param, property) ->
                param to when (param.type.jvmErasure) {
                    String::class -> property
                    Int::class -> tryConvertProperty(param, property) { it.toInt() }
                    Long::class -> tryConvertProperty(param, property) { it.toLong() }
                    URI::class -> tryConvertProperty(param, property) { URI(it) }
                    else -> throw IllegalArgumentException("Config class [ ${configClass.qualifiedName} ] has parameter [ ${param.name} ] of type [ ${param.type.jvmErasure.qualifiedName} ] which is not currently supported by the config parser.")
                }
            }

        return primaryConstructor.callBy(parameters)
    }

    private fun propertyWithModulePrefix(parameter: KParameter): String = "$moduleName.${parameter.name}"

    private fun Properties.getPropertyOrThrow(parameter: KParameter): String {
        val propertyWithModulePrefix = propertyWithModulePrefix(parameter)

        return getProperty(propertyWithModulePrefix)
            ?: throw IllegalArgumentException("No config property provided for [ $propertyWithModulePrefix ]")
    }

    private inline fun <reified T> tryConvertProperty(
        parameter: KParameter,
        value: String,
        convertor: (String) -> T
    ): T = try {
        convertor(value)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("Config property [ $moduleName.${parameter.name} ] could not be converted to [ ${T::class.qualifiedName} ], value was [ $value ]")
    } catch (e: URISyntaxException) {
        throw IllegalArgumentException("Config property [ $moduleName.${parameter.name} ] could not be converted to [ ${T::class.qualifiedName} ], value was [ $value ]")
    }
}
