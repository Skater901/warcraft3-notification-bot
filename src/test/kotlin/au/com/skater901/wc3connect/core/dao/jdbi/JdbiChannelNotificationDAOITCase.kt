package au.com.skater901.wc3connect.core.dao.jdbi

import au.com.skater901.wc3connect.core.domain.ChannelNotification
import au.com.skater901.wc3connect.utils.MySQLExtension
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
        val channelNotification = ChannelNotification("12345", Regex("any"), "mymodule")

        val channelNotificationDAO = JdbiChannelNotificationDAO(jdbi)

        var notifications = runBlocking {
            channelNotificationDAO.save(channelNotification)

            channelNotificationDAO.find()
        }

        assertThat(notifications).hasSize(1)
            .anyMatch {
                it.id == channelNotification.id &&
                        it.mapRegex.pattern == channelNotification.mapRegex.pattern
            }

        val newChannelNotification = channelNotification.copy(mapRegex = Regex("different"))

        notifications = runBlocking {
            channelNotificationDAO.save(newChannelNotification)

            channelNotificationDAO.find()
        }

        assertThat(notifications).hasSize(1)
            .anyMatch {
                it.id == newChannelNotification.id &&
                        it.mapRegex.pattern == newChannelNotification.mapRegex.pattern
            }

        notifications = runBlocking {
            channelNotificationDAO.delete(newChannelNotification.id)

            channelNotificationDAO.find()
        }

        assertThat(notifications).isEmpty()
    }

    @Test
    fun `should save multiple notifications`(jdbi: Jdbi) {
        val channelNotification1 = ChannelNotification("12345", Regex("patterna"), "mymodule")
        val channelNotification2 = ChannelNotification("22222", Regex("patternb"), "mymodule")

        val channelNotificationDAO = JdbiChannelNotificationDAO(jdbi)

        val notifications = runBlocking {
            channelNotificationDAO.save(channelNotification1)
            channelNotificationDAO.save(channelNotification2)

            channelNotificationDAO.find()
        }

        assertThat(notifications).hasSize(2)
            .anyMatch {
                it.id == channelNotification1.id &&
                        it.mapRegex.pattern == channelNotification1.mapRegex.pattern
            }
            .anyMatch {
                it.id == channelNotification2.id &&
                        it.mapRegex.pattern == channelNotification2.mapRegex.pattern
            }
    }
}