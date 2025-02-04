package org.jbnu.jdevops.jcodeportallogin.service

import org.jbnu.jdevops.jcodeportallogin.dto.LoginUserDto

import org.jbnu.jdevops.jcodeportallogin.entity.RoleType
import org.jbnu.jdevops.jcodeportallogin.entity.User
import org.jbnu.jdevops.jcodeportallogin.repo.LoginRepository
import org.jbnu.jdevops.jcodeportallogin.repo.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val loginRepository: LoginRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtAuthService: JwtAuthService
) {

    fun basicLogin(loginUserDto: LoginUserDto): Map<String, String> {
        val user = userRepository.findByEmail(loginUserDto.email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        val login = loginRepository.findByUser_UserId(user.userId)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")

        if (!passwordEncoder.matches(loginUserDto.password, login.password)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password")
        }

        // JWT 토큰 생성 (이메일 + 학교 정보)
        val jwt = jwtAuthService.createToken(user.email, RoleType.ADMIN)
        return mapOf("message" to "Login successful", "token" to jwt)
    }

    fun oidcLogin(email: String): Map<String, String> {
        try {
            // 데이터베이스에서 사용자 확인 후 저장
            val user = userRepository.findByEmail(email) ?: userRepository.save(
                User(
                    email = email,
                    role = RoleType.STUDENT
                )
            )

            // JWT 토큰 생성 (이메일 + 학교 정보)
            val jwt = jwtAuthService.createToken(email, RoleType.STUDENT)
            return mapOf("message" to "Login successful", "token" to jwt)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "OIDC Login failed")
        }
    }
}
