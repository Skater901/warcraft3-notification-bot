package au.com.skater901.wc3.discord.annotations

import com.google.inject.BindingAnnotation

@Target(AnnotationTarget.VALUE_PARAMETER)
@BindingAnnotation
internal annotation class DiscordModule
