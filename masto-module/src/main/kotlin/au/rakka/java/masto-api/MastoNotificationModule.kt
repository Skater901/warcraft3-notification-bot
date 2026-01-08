package au.rakka.java.mastoapi

// Required imports that make sense
import au.com.skater901.wc3.api.NotificationModule
// Required imports for some reason
import au.com.skater901.wc3.api.core.service.WC3GameNotificationService
import au.rakka.java.`masto-api`.MastodonModule
import com.google.inject.Injector
// Maybe required imports
import kotlin.reflect.KClass

import org.slf4j.LoggerFactory

public class MastoNotificationModule : NotificationModule<MastoConfig>
{
	override val moduleName: String = "mastodon"
	override val configClass: KClass<MastoConfig> = MastoConfig::class
    override val annotation: KClass<out Annotation> = MastodonModule::class
    private val logger = LoggerFactory.getLogger(MastoNotificationModule::class.java)
	public override fun initializeNotificationHandlers(config:MastoConfig, injector:Injector, wc3GameNotificationService:WC3GameNotificationService)
	{
		logger.debug("xyzzy")
	}
	override val gameNotifier: KClass<MastoNotifier> = MastoNotifier::class
	override val scheduledTask: KClass<MastoReplyGuy> = MastoReplyGuy::class
}