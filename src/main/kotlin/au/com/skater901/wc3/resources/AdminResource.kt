package au.com.skater901.wc3.resources

import au.com.skater901.wc3.api.core.service.AdminMessageNotifier
import au.com.skater901.wc3.core.dao.NotificationDAO
import au.com.skater901.wc3.utilities.collections.forEachAsync
import au.com.skater901.wc3.utilities.metricsWork
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.MediaType
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@Path("/admin")
internal class AdminResource @Inject constructor(
    private val notificationDAO: NotificationDAO,
    private val adminMessageNotifiers: Map<String, @JvmSuppressWildcards AdminMessageNotifier>
) {
    companion object {
        private val logger = LoggerFactory.getLogger(AdminResource::class.java)
    }

    private val sendAdminMessageWork = metricsWork(::sendAdminMessage)

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    fun sendAdminMessage(message: String) {
        runBlocking {
            sendAdminMessageWork {
                val notifications = notificationDAO.find()
                    .groupBy { it.type }
                    .mapValues { (_, notifications) -> notifications.map { it.id } }

                adminMessageNotifiers.forEachAsync { (moduleName, notifier) ->
                    try {
                        notifier.sendAdminMessage(message, notifications[moduleName] ?: emptyList())
                    } catch (e: Exception) {
                        logger.error("Exception when sending admin message ", e)
                    }
                }
            }
        }
    }
}