package com.deukgeun.deukgeunserver.app.domain.role

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleGroupRepository : JpaRepository<RoleGroup, Long> {
}