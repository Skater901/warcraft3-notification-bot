package au.com.skater901.wc3connect.core.dao.jdbi

import au.com.skater901.wc3connect.core.dao.NotificationDAO
import au.com.skater901.wc3connect.core.domain.WC3GameNotification
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi

internal class JdbiNotificationDAO @Inject constructor(
    private val jdbi: Jdbi
) : NotificationDAO {
    private val saveWork = databaseUnitOfWork(::save)
    override suspend fun save(wc3GameNotification: WC3GameNotification) {
        saveWork {
            jdbi.usingHandle {
                it.updateFromFile("sql/save.sql")
                    .bind("id", wc3GameNotification.id)
                    .bind("type", wc3GameNotification.type)
                    .bind("regexPattern", wc3GameNotification.mapNameRegexPattern.pattern)
                    .execute()
            }
        }
    }

    private val findWork = databaseUnitOfWork(::find)
    override suspend fun find(): List<WC3GameNotification> = findWork {
        jdbi.wHandle {
            it.queryFromFile("sql/find.sql")
                .map { rs, _ ->
                    WC3GameNotification(
                        rs.getString("id"),
                        rs.getString("type"),
                        Regex(rs.getString("regex_pattern"))
                    )
                }
                .list()
        }
    }

    private val deleteWork = databaseUnitOfWork(::delete)
    override suspend fun delete(id: String): Boolean = deleteWork {
        jdbi.wHandle {
            it.updateFromFile("sql/delete.sql")
                .bind("id", id)
                .execute() == 1
        }
    }
}