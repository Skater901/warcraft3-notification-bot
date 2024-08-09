package au.com.skater901.w3cconnect.application.config

import java.util.*

class DatabaseConfig(
    val host: String,
    val port: Int,
    val username: String,
    val password: String
) {
    companion object {
        fun parse(prefix: String, properties: Properties): DatabaseConfig {
            val portPrefix = "$prefix.${DatabaseConfig::port.name}"

            return DatabaseConfig(
                properties.getPropertyOrThrow(prefix, DatabaseConfig::host),
                try {
                    properties.getPropertyOrThrow(prefix, DatabaseConfig::port)
                        .toInt()
                } catch (e: NumberFormatException) {
                    throw IllegalStateException(
                        "Database port property [ $portPrefix ] must be an integer, was [ ${
                            properties.getPropertyOrThrow(
                                prefix,
                                DatabaseConfig::port
                            )
                        } ]"
                    )
                },
                properties.getPropertyOrThrow(prefix, DatabaseConfig::username),
                properties.getPropertyOrThrow(prefix, DatabaseConfig::password),
            )
        }
    }
}