package org.jbnu.jdevops.jcodeportallogin.dto.user

import org.jbnu.jdevops.jcodeportallogin.entity.RoleType

data class UserRoleChangeDto (
    val newRole: RoleType,
    val courseId: Long? = null
)
