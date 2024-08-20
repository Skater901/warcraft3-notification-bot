package au.com.skater901.wc3connect.core.domain

internal data class ChannelNotification(
    val id: String,
    val mapRegex: Regex,
    val type: String
)