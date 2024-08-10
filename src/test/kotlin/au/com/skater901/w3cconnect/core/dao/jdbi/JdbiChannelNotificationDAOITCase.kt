package au.com.skater901.w3cconnect.core.dao.jdbi

import au.com.skater901.w3cconnect.core.ChannelNotification
import au.com.skater901.w3cconnect.utils.MySQLExtension
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MySQLExtension::class)
class JdbiChannelNotificationDAOITCase {
    @AfterEach
    fun tearDown(jdbi: Jdbi) {
        jdbi.usingHandle {
            it.createUpdate("DELETE FROM channel_notification;")
                .execute()
        }
    }

    @Test
    fun `should save, retrieve, update, and delete`(jdbi: Jdbi) {
        val channelNotification = ChannelNotification(12345, Regex("any"))

        val channelNotificationDAO = JdbiChannelNotificationDAO(jdbi)

        var notifications = runBlocking {
            channelNotificationDAO.save(channelNotification)

            channelNotificationDAO.find()
        }

        assertThat(notifications).hasSize(1)
            .anyMatch {
                it.channelId == channelNotification.channelId &&
                        it.mapRegex.pattern == channelNotification.mapRegex.pattern
            }

        val newChannelNotification = channelNotification.copy(mapRegex = Regex("different"))

        notifications = runBlocking {
            channelNotificationDAO.save(newChannelNotification)

            channelNotificationDAO.find()
        }

        assertThat(notifications).hasSize(1)
            .anyMatch {
                it.channelId == newChannelNotification.channelId &&
                        it.mapRegex.pattern == newChannelNotification.mapRegex.pattern
            }

        notifications = runBlocking {
            channelNotificationDAO.delete(newChannelNotification.channelId)

            channelNotificationDAO.find()
        }

        assertThat(notifications).isEmpty()
    }

    @Test
    fun `should save multiple notifications`(jdbi: Jdbi) {
        val channelNotification1 = ChannelNotification(12345, Regex("patterna"))
        val channelNotification2 = ChannelNotification(22222, Regex("patternb"))

        val channelNotificationDAO = JdbiChannelNotificationDAO(jdbi)

        val notifications = runBlocking {
            channelNotificationDAO.save(channelNotification1)
            channelNotificationDAO.save(channelNotification2)

            channelNotificationDAO.find()
        }

        assertThat(notifications).hasSize(2)
            .anyMatch {
                it.channelId == channelNotification1.channelId &&
                        it.mapRegex.pattern == channelNotification1.mapRegex.pattern
            }
            .anyMatch {
                it.channelId == channelNotification2.channelId &&
                        it.mapRegex.pattern == channelNotification2.mapRegex.pattern
            }
    }
}