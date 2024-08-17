package au.com.skater901.wc3connect.core.dao.jdbi

import au.com.skater901.wc3connect.application.UnitOfWork
import au.com.skater901.wc3connect.application.defaultUnitOfWork
import kotlinx.coroutines.Dispatchers
import kotlin.reflect.KCallable

internal inline fun <reified T : Any> T.databaseUnitOfWork(function: KCallable<*>): UnitOfWork =
    databaseUnitOfWork(function.name)

internal inline fun <reified T : Any> T.databaseUnitOfWork(name: String): UnitOfWork = defaultUnitOfWork(name)
    .withDispatcher(Dispatchers.IO)