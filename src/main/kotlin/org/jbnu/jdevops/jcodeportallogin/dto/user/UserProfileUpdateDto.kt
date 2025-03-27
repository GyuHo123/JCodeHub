package org.jbnu.jdevops.jcodeportallogin.dto.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserProfileUpdateDto (
    @field:NotBlank(message = "{userProfileUpdate.name.required}")
    @field:Size(max = 50, message = "{userProfileUpdate.name.size}")
    val name: String,
    val studentNum: Int
)
