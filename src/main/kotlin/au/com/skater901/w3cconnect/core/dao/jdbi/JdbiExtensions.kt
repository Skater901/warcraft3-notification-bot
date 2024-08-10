package au.com.skater901.w3cconnect.core.dao.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.locator.ClasspathSqlLocator
import org.jdbi.v3.core.statement.Query
import org.jdbi.v3.core.statement.Update

val sqlLocator: ClasspathSqlLocator by lazy {
    ClasspathSqlLocator.create()
}

fun Handle.queryFromFile(queryFileName: String): Query = createQuery(sqlLocator.getResource(queryFileName))

fun Handle.updateFromFile(updateFileName: String): Update = createUpdate(sqlLocator.getResource(updateFileName))

fun <T> Jdbi.wHandle(block: (Handle) -> T): T = withHandle<T, Exception>(block)

fun Jdbi.usingHandle(block: (Handle) -> Unit) {
    useHandle<Exception>(block)
}