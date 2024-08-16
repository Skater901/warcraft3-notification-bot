package au.com.skater901.wc3connect.core.dao.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.locator.ClasspathSqlLocator
import org.jdbi.v3.core.statement.Query
import org.jdbi.v3.core.statement.Update

internal val sqlLocator: ClasspathSqlLocator by lazy {
    ClasspathSqlLocator.create()
}

internal fun Handle.queryFromFile(queryFileName: String): Query = createQuery(sqlLocator.getResource(queryFileName))

internal fun Handle.updateFromFile(updateFileName: String): Update = createUpdate(sqlLocator.getResource(updateFileName))

internal fun <T> Jdbi.wHandle(block: (Handle) -> T): T = withHandle<T, Exception>(block)

internal fun Jdbi.usingHandle(block: (Handle) -> Unit) {
    useHandle<Exception>(block)
}