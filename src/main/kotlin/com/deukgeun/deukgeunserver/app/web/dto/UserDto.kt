package com.deukgeun.deukgeunserver.app.web.dto

import com.deukgeun.deukgeunserver.app.domain.user.UserType
import com.deukgeun.deukgeunserver.app.domain.user.userRole.UserRole
import java.time.LocalDateTime

data class UserDto(
    var userId: String,
    var userType: UserType,
    var userRoles: MutableSet<UserRole>,
    var accessToken: String,
    var refreshToken: String,
    var userName: String,
    var isRegistered: Boolean,
    var seq: Long,
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
)