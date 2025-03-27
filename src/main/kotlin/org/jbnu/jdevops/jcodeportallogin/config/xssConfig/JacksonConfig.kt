package org.jbnu.jdevops.jcodeportallogin.config.xssConfig

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfig {
    @Bean
    // xss config의 jackson 커스텀 deserializer를 통해 역직렬화 과정에서 문자열 필드 정제 (json 형태에 적용)
    fun jacksonCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            // XssSanitizerModule를 등록하여 모든 String 필드에 정제 적용
            builder.modulesToInstall(XssSanitizerModule())
        }
    }
}
