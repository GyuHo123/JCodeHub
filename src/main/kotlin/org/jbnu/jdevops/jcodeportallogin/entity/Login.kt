package org.jbnu.jdevops.jcodeportallogin.entity

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Entity
@Table(name = "login")
data class Login(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    @field:NotBlank(message = "{login.password.required}")
    @field:Size(min = 8, max = 100, message = "{login.password.size}")
    val password: String,

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
