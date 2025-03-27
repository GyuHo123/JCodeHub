package org.jbnu.jdevops.jcodeportallogin.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.jbnu.jdevops.jcodeportallogin.dto.assignment.AssignmentDto
import org.jbnu.jdevops.jcodeportallogin.service.AssignmentService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "Assignment API", description = "강의별 과제 관련 CRUD API (ADMIN, PROFESSOR 전용)")
@RestController
@RequestMapping("/api/courses/{courseId}/assignments")
@PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")  // ADMIN, PROFESSOR 권한이 없는 사용자는 모두 접근 불가
class AssignmentController(
    private val assignmentService: AssignmentService
) {
    // 과제 추가
    @Operation(summary = "과제 추가", description = "특정 강의에 새로운 과제를 추가합니다.")
    @PostMapping
    fun createAssignment(@PathVariable courseId: Long, @RequestBody assignmentDto: AssignmentDto): ResponseEntity<AssignmentDto> {
        return ResponseEntity.ok(assignmentService.createAssignment(courseId, assignmentDto))
    }

    // 과제 수정
    @Operation(summary = "과제 수정", description = "특정 강의의 특정 과제를 수정합니다.")
    @PutMapping("/{assignmentId}")
    fun updateAssignment(@PathVariable courseId: Long, @PathVariable assignmentId: Long, @RequestBody assignmentDto: AssignmentDto): ResponseEntity<AssignmentDto> {
        return ResponseEntity.ok(assignmentService.updateAssignment(courseId, assignmentId, assignmentDto))
    }

    // 과제 삭제
    @Operation(summary = "과제 삭제", description = "특정 강의의 특정 과제를 삭제합니다.")
    @DeleteMapping("/{assignmentId}")
    fun deleteAssignment(@PathVariable courseId: Long, @PathVariable assignmentId: Long): ResponseEntity<String> {
        assignmentService.deleteAssignment(courseId, assignmentId)
        return ResponseEntity.ok("Assignment deleted successfully")
    }
}
