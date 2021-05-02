package com.deukgeun.deukgeunserver.app.domain.role

import com.deukgeun.deukgeunserver.app.domain.BaseEntity
import javax.persistence.Entity

@Entity
class RoleGroup(
    var name: String,
    var role_type: String
) : BaseEntity()