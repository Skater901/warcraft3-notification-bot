package au.com.skater901.wc3.core.dao.jdbi

import au.com.skater901.wc3.core.domain.WC3GameNotification
import au.com.skater901.wc3.utils.MariaDBConnection
import au.com.skater901.wc3.utils.MariaDBExtension
import au.com.skater901.wc3.utils.MySQLConnection
import au.com.skater901.wc3.utils.MySQLDBExtension
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MariaDBExtension::class)
@ExtendWith(MySQLDBExtension::class)
class JdbiNotificationDAOITCase {
    companion object {
        private lateinit var mariaDBJdbi: Jdbi
        private lateinit var mySQLJdbi: Jdbi

        @BeforeAll
        @JvmStatic
        fun setUp(@MariaDBConnection mariaDBJdbi: Jdbi, @MySQLConnection mySQLJdbi: Jdbi) {
            this.mariaDBJdbi = mariaDBJdbi
            this.mySQLJdbi = mySQLJdbi
        }

        fun runDatabaseTest(test: (Jdbi) -> Unit) {
            test(mariaDBJdbi)
            test(mySQLJdbi)
        }
    }

    @Test
    fun `should save, retrieve, update, and delete`() {
        runDatabaseTest { jdbi ->
            val wc3GameNotification = WC3GameNotification("12345", "mymodule", Regex("any"))

            val notificationDAO = JdbiNotificationDAO(jdbi)

            var notifications = runBlocking {
                notificationDAO.save(wc3GameNotification)

                notificationDAO.find()
            }

            assertThat(notifications).hasSize(1)
                .anyMatch {
                    it.id == wc3GameNotification.id &&
                            it.type == wc3GameNotification.type &&
                            it.mapNameRegexPattern.pattern == wc3GameNotification.mapNameRegexPattern.pattern
                }

            val newWC3Notification = wc3GameNotification.copy(mapNameRegexPattern = Regex("different"))

            notifications = runBlocking {
                notificationDAO.save(newWC3Notification)

                notificationDAO.find()
            }

            assertThat(notifications).hasSize(1)
                .anyMatch {
                    it.id == newWC3Notification.id &&
                            it.type == newWC3Notification.type &&
                            it.mapNameRegexPattern.pattern == newWC3Notification.mapNameRegexPattern.pattern
                }

            notifications = runBlocking {
                notificationDAO.delete(newWC3Notification.id)

                notificationDAO.find()
            }

            assertThat(notifications).isEmpty()
        }
    }

    @Test
    fun `should save multiple notifications`() {
        runDatabaseTest { jdbi ->
            val wc3GameNotification1 = WC3GameNotification("12345", "mymodule", Regex("patterna"))
            val wc3GameNotification2 = WC3GameNotification("22222", "mymodule", Regex("patternb"))
            val wc3GameNotification3 = WC3GameNotification("22222", "othermodule", Regex("patternb"))

            val notificationDAO = JdbiNotificationDAO(jdbi)

            val notifications = runBlocking {
                notificationDAO.save(wc3GameNotification1)
                notificationDAO.save(wc3GameNotification2)
                notificationDAO.save(wc3GameNotification3)

                notificationDAO.find()
            }

            assertThat(notifications).hasSize(3)
                .anyMatch {
                    it.id == wc3GameNotification1.id &&
                            it.type == wc3GameNotification1.type &&
                            it.mapNameRegexPattern.pattern == wc3GameNotification1.mapNameRegexPattern.pattern
                }
                .anyMatch {
                    it.id == wc3GameNotification2.id &&
                            it.type == wc3GameNotification2.type &&
                            it.mapNameRegexPattern.pattern == wc3GameNotification2.mapNameRegexPattern.pattern
                }
                .anyMatch {
                    it.id == wc3GameNotification3.id &&
                            it.type == wc3GameNotification3.type &&
                            it.mapNameRegexPattern.pattern == wc3GameNotification3.mapNameRegexPattern.pattern
                }
        }
    }
}