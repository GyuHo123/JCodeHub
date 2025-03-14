package org.jbnu.jdevops.jcodeportallogin.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jbnu.jdevops.jcodeportallogin.dto.jcode.RedirectDto
import org.jbnu.jdevops.jcodeportallogin.repo.CourseRepository
import org.jbnu.jdevops.jcodeportallogin.repo.JCodeRepository
import org.jbnu.jdevops.jcodeportallogin.repo.UserRepository
import org.jbnu.jdevops.jcodeportallogin.service.RedisService
import org.jbnu.jdevops.jcodeportallogin.util.JwtUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Tag(name = "Redirect API", description = "JCode(Node.js 서버)로의 리다이렉션 관련 API")
@RestController
@RequestMapping("/api/redirect")
class RedirectController(
    private val jwtUtil: JwtUtil,
    private val redisService: RedisService,
    private val jCodeRepository: JCodeRepository,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository
) {

    @Value("\${router.url}")  // 환경 변수에서 Node.js URL 가져오기
    private lateinit var routerUrl: String

    // Node.js 서버로 리다이렉션 (JCode)
    @Operation(
        summary = "JCode(Node.js 서버) 리다이렉션",
        description = "사용자 정보를 Redis에 저장 후 UUID를 파라미터로 전달하여 Node.js 서버에 리다이렉트 합니다."
    )
    @PostMapping
    fun redirectToNode(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @RequestBody redirectRequest: RedirectDto
    ): ResponseEntity<Void> {

        val token = request.getHeader("Authorization")?.removePrefix("Bearer ")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization Token")

        val user = userRepository.findByEmail(redirectRequest.userEmail)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        val course = courseRepository.findById(redirectRequest.courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }

        // 사용자 프로필 정보를 Redis에 저장하고 UUID를 획득 & UUID를 UTF-8로 URL 인코딩
        val uuid = redisService.storeUserProfile(user.email, user.studentNum.toString(), course.code, course.clss.toString())
        val encodedUUID = URLEncoder.encode(uuid, StandardCharsets.UTF_8.toString()).replace("+", "%2B")

        // 학생 Jcode 정보 Redis 동기화
        val storedJcode = jCodeRepository.findByUserIdAndCourseIdAndSnapshot(user.id, course.id, redirectRequest.snapshot)
        if (storedJcode != null) redisService.storeUserCourse(user.email, course.code, course.clss, storedJcode.jcodeUrl)

        // Node.js 서버 URL에 인코딩된 UUID 파라미터만 포함하여 구성
        val finalNodeJsUrl = "$routerUrl?id=$encodedUUID&folder=/home/coder/project"
        println("Redirect URL: $finalNodeJsUrl")

        // Keycloak Access Token을 HTTP-Only Secure 쿠키로 설정
        response.addCookie(jwtUtil.createJwtCookie("jcodeAt", token))

        // 클라이언트를 Node.js 서버로 리다이렉트
        response.sendRedirect(finalNodeJsUrl)
        return ResponseEntity.status(HttpStatus.FOUND).build()
    }
}