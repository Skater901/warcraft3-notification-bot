package au.com.skater901.wc3.discord.core.dao.jdbi

import au.com.skater901.wc3.test.utilities.MariaDBConnection
import au.com.skater901.wc3.test.utilities.MariaDBExtension
import au.com.skater901.wc3.test.utilities.MySQLConnection
import au.com.skater901.wc3.test.utilities.MySQLDBExtension
import au.com.skater901.wc3.utilities.database.usingHandle
import au.com.skater901.wc3.utilities.database.wHandle
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MariaDBExtension::class, MySQLDBExtension::class)
class JdbiRoleNotificationDAOITCase {
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
    fun `should save role to be notified`() {
        runDatabaseTest { jdbi ->
            val channelId = "012345"
            val roleId = "11111"
            runBlocking { JdbiRoleNotificationDAO(jdbi).save(channelId, roleId) }

            val savedRoleNotification = jdbi.wHandle {
                it.createQuery("SELECT * FROM discord_notification_role")
                    .mapToMap()
                    .first()
            }

            assertThat(savedRoleNotification["channel_id"]).isEqualTo(channelId)
            assertThat(savedRoleNotification["role_id"]).isEqualTo(roleId)
        }
    }

    @Test
    fun `should find role to be notified`() {
        runDatabaseTest { jdbi ->
            val channelId = "012345"
            val roleId = "11111"

            jdbi.usingHandle {
                it.createUpdate("INSERT INTO discord_notification_role (channel_id, role_id) VALUES (:channelId, :roleId)")
                    .bind("channelId", channelId)
                    .bind("roleId", roleId)
                    .execute()
            }

            val savedRoleNotification = runBlocking {
                JdbiRoleNotificationDAO(jdbi).find(channelId)
            }

            assertThat(savedRoleNotification).isEqualTo(roleId)
        }
    }

    @Test
    fun `should not find role to be notified`() {
        runDatabaseTest { jdbi ->
            val channelId = "012345"
            val savedRoleNotification = runBlocking {
                JdbiRoleNotificationDAO(jdbi).find(channelId)
            }

            assertThat(savedRoleNotification).isNull()
        }
    }

    @Test
    fun `should delete role to be notified`() {
        runDatabaseTest { jdbi ->
            val channelId = "012345"
            val roleId = "11111"

            jdbi.usingHandle {
                it.createUpdate("INSERT INTO discord_notification_role (channel_id, role_id) VALUES (:channelId, :roleId)")
                    .bind("channelId", channelId)
                    .bind("roleId", roleId)
                    .execute()
            }

            runBlocking {
                JdbiRoleNotificationDAO(jdbi).delete(channelId)
            }

            val notifications = jdbi.wHandle {
                it.createQuery("SELECT * FROM discord_notification_role")
                    .mapToMap()
                    .list()
                    .size
            }

            assertThat(notifications).isEqualTo(0)
        }
    }
}