package au.com.skater901.wc3connect.application.config

import au.com.skater901.wc3connect.application.annotation.ConfigClass
import java.net.URI

@ConfigClass("wc3stats")
internal class WC3StatsConfig(val url: URI = URI("https://api.wc3stats.com/gamelist"))