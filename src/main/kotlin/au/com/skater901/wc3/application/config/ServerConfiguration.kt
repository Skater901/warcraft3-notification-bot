package au.com.skater901.wc3.application.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
internal class ServerConfiguration {
    @JsonProperty
    @NotNull
    @Min(1)
    val port: Int? = null

    @JsonProperty
    @NotNull
    @Min(1)
    val adminPort: Int? = null

    @JsonProperty
    @NotNull
    @Min(1)
    val requestThreads: Int? = null

    @JsonProperty
    @NotNull
    @Min(10)
    val maxQueuedRequests: Int? = null

    @JsonProperty
    @NotNull
    @Valid
    val logging: LogConfiguration? = null
}