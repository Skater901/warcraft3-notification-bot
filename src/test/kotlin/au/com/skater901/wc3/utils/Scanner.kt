package au.com.skater901.wc3.utils

import au.com.skater901.wc3.WC3NotificationBot
import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult

fun <T> scanResult(useScanner: (ScanResult) -> T): T =
    ClassGraph().acceptPackages(WC3NotificationBot::class.java.packageName)
        .enableAnnotationInfo()
        .scan()
        .use(useScanner)