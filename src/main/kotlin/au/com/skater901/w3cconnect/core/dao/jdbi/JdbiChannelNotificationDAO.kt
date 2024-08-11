package au.com.skater901.w3cconnect.core.dao.jdbi

import au.com.skater901.w3cconnect.core.dao.ChannelNotificationDAO
import au.com.skater901.w3cconnect.core.domain.ChannelNotification
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi

class JdbiChannelNotificationDAO @Inject constructor(
    private val jdbi: Jdbi
) : ChannelNotificationDAO {
    private val saveWork = databaseUnitOfWork(::save)
    override suspend fun save(channelNotification: ChannelNotification) {
        saveWork {
            jdbi.usingHandle {
                it.updateFromFile("sql/save.sql")
                    .bind("channelId", channelNotification.channelId)
                    .bind("regexPattern", channelNotification.mapRegex.pattern)
                    .execute()
            }
        }
    }

    private val findWork = databaseUnitOfWork(::find)
    override suspend fun find(): List<ChannelNotification> = findWork {
        jdbi.wHandle {
            it.queryFromFile("sql/find.sql")
                .map { rs, _ ->
                    ChannelNotification(
                        rs.getLong("channel_id"),
                        Regex(rs.getString("regex_pattern"))
                    )
                }
                .list()
        }
    }

    private val deleteWork = databaseUnitOfWork(::delete)
    override suspend fun delete(channelId: Long) {
        deleteWork {
            jdbi.usingHandle {
                it.updateFromFile("sql/delete.sql")
                    .bind("channelId", channelId)
                    .execute()
            }
        }
    }
}