package au.com.skater901.wc3.application.config

import au.com.skater901.wc3.application.annotation.ConfigClass

@ConfigClass("database")
internal class DatabaseConfig(
    val type: DatabaseType = DatabaseType.MySQL,
    val host: String = "localhost",
    val port: Int = 3306,
    val username: String,
    val password: String
) {
    enum class DatabaseType {
        MySQL,
        MariaDB
    }
}