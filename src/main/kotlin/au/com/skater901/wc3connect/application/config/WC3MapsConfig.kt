package au.com.skater901.wc3connect.application.config

import au.com.skater901.wc3connect.application.annotation.ConfigClass
import java.net.URI

@ConfigClass("wc3maps")
internal class WC3MapsConfig(val url: URI = URI("https://wc3maps.com/api/lobbies"))