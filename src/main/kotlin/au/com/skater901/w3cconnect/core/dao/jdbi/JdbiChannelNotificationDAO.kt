package au.com.skater901.w3cconnect.core.dao.jdbi

import au.com.skater901.w3cconnect.core.domain.ChannelNotification
import au.com.skater901.w3cconnect.core.dao.ChannelNotificationDAO
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi

class JdbiChannelNotificationDAO @Inject constructor(
    private val jdbi: Jdbi
) : ChannelNotificationDAO {
    override suspend fun save(channelNotification: ChannelNotification) {
        jdbi.usingHandle {
            it.updateFromFile("sql/save.sql")
                .bind("channelId", channelNotification.channelId)
                .bind("regexPattern", channelNotification.mapRegex.pattern)
                .execute()
        }
    }

    override suspend fun find(): List<ChannelNotification> = jdbi.wHandle {
        it.queryFromFile("sql/find.sql")
            .map { rs, _ ->
                ChannelNotification(
                    rs.getLong("channel_id"),
                    Regex(rs.getString("regex_pattern"))
                )
            }
            .list()
    }

    override suspend fun delete(channelId: Long) {
        jdbi.usingHandle {
            it.updateFromFile("sql/delete.sql")
                .bind("channelId", channelId)
                .execute()
        }
    }
}