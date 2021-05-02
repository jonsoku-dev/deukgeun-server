package com.deukgeun.deukgeunserver.app.mapper

import com.deukgeun.deukgeunserver.app.domain.user.User
import com.deukgeun.deukgeunserver.app.web.dto.RegisterRequestDto
import com.deukgeun.deukgeunserver.app.web.dto.UserDto
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [])
interface UserMapper : BaseMapper<UserDto, User> {
    fun registerRequestDtoToEntity(registerRequestDto: RegisterRequestDto): User
}