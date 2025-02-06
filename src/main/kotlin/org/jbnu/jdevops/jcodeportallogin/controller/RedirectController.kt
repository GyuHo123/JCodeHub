package org.jbnu.jdevops.jcodeportallogin.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jbnu.jdevops.jcodeportallogin.util.JwtUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@Tag(name = "Redirect API", description = "JCode(Node.js 서버)로의 리다이렉션 관련 API")
@RestController
@RequestMapping("/api/redirect")
class RedirectController {

    @Value("\${nodejs.url}")  // 환경 변수에서 Node.js URL 가져오기
    private lateinit var nodeJsUrl: String

    // Node.js 서버로 리다이렉션 (JCode)
    @Operation(
        summary = "JCode(Node.js 서버) 리다이렉션",
        description = "url 파라미터로 courseCode 및 st(email)을 Node.js 서버에 보내어 검증 및 jcode로 리다이렉트 합니다."
    )
    @GetMapping("/redirect")
    fun redirectToNode(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @RequestParam courseCode: String,
        @RequestParam st: String
    ): ResponseEntity<Void> {

        val token = request.getHeader("Authorization")?.removePrefix("Bearer ")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization Token")

        // Node.js 서버 URL 설정 (st 파라미터 추가)
        val nodeJsUrl = "$nodeJsUrl?courseCode=$courseCode&st=$st"

        // Keycloak Access Token을 HTTP-Only Secure 쿠키로 설정
        response.addCookie(JwtUtil.createJwtCookie("jwt", token))

        // 클라이언트를 Node.js 서버로 리다이렉트
        response.sendRedirect(nodeJsUrl)
        return ResponseEntity.status(HttpStatus.FOUND).build()
    }
}