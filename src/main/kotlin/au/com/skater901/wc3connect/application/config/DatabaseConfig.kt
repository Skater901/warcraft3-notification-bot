package au.com.skater901.wc3connect.application.config

internal class DatabaseConfig(
    val host: String = "localhost",
    val port: Int = 3306,
    val username: String,
    val password: String
)