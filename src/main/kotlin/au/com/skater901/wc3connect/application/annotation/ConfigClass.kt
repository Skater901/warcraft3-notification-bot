package au.com.skater901.wc3connect.application.annotation

@Target(AnnotationTarget.CLASS)
@Retention
internal annotation class ConfigClass(val prefix: String = "")