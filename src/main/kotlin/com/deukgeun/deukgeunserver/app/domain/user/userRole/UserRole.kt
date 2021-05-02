package com.deukgeun.deukgeunserver.app.domain.user.userRole

import com.deukgeun.deukgeunserver.app.domain.BaseEntity
import com.deukgeun.deukgeunserver.app.domain.user.User
import org.springframework.security.core.GrantedAuthority
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

enum class RoleName {
    MEMBER, MASTER
}

@Entity
class UserRole(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_seq")
    var user: User,
    var roleName: RoleName
) : BaseEntity(), GrantedAuthority {

    override fun getAuthority(): String {
        return roleName.toString()
    }
}