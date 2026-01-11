package au.com.skater901.wc3.extras.annotation

import com.google.inject.BindingAnnotation

/**
 * This annotation can be used to bind and configure an [java.util.concurrent.ExecutorService] for injection.
 *
 * [value] This is the name of the configuration property that specifies how many threads your thread pool should have.
 * The format should be the name of the property minus `.threads`. For example, if you have a Facebook module that
 * requires a thread pool, you would create a configuration property called `facebook.executor.threads=10`. When you
 * inject that thread pool, you would use `@ExecutorFor("facebook.executor")`.
 */

@BindingAnnotation
public annotation class ExecutorFor(val value: String)
