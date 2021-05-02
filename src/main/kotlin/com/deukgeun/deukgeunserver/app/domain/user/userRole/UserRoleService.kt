package com.deukgeun.deukgeunserver.app.domain.user.userRole

import com.deukgeun.deukgeunserver.app.domain.user.User
import org.springframework.stereotype.Service

@Service
class UserRoleService(
    val userRoleRepository: UserRoleRepository
) {

    fun addRole(user: User, roleName: RoleName) {
        userRoleRepository.save(UserRole(user, roleName))
    }

    fun removeRoleIfExist(user: User, roleName: RoleName) {
        val userRole = userRoleRepository.findByUserSeqAndRoleName(user.seq!!, roleName)

        if(userRole != null) {
            userRoleRepository.delete(userRole)
        }
    }
}