package org.jbnu.jdevops.jcodeportallogin.dto.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginUserDto (
    @field:NotBlank(message = "{user.email.required}")
    @field:Size(max = 100, message = "{user.email.size}")
    val email: String,

    @field:NotBlank(message = "{login.password.required}")
    @field:Size(min = 8, max = 100, message = "{login.password.size}")
    val password: String
)
