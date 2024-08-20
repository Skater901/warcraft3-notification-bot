package au.com.skater901.wc3connect.core.service

import au.com.skater901.wc3connect.api.core.domain.exceptions.InvalidRegexPatternException
import au.com.skater901.wc3connect.core.dao.NotificationDAO
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify

class WC3GameNotificationServiceImplTest {
    @Test
    fun `should throw InvalidRegexPatternException if regex is invalid`() {
        assertThatThrownBy {
            runBlocking { WC3GameNotificationServiceImpl(mock(), "hello").createNotification("12345", "\\\\\\\\\\") }
        }
            .isInstanceOf(InvalidRegexPatternException::class.java)
    }

    @Test
    fun `should save new channel notification`() {
        val notificationDAO = mock<NotificationDAO>()

        runBlocking {
            WC3GameNotificationServiceImpl(notificationDAO, "module1").createNotification("12345", "DotA")
        }

        verify(notificationDAO) {
            1 * { runBlocking { save(argThat { id == "12345" && type == "module1" && mapNameRegexPattern.pattern == "DotA" }) } }
        }
    }
}