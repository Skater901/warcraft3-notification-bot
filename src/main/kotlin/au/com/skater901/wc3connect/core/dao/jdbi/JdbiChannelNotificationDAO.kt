package au.com.skater901.wc3connect.core.dao.jdbi

import au.com.skater901.wc3connect.core.dao.ChannelNotificationDAO
import au.com.skater901.wc3connect.core.domain.ChannelNotification
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi

internal class JdbiChannelNotificationDAO @Inject constructor(
    private val jdbi: Jdbi
) : ChannelNotificationDAO {
    private val saveWork = databaseUnitOfWork(::save)
    override suspend fun save(channelNotification: ChannelNotification) {
        saveWork {
            jdbi.usingHandle {
                it.updateFromFile("sql/save.sql")
                    .bind("channelId", channelNotification.id)
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
                        rs.getString("channel_id"),
                        Regex(rs.getString("regex_pattern")),
                        "discord" // TODO fix
                    )
                }
                .list()
        }
    }

    private val deleteWork = databaseUnitOfWork(::delete)
    override suspend fun delete(channelId: String) {
        deleteWork {
            jdbi.usingHandle {
                it.updateFromFile("sql/delete.sql")
                    .bind("channelId", channelId)
                    .execute()
            }
        }
    }
}