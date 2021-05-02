package com.deukgeun.deukgeunserver.app.web.dto

import com.deukgeun.deukgeunserver.app.domain.user.UserType
import com.deukgeun.deukgeunserver.app.domain.user.userRole.UserRole
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class UserDto(
    var userId: String,
    var password: String,
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

data class RegisterRequestDto(
    var userId: String? = "",
    var userName: String? = "",
    var password: String? = ""
)

data class RegisterResponseDto(
    val userId: String? = ""
)

data class LoginRequestDto(
    val userId: String,
    val password: String
)

data class LoginResponseDto(
    val token: String
)