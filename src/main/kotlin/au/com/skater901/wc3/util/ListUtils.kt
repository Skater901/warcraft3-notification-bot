package au.com.skater901.wc3.util

internal fun <T> List<T>.randomize(): List<T> {
    val itemsToRandomize = toMutableList()

    val results = mutableListOf<T>()

    while (itemsToRandomize.isNotEmpty()) {
        val item = if (itemsToRandomize.size == 1) {
            itemsToRandomize.first()
        } else {
            itemsToRandomize.random()
        }

        itemsToRandomize.remove(item)

        results += item
    }

    return results.toList()
}