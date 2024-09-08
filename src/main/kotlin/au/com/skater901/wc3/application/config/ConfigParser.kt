package au.com.skater901.wc3.application.config

import jakarta.inject.Provider
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubclassOf
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
                    it.type.isMarkedNullable -> it to properties.getNullableProperty(propertyWithModulePrefix(it))
                    else -> it to properties.getPropertyOrThrow(it)
                }
            }
            .associate { (param, property) ->
                param to property?.let { p ->
                    when {
                        param.type.jvmErasure.isSubclassOf(Enum::class) -> tryConvertProperty<Enum<*>>(
                            param,
                            p,
                            param.type.jvmErasure as KClass<Enum<*>>
                        ) {
                            val enumValues = param.type
                                .jvmErasure
                                .java
                                .enumConstants

                            enumValues.firstOrNull { (it as Enum<*>).name.equals(property, ignoreCase = true) }
                                ?.let { it as Enum<*> }
                                ?: throw IllegalArgumentException("Property value [ $property ] is not one of [ ${enumValues.joinToString()} ]")
                        }

                        else -> when (param.type.jvmErasure) {
                            String::class -> p
                            Int::class -> tryConvertProperty(param, p) { it.toInt() }
                            Long::class -> tryConvertProperty(param, p) { it.toLong() }
                            URI::class -> tryConvertProperty(param, p) { URI(it) }

                            else -> throw IllegalArgumentException("Config class [ ${configClass.qualifiedName} ] has parameter [ ${param.name} ] of type [ ${param.type.jvmErasure.qualifiedName} ] which is not currently supported by the config parser.")
                        }
                    }
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

    private fun Properties.getNullableProperty(propertyName: String): String? = getProperty(propertyName)

    private inline fun <reified T : Any> tryConvertProperty(
        parameter: KParameter,
        value: String,
        typeClass: KClass<T> = T::class,
        convertor: (String) -> T
    ): T = try {
        convertor(value)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException(
            "Config property [ $moduleName.${parameter.name} ] could not be converted to [ ${typeClass.qualifiedName} ], value was [ $value ]",
            e
        )
    } catch (e: URISyntaxException) {
        throw IllegalArgumentException(
            "Config property [ $moduleName.${parameter.name} ] could not be converted to [ ${typeClass.qualifiedName} ], value was [ $value ]",
            e
        )
    }
}
