package org.jbnu.jdevops.jcodeportallogin.config.xssConfig

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

class XssStringDeserializer : StdDeserializer<String>(String::class.java) {
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext?): String {
        val rawValue = parser.valueAsString
        // jsoup을 이용해 모든 HTML 태그 제거 (모든 태그를 허용하지 않음)
        return Jsoup.clean(rawValue, Safelist.none())
    }
}
