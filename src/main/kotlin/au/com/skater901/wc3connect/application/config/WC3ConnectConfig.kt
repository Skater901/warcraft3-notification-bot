package au.com.skater901.wc3connect.application.config

import au.com.skater901.wc3connect.application.annotation.ConfigClass
import java.net.URI

@ConfigClass("wc3connect")
internal class WC3ConnectConfig(val url: URI = URI("https://host.entgaming.net/allgames"))