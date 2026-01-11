package au.com.skater901.wc3.application.module

import au.com.skater901.wc3.application.provider.ExecutorProvider
import au.com.skater901.wc3.extras.annotation.ExecutorFor
import com.google.inject.Key
import com.google.inject.name.Names.named
import dev.misfitlabs.kotlinguice4.KotlinModule
import io.dropwizard.core.setup.Environment
import io.github.classgraph.ClassRefTypeSignature
import io.github.classgraph.ScanResult
import jakarta.inject.Inject
import java.util.*
import java.util.concurrent.ExecutorService

internal class ExecutorsModule(scanResult: ScanResult) : KotlinModule() {
    private val executorsAnnotations = scanResult.allClasses
        .asSequence()
        .flatMap { it.constructorInfo + it.methodInfo }
        .filter { it.hasAnnotation(Inject::class.java) }
        .flatMap { it.parameterInfo.asSequence() }
        .filter {
            it.hasAnnotation(ExecutorFor::class.java) &&
                    (it.typeDescriptor as? ClassRefTypeSignature)?.fullyQualifiedClassName == ExecutorService::class.java.name
        }
        .map { it.getAnnotationInfo(ExecutorFor::class.java).loadClassAndInstantiate() }
        .filterIsInstance<ExecutorFor>()
        .toList()

    override fun configure() {
        val environment = getProvider<Environment>()
        val configProperties = getProvider(Key.get(Properties::class.java, named("configProperties")))

        executorsAnnotations.forEach {
            bind<ExecutorService>().annotatedWith(it)
                .toProvider(ExecutorProvider(environment, configProperties, it))
        }
    }
}