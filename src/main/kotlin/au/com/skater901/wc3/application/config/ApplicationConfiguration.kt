package au.com.skater901.wc3.application.config

import au.com.skater901.wc3.application.annotation.ConfigClass

@ConfigClass("application")
internal class ApplicationConfiguration(val refreshInterval: Long = 10_000)