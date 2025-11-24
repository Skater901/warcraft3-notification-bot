package au.com.skater901.wc3.application.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
internal class DatabaseConfig {
    @JsonProperty
    val type: DatabaseType = DatabaseType.MySQL

    @JsonProperty
    @NotBlank
    val host: String = "localhost"

    @JsonProperty
    @Min(1)
    @Max(65535)
    val port: Int = 3306

    @JsonProperty
    @NotBlank
    val schema: String = "wc3_bot"

    @JsonProperty
    @NotBlank
    val username: String? = null

    @JsonProperty
    @NotBlank
    val password: String? = null

    enum class DatabaseType {
        MySQL,
        MariaDB
    }
}