package au.com.skater901.wc3connect.api.core.domain

/**
 * The area in the world where the server hosting this game is located.
 */
public enum class Region {
    EU,
    US,
    Asia,

    /**
     * Fallback option in case of unrecognised region. Should never actually be used, just a failsafe.
     */
    Unknown
}