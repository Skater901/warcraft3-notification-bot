package au.com.skater901.wc3.core.domain

internal data class WC3GameNotification(
    val id: String,
    val type: String,
    val mapNameRegexPattern: Regex
)