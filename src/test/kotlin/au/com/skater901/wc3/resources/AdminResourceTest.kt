package au.com.skater901.wc3.resources

import au.com.skater901.wc3.api.core.service.AdminMessageNotifier
import au.com.skater901.wc3.core.dao.NotificationDAO
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class AdminResourceTest {
    @Test
    fun `should handle exception from one notifier and not let it affect other notifiers`() {
        val brokenNotifier = mock<AdminMessageNotifier> {
            on { sendAdminMessage(any(), any()) } doThrow RuntimeException("Kaboom!")
        }
        var happyNotifierSucceeded = false
        val happyNotifier = mock<AdminMessageNotifier> {
            on { sendAdminMessage(any(), any()) } doSuspendableAnswer {
                delay(100)

                happyNotifierSucceeded = true
            }
        }
        val notificationDAO = mock<NotificationDAO> {
            on { find() } doReturn emptyList()
        }

        AdminResource(notificationDAO, mapOf("module1" to brokenNotifier, "module2" to happyNotifier)).sendAdminMessage(
            "hello"
        )

        assertThat(happyNotifierSucceeded).isTrue()
    }
}