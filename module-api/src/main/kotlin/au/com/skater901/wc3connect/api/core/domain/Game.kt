package au.com.skater901.wc3connect.api.core.domain

import java.time.Instant

/**
 * A Warcraft 3 hosted game. Represents the current state of the game in lobby.
 */
public interface Game {
    /**
     * The unique identifier of the Warcraft 3 hosted game.
     */
    public val id: Int

    /**
     * The name of the Warcraft 3 hosted game.
     */
    public val name: String

    /**
     * The Warcraft 3 map that this game is hosting.
     */
    public val map: String

    /**
     * The user who hosted this game. For games automatically hosted by WC3Connect, this value will be an empty String.
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
     * The timestamp when this game was created.
     */
    public val created: Instant

    /**
     * Where in the world the game server is located.
     */
    public val region: Region

    /**
     * Where this game is hosted, Battle.Net, WC3Connect, etc.
     */
    public val gameSource: GameSource
}