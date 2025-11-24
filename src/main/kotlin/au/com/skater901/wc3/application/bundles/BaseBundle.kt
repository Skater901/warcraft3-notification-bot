package au.com.skater901.wc3.application.bundles

import au.com.skater901.wc3.WC3NotificationBotConfiguration
import dev.misfitlabs.kotlinguice4.KotlinModule
import io.dropwizard.core.ConfiguredBundle

internal interface BaseBundle : ConfiguredBundle<WC3NotificationBotConfiguration> {
    val module: KotlinModule?
        get() = null
}