package org.jbnu.jdevops.jcodeportallogin.service

import jakarta.servlet.http.HttpServletRequest
import org.jbnu.jdevops.jcodeportallogin.dto.auth.LoginUserDto

import org.jbnu.jdevops.jcodeportallogin.entity.RoleType
import org.jbnu.jdevops.jcodeportallogin.repo.LoginRepository
import org.jbnu.jdevops.jcodeportallogin.repo.UserRepository
import org.jbnu.jdevops.jcodeportallogin.service.token.JwtAuthService
import org.jbnu.jdevops.jcodeportallogin.service.token.TokenType
import org.jbnu.jdevops.jcodeportallogin.util.JwtUtil
import org.jbnu.jdevops.jcodeportallogin.util.RefreshTokenUtil
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val loginRepository: LoginRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtAuthService: JwtAuthService,
    private val redisService: RedisService,
    private val jwtUtil: JwtUtil
) {

    fun basicLogin(loginUserDto: LoginUserDto): Map<String, String> {
        val user = userRepository.findByEmail(loginUserDto.email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        val login = loginRepository.findByUserId(user.id)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")

        if (!passwordEncoder.matches(loginUserDto.password, login.password)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password")
        }

        // JWT 토큰 생성 (이메일 + 학교 정보)
        val jwt = jwtAuthService.createToken(user.email, RoleType.STUDENT, TokenType.ACCESS)
        return mapOf("message" to "Login successful", "token" to jwt)
    }

    // 검증: refresh token, session 유효성 검증
    // 발급: 새로운 access token 생성 후 헤더에 반환 (RTR)
    fun getAccessToken(request: HttpServletRequest): Map<String, String> {
        // 1. 쿠키에서 refresh token 추출
        val refreshToken = jwtUtil.extractCookieToken(request, "jcodeRt")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "No refresh token provided")

        // 2. refresh token의 클레임에서 사용자 이메일 추출
        val email = jwtAuthService.extractEmail(refreshToken, TokenType.REFRESH)

        // 3. Refresh token 검증 (유효성, 블랙리스트, Redis 저장값 일치 여부)
        RefreshTokenUtil.validate(refreshToken, jwtAuthService, redisService, email)

        // 4. 세션 검증: HTTP 세션이 없으면 오류 처리
        val session = request.getSession(false)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "No active session")

        // 5. 사용자 조회 및 역할 추출
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")
        val role: RoleType = user.role

        // 6. 새 access token 및 refresh token 생성 (RTR 적용)
        val newAccessToken = jwtAuthService.createToken(email, role, TokenType.ACCESS)
        val newRefreshToken = jwtAuthService.createToken(email, role, TokenType.REFRESH)

        // 7. Redis에 새로운 refresh token 저장
        redisService.storeRefreshToken(email, newRefreshToken)

        // 두 토큰을 Map에 담아서 반환
        return mapOf(
            "accessToken" to newAccessToken,
            "refreshToken" to newRefreshToken
        )
    }

    // 검증: refresh token 유효성 검증
    // 재발급: 새로운 access token과 refresh token 생성 후 Redis 및 쿠키/헤더 갱신 (RTR)
    fun refreshTokens(request: HttpServletRequest): Map<String, String> {
        // 1. 쿠키에서 refresh token 추출
        val refreshToken = jwtUtil.extractCookieToken(request, "jcodeRt")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "No refresh token provided")

        // 2. refresh token의 클레임에서 사용자 정보 추출
        val email = jwtAuthService.extractEmail(refreshToken, TokenType.REFRESH)

        // 3. refresh token 검증 (유효성, 블랙리스트, Redis 저장값 일치 여부 확인)
        RefreshTokenUtil.validate(refreshToken, jwtAuthService, redisService, email)

        // 4. 사용자 조회
        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")
        val role = user.role

        // 5. 새 access token 및 refresh token 생성 (RTR 적용)
        val newAccessToken = jwtAuthService.createToken(email, role, TokenType.ACCESS)
        val newRefreshToken = jwtAuthService.createToken(email, role, TokenType.REFRESH)

        // 6. Redis에 새로운 refresh token 저장
        redisService.storeRefreshToken(email, newRefreshToken)

        // 두 토큰을 Map에 담아서 반환
        return mapOf(
            "accessToken" to newAccessToken,
            "refreshToken" to newRefreshToken
        )
    }
}
