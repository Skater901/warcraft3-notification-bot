package au.com.skater901.w3cconnect.core.dao.jdbi

import au.com.skater901.w3cconnect.application.UnitOfWork
import au.com.skater901.w3cconnect.application.defaultUnitOfWork
import kotlinx.coroutines.Dispatchers
import kotlin.reflect.KCallable

inline fun <reified T : Any> T.databaseUnitOfWork(function: KCallable<*>): UnitOfWork =
    databaseUnitOfWork(function.name)

inline fun <reified T : Any> T.databaseUnitOfWork(name: String): UnitOfWork = defaultUnitOfWork(name)
    .withDispatcher(Dispatchers.IO)