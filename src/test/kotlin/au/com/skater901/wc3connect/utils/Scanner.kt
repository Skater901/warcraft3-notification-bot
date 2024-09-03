package au.com.skater901.wc3connect.utils

import au.com.skater901.wc3connect.WC3ConnectNotificationBot
import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult

fun <T> scanResult(useScanner: (ScanResult) -> T): T =
    ClassGraph().acceptPackages(WC3ConnectNotificationBot::class.java.packageName)
        .enableAnnotationInfo()
        .scan()
        .use(useScanner)