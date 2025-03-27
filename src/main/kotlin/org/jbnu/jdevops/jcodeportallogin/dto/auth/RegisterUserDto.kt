package org.jbnu.jdevops.jcodeportallogin.dto.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.jbnu.jdevops.jcodeportallogin.entity.RoleType

data class RegisterUserDto(
    @field:NotBlank(message = "{user.email.required}")
    @field:Size(max = 100, message = "{user.email.size}")
    val email: String,

    val studentNum: Int,

    val role: RoleType = RoleType.STUDENT, // 기본적으로 학생 역할 부여

    @field:NotBlank(message = "{login.password.required}")
    @field:Size(min = 8, max = 100, message = "{login.password.size}")
    val password: String
)
