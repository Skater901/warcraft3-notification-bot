package au.com.skater901.wc3.application.healthcheck

import com.codahale.metrics.health.HealthCheck

internal abstract class GameSourceHealthCheck : HealthCheck() {
    private var result: Result = Result.healthy()

    override fun check(): Result = result

    fun healthy() {
        result = Result.healthy()
    }

    fun noGames() {
        result = Result.unhealthy("No games found.")
    }

    fun exception(exception: Exception) {
        result = Result.unhealthy(exception)
    }
}