package com.deukgeun.deukgeunserver.app.domain.user

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    @EntityGraph(attributePaths = ["userRoles"])
    fun findByUserId(userId: String?): User?
    fun findByUserName(username: String): User?
}