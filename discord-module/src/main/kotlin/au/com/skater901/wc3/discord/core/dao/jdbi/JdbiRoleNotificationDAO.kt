package au.com.skater901.wc3.discord.core.dao.jdbi

import au.com.skater901.wc3.discord.core.dao.RoleNotificationDAO
import au.com.skater901.wc3.utilities.database.*
import jakarta.inject.Inject
import org.jdbi.v3.core.Jdbi
import kotlin.jvm.optionals.getOrNull

internal class JdbiRoleNotificationDAO @Inject constructor(
    private val jdbi: Jdbi
) : RoleNotificationDAO {
    private val saveWork = databaseUnitOfWork(::save)
    override suspend fun save(channelId: String, roleId: String) {
        saveWork {
            jdbi.usingHandle {
                it.updateFromFile("sql/save.sql")
                    .bind("channelId", channelId)
                    .bind("roleId", roleId)
                    .execute()
            }
        }
    }

    private val findWork = databaseUnitOfWork(::find)
    override suspend fun find(channelId: String): String? = findWork {
        jdbi.wHandle {
            it.queryFromFile("sql/find.sql")
                .bind("channelId", channelId)
                .mapTo(String::class.java)
                .findFirst()
                .getOrNull()
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