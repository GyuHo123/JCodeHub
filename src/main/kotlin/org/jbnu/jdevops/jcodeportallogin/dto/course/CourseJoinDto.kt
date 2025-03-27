package org.jbnu.jdevops.jcodeportallogin.dto.course

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class CourseJoinDto (
    @field:NotBlank(message = "{course.key.required}")
    @field:Size(max = 100, message = "{course.key.size}")
    val courseKey: String
)
