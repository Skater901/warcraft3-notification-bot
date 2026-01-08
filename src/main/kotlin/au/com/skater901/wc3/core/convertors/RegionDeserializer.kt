package au.com.skater901.wc3.core.convertors

import au.com.skater901.wc3.api.core.domain.Region
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

internal class RegionDeserializer : JsonDeserializer<Region>() {
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): Region =
        when (parser.valueAsString) {
            "eu" -> Region.EU
            "usw" -> Region.US
            "kr" -> Region.Asia
            else -> Region.Unknown
        }
}