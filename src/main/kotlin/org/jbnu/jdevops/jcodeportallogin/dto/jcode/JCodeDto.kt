package org.jbnu.jdevops.jcodeportallogin.dto.jcode

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class JCodeDto(
    val jcodeId: Long?,

    @field:NotBlank(message = "{course.name.required}")
    @field:Size(max = 100, message = "{course.name.size}")
    val courseName: String
)


data class JCodeMainRequestDto(
    @field:NotBlank(message = "{userDto.email.required}")
    @field:Size(max = 100, message = "{userDto.email.size}")
    val userEmail: String,

    val snapshot: Boolean = false
)