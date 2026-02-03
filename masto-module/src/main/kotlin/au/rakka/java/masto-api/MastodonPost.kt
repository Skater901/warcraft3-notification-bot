package au.rakka.java.`masto-api`

import com.fasterxml.jackson.annotation.JsonProperty

internal data class MastodonPost(
    val id: String,
    val type: String,
    val status: MastodonStatus
)

internal data class MastodonStatus(
    val id: String,
    val pleroma: Pleroma?,
    val content: String
)

internal data class Pleroma(
    val content: Content
)

internal data class Content(
    @param:JsonProperty("text/plain")
    val text: String
)