package au.com.skater901.wc3.api.annotation

import com.google.inject.BindingAnnotation

/**
 * This annotation can be used to configure a [jakarta.ws.rs.client.Client] for injection. Use this annotation on a
 * constructor parameter of type [jakarta.ws.rs.client.Client].
 */
@BindingAnnotation
public annotation class ClientFor(public val value: String)
