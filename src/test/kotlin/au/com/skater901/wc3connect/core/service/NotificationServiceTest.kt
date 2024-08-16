package au.com.skater901.wc3connect.core.service

import au.com.skater901.wc3connect.core.dao.ChannelNotificationDAO
import au.com.skater901.wc3connect.core.domain.exceptions.InvalidRegexPatternException
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify

class NotificationServiceTest {
    @Test
    fun `should throw InvalidRegexPatternException if regex is invalid`() {
        assertThatThrownBy {
            runBlocking { NotificationService(mock()).createNotification(12345, "\\\\\\\\\\") }
        }
            .isInstanceOf(InvalidRegexPatternException::class.java)
    }

    @Test
    fun `should save new channel notification`() {
        val channelNotificationDAO = mock<ChannelNotificationDAO>()

        runBlocking {
            NotificationService(channelNotificationDAO).createNotification(12345, "DotA")
        }

        verify(channelNotificationDAO) {
            1 * { runBlocking { save(argThat { channelId == 12345L && mapRegex.pattern == "DotA" }) } }
        }
    }
}