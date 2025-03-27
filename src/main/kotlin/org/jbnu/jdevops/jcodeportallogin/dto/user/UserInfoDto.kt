package org.jbnu.jdevops.jcodeportallogin.dto.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.jbnu.jdevops.jcodeportallogin.entity.RoleType

data class UserInfoDto(
    val userId: Long? = null,
    val name: String?,
    @field:NotBlank(message = "{userInfoDto.email.required}")
    @field:Size(max = 100, message = "{userInfoDto.email.size}")
    val email: String,
    val role: RoleType,
    val courseRole: RoleType? = null,
    val studentNum: Int?
)
