package org.jbnu.jdevops.jcodeportallogin.service

import org.jbnu.jdevops.jcodeportallogin.dto.jcode.*
import org.jbnu.jdevops.jcodeportallogin.entity.Jcode
import org.jbnu.jdevops.jcodeportallogin.entity.RoleType
import org.jbnu.jdevops.jcodeportallogin.repo.JCodeRepository
import org.jbnu.jdevops.jcodeportallogin.repo.CourseRepository
import org.jbnu.jdevops.jcodeportallogin.repo.UserCoursesRepository
import org.jbnu.jdevops.jcodeportallogin.repo.UserRepository
import org.jbnu.jdevops.jcodeportallogin.util.AuthorizationUtil
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

@Service
class JCodeService(
    @Qualifier("generatorWebClient")
    private val webClient: WebClient,
    private val jCodeRepository: JCodeRepository,
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val userCoursesRepository: UserCoursesRepository
) {
    // JCode 생성
    fun createJCode(courseId: Long, userEmail: String, email: String, token: String, snapshot: Boolean): JCodeDto {
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }

        val user = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        val userCourse = userCoursesRepository.findByUserIdAndCourseId(user.id, course.id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "UserCourse not found")

        // 이미 JCode가 존재하는지 확인
        val storedJcode = jCodeRepository.findByUserIdAndCourseIdAndSnapshot(user.id, course.id, snapshot)
        if (storedJcode != null) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Jcode already exists")
        }

        // 생성하려는 jcode의 주체가 현재 요청한 주체와 다를 경우 생성 불가 (ADMIN은 전부 가능)
        if (user.role != RoleType.ADMIN && user.email != userEmail) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "User role not allowed")
        }

        // snapshot 권한 확인 (targetUserId를 0으로 둬서 자기 자신의 스냅샷도 열람 못하게 설정)
        var deployment_name = "jcode-${course.code.lowercase()}-${course.clss}-${user.studentNum}"
        var file_path = "workspace/${course.code.lowercase()}-${course.clss}-${user.studentNum}"
        if (snapshot) {
            AuthorizationUtil.validateUserAuthority(user.role, user.id, 0, course.id, userCoursesRepository)
            deployment_name = "jcode-snapshot-${course.code.lowercase()}-${user.studentNum}"
            file_path = "${course.code.lowercase()}-${course.clss}"
        }
        val app_label = deployment_name

        val jcodeRequestBody = JCodeRequestDto (
            namespace = "jcode-${course.code.lowercase()}-${course.clss}",
            deployment_name = deployment_name,
            service_name = deployment_name + "-svc",
            app_label = app_label,
            file_path = file_path,
            student_num = user.studentNum.toString(),
            use_vnc = course.vnc,
            use_snapshot = snapshot
        )

        // 외부 API 호출: JCode가 없으므로 쿠버네티스에 실제 JCode 생성 요청
        val externalJcodeDto: JCodeResponseDto? = try {
            webClient.post()
                .uri("/api/jcode")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jcodeRequestBody)
                .retrieve()
                .bodyToMono(JCodeResponseDto::class.java)
                .block()
        } catch (ex: Exception) {
            println("Error calling external API: ${ex.message}")
            null
        }

        if (externalJcodeDto == null) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create Jcode externally")
        }

        val jCode = jCodeRepository.save(
            Jcode(
                jcodeUrl = externalJcodeDto.jcodeUrl,
                course = course,
                user = user,
                userCourse = userCourse,
                snapshot = snapshot,
            )
        )

        return JCodeDto(
            jcodeId = jCode.id,
            courseName = jCode.course.name
        )
    }

    // JCode 삭제 (관리자 전용)
    fun deleteJCode(userEmail: String, courseId: Long, token: String, snapshot: Boolean) {
        val user = userRepository.findByEmail(userEmail)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }

        val jCode = jCodeRepository.findByUserIdAndCourseIdAndSnapshot(user.id, course.id, snapshot)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "JCode not found for the specified user and course")

        val jcodeRequestBody = JCodeDeleteRequestDto (
            namespace = "jcode-${course.code.lowercase()}-${course.clss}",
            deployment_name = "jcode-${course.code.lowercase()}-${course.clss}-${user.studentNum}",
            service_name = "jcode-${course.code.lowercase()}-${course.clss}-${user.studentNum}-svc"
        )

        // 외부 API 호출: 쿠버네티스에 실제 JCode 삭제 요청
        val externalJcodeDto: JCodeDeleteResponseDto? = try {
            webClient.method(HttpMethod.DELETE)
                .uri("/api/jcode")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jcodeRequestBody)
                .retrieve()
                .bodyToMono(JCodeDeleteResponseDto::class.java)
                .block()
        } catch (ex: Exception) {
            println("Error calling external API: ${ex.message}")
            null
        }

        if (externalJcodeDto == null) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete Jcode externally")
        }

        jCodeRepository.delete(jCode)
    }
}
