package au.com.skater901.wc3connect.api.core.domain

/**
 * A WC3Connect hosted game. Represents the current state of the game in lobby.
 */
public interface Game {
    /**
     * The unique identifier of the WC3Connect hosted game.
     */
    public val id: Int

    /**
     * The name of the WC3Connect hosted game.
     */
    public val name: String

    /**
     * The Warcraft 3 map that this game is hosting.
     */
    public val map: String

    /**
     * The WC3Connect user who hosted this game. For games automatically hosted by WC3Connect, this value will be an empty String.
     */
    public val host: String

    /**
     * The number of players currently in the lobby.
     */
    public val currentPlayers: Int

    /**
     * The maximum number of players the hosted map supports.
     */
    public val maxPlayers: Int

    /**
     * The number of minutes, as a String, since the game was hosted.
     */
    public val uptime: String
}