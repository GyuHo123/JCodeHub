package org.jbnu.jdevops.jcodeportallogin.service

import org.jbnu.jdevops.jcodeportallogin.dto.assignment.AssignmentDto
import org.jbnu.jdevops.jcodeportallogin.entity.Assignment
import org.jbnu.jdevops.jcodeportallogin.repo.AssignmentRepository
import org.jbnu.jdevops.jcodeportallogin.repo.CourseRepository
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional

@Service
class AssignmentService(
    private val assignmentRepository: AssignmentRepository,
    private val courseRepository: CourseRepository
) {

    // 과제 추가
    @Transactional
    fun createAssignment(courseId: Long, assignmentDto: AssignmentDto): AssignmentDto {
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }

        if(assignmentRepository.existsByCourseIdAndName(course.id, assignmentDto.assignmentName)){
            throw ResponseStatusException(HttpStatus.CONFLICT, "Assignment already exists")
        }

        val assignment = assignmentRepository.save(Assignment(
            name = assignmentDto.assignmentName,
            description = assignmentDto.assignmentDescription,
            kickoffDate = assignmentDto.kickoffDate,
            deadlineDate = assignmentDto.deadlineDate,
            course = course))
        return AssignmentDto(
            assignmentId = assignment.id,
            assignmentName = assignment.name,
            assignmentDescription = assignment.description,
            kickoffDate = assignment.kickoffDate,
            deadlineDate = assignment.deadlineDate,
            createdAt = assignment.createdAt.toString(),
            updatedAt = assignment.updatedAt.toString())
    }

    // 과제 수정 (업데이트)
    @Transactional
    fun updateAssignment(courseId: Long, assignmentId: Long, assignmentDto: AssignmentDto): AssignmentDto {
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }

        val assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found") }

        // 수정된 데이터 저장
        val updatedAssignment = assignment.copy(
            name = assignmentDto.assignmentName,
            description = assignmentDto.assignmentDescription,
            kickoffDate = assignmentDto.kickoffDate,
            deadlineDate = assignmentDto.deadlineDate
        )

        assignmentRepository.save(updatedAssignment)

        return AssignmentDto(
            assignmentId = assignment.id,
            assignmentName = updatedAssignment.name,
            assignmentDescription = updatedAssignment.description,
            kickoffDate = updatedAssignment.kickoffDate,
            deadlineDate = updatedAssignment.deadlineDate,
            createdAt = updatedAssignment.createdAt.toString(),
            updatedAt = updatedAssignment.updatedAt.toString()
        )
    }

    // 과제 삭제
    @Transactional
    fun deleteAssignment(courseId: Long, assignmentId: Long) {
        if (!assignmentRepository.existsById(assignmentId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found")
        }
        assignmentRepository.deleteById(assignmentId)
    }
}
