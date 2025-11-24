package au.com.skater901.wc3.utilities.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.future.await
import kotlinx.coroutines.yield
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

public suspend fun <T> Future<T>.await(): T {
    if (this is CompletableFuture<T>) {
        return await()
    }

    while (!isDone) {
        yield()
    }

    when (state()) {
        Future.State.SUCCESS -> return resultNow()
        Future.State.FAILED -> throw exceptionNow()
        Future.State.CANCELLED -> throw CancellationException()
        Future.State.RUNNING -> throw IllegalStateException("Future should be done but state is RUNNING.")
    }
}