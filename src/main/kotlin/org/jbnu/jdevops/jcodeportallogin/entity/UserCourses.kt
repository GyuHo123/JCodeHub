package org.jbnu.jdevops.jcodeportallogin.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

@Entity
@Table(name = "user_courses")
data class UserCourses(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    @field:NotNull(message = "{userCourses.course.required}")
    var course: Course,

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @field:NotNull(message = "{userCourses.user.required}")
    var user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @field:NotNull(message = "{userCourses.role.required}")
    var role: RoleType,

    @OneToMany(mappedBy = "userCourse", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    val jcodes: List<Jcode> = mutableListOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
