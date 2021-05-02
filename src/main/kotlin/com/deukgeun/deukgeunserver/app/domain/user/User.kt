package com.deukgeun.deukgeunserver.app.domain.user

import com.deukgeun.deukgeunserver.app.domain.BaseEntity
import com.deukgeun.deukgeunserver.app.domain.user.userRole.UserRole
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.OneToMany

@Entity
@JsonIdentityInfo(property = "objId", generator = ObjectIdGenerators.StringIdGenerator::class)
data class User(
    var userId: String? = "",
    var password: String? = "",
    @Enumerated(EnumType.STRING)
    var userType: UserType? = UserType.COMMON,
    @OneToMany(mappedBy = "user")
    var userRoles: MutableSet<UserRole>? = mutableSetOf(),
    var accessToken: String? = "",
    var refreshToken: String? = "",
    var userName: String? = "",
    var registered: Boolean? = true
) : BaseEntity()