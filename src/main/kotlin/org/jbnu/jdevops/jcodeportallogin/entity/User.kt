package org.jbnu.jdevops.jcodeportallogin.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.jbnu.jdevops.jcodeportallogin.dto.user.UserDto
import java.time.LocalDateTime

@Entity
@Table(name = "user")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,  // JPA가 자동으로 설정

    @Column(nullable = false, unique = true)
    @field:NotBlank(message = "{user.email.required}")
    @field:Size(max = 100, message = "{user.email.size}")
    val email: String,

    @Column(nullable = true)
    var name: String? = null, // 이름 필드 (optional)

    @Column(nullable = true, unique = true)
    var studentNum: Int? = null,  // 처음엔 null 허용, 업데이트는 메서드로 제어

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: RoleType,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    // UserCourses와의 일대다 관계 설정
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val courses: List<UserCourses> = mutableListOf()
) {
    // studentNum 업데이트 메서드 (한번 설정되면 수정 불가)
    fun updateStudentNum(newStudentNum: Int) {
        if (this.studentNum != null) {
            throw IllegalStateException("Student number is already set and cannot be changed")
        }
        this.studentNum = newStudentNum
    }
}

fun User.toDto(): UserDto {
    return UserDto(
        email = this.email,
        role = this.role,
        studentNum = this.studentNum,
        name = this.name
    )
}
