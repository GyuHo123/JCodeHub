package org.jbnu.jdevops.jcodeportallogin.security

import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class XssProtectionFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 요청을 감싸서 파라미터가 읽힐 때마다 정제되도록 함
        val sanitizedRequest = XssHttpServletRequestWrapper(request)
        filterChain.doFilter(sanitizedRequest, response)
    }
}
