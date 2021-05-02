package com.deukgeun.deukgeunserver.app.domain.user

import com.deukgeun.deukgeunserver.app.domain.BaseEntity
import com.deukgeun.deukgeunserver.app.domain.user.userRole.UserRole
import com.deukgeun.deukgeunserver.app.web.dto.KakaoToken
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.OneToMany

@Entity
@JsonIdentityInfo(property = "objId", generator = ObjectIdGenerators.StringIdGenerator::class)
class User: BaseEntity {

    var userId: String

    @Enumerated(EnumType.STRING)
    var userType: UserType

    @OneToMany(mappedBy = "user")
    var userRoles: MutableSet<UserRole>

    var accessToken: String? = ""
    var refreshToken: String? = ""

    var userName: String? = ""

    var birthday: LocalDate? = null

    var profileImageLink: String? = ""

    var isRegistered: Boolean

    constructor(
        userId: String,
        userType: UserType,
        userRoles: MutableSet<UserRole>,
        userName: String,
        birthday: LocalDate
    ) {
        this.userId = userId
        this.userType = userType
        this.userRoles = userRoles
        this.userName = userName
        this.birthday = birthday
        this.isRegistered = false
    }

    constructor(userId: String) {
        this.userId = userId
        this.userType = UserType.KAKAO
        this.userRoles = mutableSetOf()
        this.isRegistered = false
    }

    constructor(userId: String, userName: String) {
        this.userId = userId
        this.userName = userName
        this.userType = UserType.KAKAO
        this.userRoles = mutableSetOf()
        this.isRegistered = false
    }

    constructor(userId: String, token: KakaoToken) {
        this.userId = userId
        this.userType = UserType.KAKAO
        this.userRoles = mutableSetOf()
        this.accessToken = token.access_token
        this.refreshToken = token.refresh_token
        this.isRegistered = false
    }
}