package org.jbnu.jdevops.jcodeportallogin.service

import org.jbnu.jdevops.jcodeportallogin.dto.assignment.AssignmentDto
import org.jbnu.jdevops.jcodeportallogin.dto.course.CourseDto
import org.jbnu.jdevops.jcodeportallogin.dto.usercourse.UserCourseDetailsDto
import org.jbnu.jdevops.jcodeportallogin.dto.user.UserInfoDto
import org.jbnu.jdevops.jcodeportallogin.entity.Course
import org.jbnu.jdevops.jcodeportallogin.entity.RoleType
import org.jbnu.jdevops.jcodeportallogin.repo.AssignmentRepository
import org.jbnu.jdevops.jcodeportallogin.repo.CourseRepository
import org.jbnu.jdevops.jcodeportallogin.repo.UserCoursesRepository
import org.jbnu.jdevops.jcodeportallogin.repo.UserRepository
import org.jbnu.jdevops.jcodeportallogin.util.CourseKeyUtil
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional

@Service
class CourseService(
    private val userCoursesRepository: UserCoursesRepository,
    private val assignmentRepository: AssignmentRepository,
    private val courseRepository: CourseRepository,
    private val courseKeyUtil: CourseKeyUtil,
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository
) {
    // 강의별 유저 조회
    @Transactional(readOnly = true)
    fun getUsersByCourse(email: String, courseId: Long): List<UserInfoDto> {
        // 강의가 존재하는지 먼저 확인
        val userCourses = userCoursesRepository.findByCourseId(courseId)

        if (userCourses.isEmpty()) {
            return emptyList()
        }

        val currentUser = userRepository.findByEmail(email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Current user not found")

        // 만약 현재 사용자가 ASSISTANT라면 ASSISTANT 역할인 강의만 필터링
        if (currentUser.role == RoleType.ASSISTANT) {
            val isAssistantInCourse = userCoursesRepository.existsByCourseIdAndUserIdAndRole(courseId, currentUser.id, RoleType.ASSISTANT)
            if (!isAssistantInCourse) {
                return emptyList()
            }
        }

       return userCourses.map {
           val user = it.user
           val role = it.role
           UserInfoDto(
               userId = user.id,
               name = user.name,
               email = user.email,
               role = user.role,
               courseRole = role,
               studentNum = user.studentNum
           )
       }
    }

    // 강의별 과제 조회
    @Transactional(readOnly = true)
    fun getAssignmentsByCourse(courseId: Long): List<AssignmentDto> {
        val assignments = assignmentRepository.findByCourseId(courseId)

        if (assignments.isEmpty()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No assignments found for this course")
        }

        return assignments.map {
            AssignmentDto(
                assignmentId = it.id,
                assignmentName = it.name,
                assignmentDescription = it.description,
                kickoffDate = it.kickoffDate,
                deadlineDate = it.deadlineDate,
                createdAt = it.createdAt.toString(),
                updatedAt = it.updatedAt.toString()
            )
        }
    }

    // 강의 key 재발급
    @Transactional
    fun reissueCourseKey(courseId: Long): String {
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }

        // 새 원본 key 생성
        val newRawKey = courseKeyUtil.generateCourseEnrollmentCode(course.code, course.clss)

        // 암호화하여 업데이트
        val newEncryptedKey = passwordEncoder.encode(newRawKey)
        course.courseKey = newEncryptedKey
        courseRepository.save(course)

        return newRawKey // 새 원본 key(평문)를 반환 (관리자에게 한 번만 노출)
    }

    // 강의 추가
    @Transactional
    fun createCourse(courseDto: CourseDto): CourseDto {
        // 랜덤 key를 생성하여 할당
        val rawKey = courseKeyUtil.generateCourseEnrollmentCode(courseDto.code, courseDto.clss)
        // PasswordEncoder를 사용해 암호화 (해싱) 처리
        val encryptedKey = passwordEncoder.encode(rawKey)

        val course = courseRepository.save(Course(
            name = courseDto.name,
            code = courseDto.code,
            professor = courseDto.professor,
            clss = courseDto.clss,
            year = courseDto.year,
            term = courseDto.term,
            vnc = courseDto.vnc,
            courseKey = encryptedKey
        ))
        return CourseDto(
            courseId = course.id,
            name = course.name,
            code = course.code,
            professor = course.professor,
            clss = course.clss,
            year = course.year,
            term = course.term,
            vnc = course.vnc,
            courseKey = rawKey
        )
    }

    // 강의 수정
    @Transactional
    fun updateCourse(courseId: Long, courseDto: CourseDto): CourseDto {
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }
        val updatedCourse = course.copy(name = courseDto.name, code = courseDto.code, clss = courseDto.clss, year = courseDto.year, term = courseDto.term, professor = courseDto.professor)
        courseRepository.save(updatedCourse)
        return CourseDto(
            courseId = updatedCourse.id,
            name = updatedCourse.name,
            code = updatedCourse.code,
            professor = updatedCourse.professor,
            clss = updatedCourse.clss,
            year = updatedCourse.year,
            term = updatedCourse.term,
            vnc = updatedCourse.vnc
        )
    }

    @Transactional
    fun deleteCourse(courseId: Long) {
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }

        // 강의 삭제
        courseRepository.delete(course)
    }

    // 전체 강의 조회
    @Transactional(readOnly = true)
    fun getAllCourses(): List<CourseDto> {
        return courseRepository.findAll()
            .map { course ->
                CourseDto(
                    courseId = course.id,
                    name = course.name,
                    code = course.code,
                    professor = course.professor,
                    term = course.term,
                    year = course.year,
                    clss = course.clss,
                    vnc = course.vnc
                )
            }
    }

    // 관리자용 강의 상세 정보 조회
    @Transactional(readOnly = true)
    fun getCourseDetails(courseId: Long): UserCourseDetailsDto {
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }

        val assignments = assignmentRepository.findByCourseId(courseId)
            .map { assignment ->
                AssignmentDto(
                    assignmentId = assignment.id,
                    assignmentName = assignment.name,
                    assignmentDescription = assignment.description,
                    kickoffDate = assignment.kickoffDate,
                    deadlineDate = assignment.deadlineDate,
                    createdAt = assignment.createdAt.toString(),
                    updatedAt = assignment.updatedAt.toString()
                )
            }

        return UserCourseDetailsDto(
            courseId = course.id,
            courseName = course.name,
            courseCode = course.code,
            courseProfessor = course.professor,
            courseYear = course.year,
            courseTerm = course.term,
            courseClss = course.clss,
            assignments = assignments,
            jcodeUrl = null // 관리자는 JCode URL이 필요 없음
        )
    }
}
