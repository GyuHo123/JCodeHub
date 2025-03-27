package org.jbnu.jdevops.jcodeportallogin.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class JwtUtil (
    @Value("\${jwt.cookie.domain}")
    private val cookieDomain: String
) {
    fun createJwtCookie(name: String, jwt: String): Cookie {
        return Cookie(name, jwt).apply {
            isHttpOnly = true  // XSS 공격 방지
            secure = true      // HTTPS에서만 전송
            path = "/"         // 전체 도메인에서 사용 가능
            maxAge = 6 * 3600      // 6시간 동안 유효
            setAttribute("SameSite", "Strict")  // 리다이렉션 시 쿠키 유지
            domain = cookieDomain // 쿠키를 모든 하위 도메인에서 사용 가능하도록 설정
        }
    }

    fun createExpiredCookie(name: String): Cookie {
        return Cookie(name, null).apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = 0
            setAttribute("SameSite", "Strict")
            domain = cookieDomain
        }
    }

    // access token을 헤더에서 추출하는 함수
    fun extractBearerToken(request: HttpServletRequest): String? {
        val authHeader = request.getHeader("Authorization")
        return if (!authHeader.isNullOrEmpty() && authHeader.startsWith("Bearer ")) {
            authHeader.substringAfter("Bearer ").trim()
        } else null
    }

    // refresh token을 쿠키에서 추출하는 함수
    fun extractCookieToken(request: HttpServletRequest, cookieName: String): String? {
        return request.cookies?.firstOrNull { it.name == cookieName }?.value
    }
}