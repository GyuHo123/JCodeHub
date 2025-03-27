package org.jbnu.jdevops.jcodeportallogin.config.xssConfig

import com.fasterxml.jackson.databind.module.SimpleModule

class XssSanitizerModule : SimpleModule() {
    init {
        // 모든 String 타입에 대해 XssStringDeserializer 적용
        addDeserializer(String::class.java, XssStringDeserializer())
    }
}
