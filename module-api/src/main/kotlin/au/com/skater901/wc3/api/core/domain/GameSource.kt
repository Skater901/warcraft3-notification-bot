package au.com.skater901.wc3.api.core.domain

/**
 * Indicates where this game info comes from
 */
public enum class GameSource(public val gameNetwork: GameNetwork) {
    WC3Connect(GameNetwork.WC3Connect),
    WC3Maps(GameNetwork.BattleNet),
    WC3Stats(GameNetwork.BattleNet)
}