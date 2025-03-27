package org.jbnu.jdevops.jcodeportallogin.config.xssConfig

import org.jbnu.jdevops.jcodeportallogin.security.XssProtectionFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FilterConfig {
    @Bean
    // xss security filter를 적용하여 파라미터 및 폼 입력값 검증  (xss protection filter)
    fun xssProtectionFilterRegistration(): FilterRegistrationBean<XssProtectionFilter> {
        val registrationBean = FilterRegistrationBean<XssProtectionFilter>()
        registrationBean.filter = XssProtectionFilter()
        registrationBean.addUrlPatterns("/*") // 모든 URL에 적용
        registrationBean.order = 1  // 필터 순서 설정 (필요에 따라 조정)
        return registrationBean
    }
}
