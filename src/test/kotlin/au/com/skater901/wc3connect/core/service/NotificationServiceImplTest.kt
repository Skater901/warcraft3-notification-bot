package au.com.skater901.wc3connect.core.service

import au.com.skater901.wc3connect.core.dao.ChannelNotificationDAO
import au.com.skater901.wc3connect.api.core.domain.exceptions.InvalidRegexPatternException
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify

class NotificationServiceImplTest {
    @Test
    fun `should throw InvalidRegexPatternException if regex is invalid`() {
        assertThatThrownBy {
            runBlocking { NotificationServiceImpl(mock()).createNotification("12345", "\\\\\\\\\\") }
        }
            .isInstanceOf(InvalidRegexPatternException::class.java)
    }

    @Test
    fun `should save new channel notification`() {
        val channelNotificationDAO = mock<ChannelNotificationDAO>()

        runBlocking {
            NotificationServiceImpl(channelNotificationDAO).createNotification("12345", "DotA")
        }

        verify(channelNotificationDAO) {
            1 * { runBlocking { save(argThat { id == "12345" && mapRegex.pattern == "DotA" }) } }
        }
    }
}