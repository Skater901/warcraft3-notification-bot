package au.com.skater901.wc3.utilities.database

import au.com.skater901.wc3.utilities.UnitOfWork
import au.com.skater901.wc3.utilities.defaultUnitOfWork
import kotlinx.coroutines.Dispatchers
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.locator.ClasspathSqlLocator
import org.jdbi.v3.core.statement.Query
import org.jdbi.v3.core.statement.Update
import kotlin.reflect.KCallable

public val sqlLocator: ClasspathSqlLocator by lazy {
    ClasspathSqlLocator.create()
}

public fun Handle.queryFromFile(queryFileName: String): Query = createQuery(sqlLocator.getResource(queryFileName))

public fun Handle.updateFromFile(updateFileName: String): Update =
    createUpdate(sqlLocator.getResource(updateFileName))

public fun <T> Jdbi.wHandle(block: (Handle) -> T): T = withHandle<T, Exception>(block)

public fun Jdbi.usingHandle(block: (Handle) -> Unit) {
    useHandle<Exception>(block)
}

public inline fun <reified T : Any> T.databaseUnitOfWork(function: KCallable<*>): UnitOfWork =
    databaseUnitOfWork(function.name)

public inline fun <reified T : Any> T.databaseUnitOfWork(name: String): UnitOfWork = defaultUnitOfWork(name)
    .withDispatcher(Dispatchers.IO)