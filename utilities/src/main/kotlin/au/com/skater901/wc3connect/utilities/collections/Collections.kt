package au.com.skater901.wc3connect.utilities.collections

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList

public suspend fun <T> Collection<T>.forEachAsync(action: suspend (T) -> Unit) {
    mapAsync(action)
}

public suspend fun <T, R> Collection<T>.mapAsync(transform: suspend (T) -> R): List<R> = asFlow()
    .flatMapMerge { flowOf(transform(it)) }
    .toList()

public suspend fun <K, V> Map<K, V>.forEachAsync(action: suspend (Map.Entry<K, V>) -> Unit) {
    entries.forEachAsync(action)
}

public fun <T> Collection<T>.ifNotEmpty(action: (Collection<T>) -> Unit) {
    if (isNotEmpty()) action(this)
}