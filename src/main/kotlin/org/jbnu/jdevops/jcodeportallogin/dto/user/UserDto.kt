package org.jbnu.jdevops.jcodeportallogin.dto.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.jbnu.jdevops.jcodeportallogin.entity.RoleType

data class UserDto(
    @field:NotBlank(message = "{userDto.email.required}")
    @field:Size(max = 100, message = "{userDto.email.size}")
    val email: String,
    val role: RoleType = RoleType.STUDENT,
    val studentNum: Int?,
    val name: String?
)
