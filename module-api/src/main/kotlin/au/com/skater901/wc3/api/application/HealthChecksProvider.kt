package au.com.skater901.wc3.api.application

import com.codahale.metrics.health.HealthCheck

/**
 * This provider will give you all the registered health check results along with their registered name. You can use
 * this to report the state of the bot to users.
 */
public interface HealthChecksProvider {
    /**
     * Get the health checks.
     *
     * @return The health checks grouped by name and result.
     */
    public fun healthChecks(): Map<String, HealthCheck.Result>
}