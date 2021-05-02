package com.deukgeun.deukgeunserver.app.domain.user

import com.deukgeun.deukgeunserver.app.domain.user.userRole.RoleName
import com.deukgeun.deukgeunserver.app.domain.user.userRole.UserRoleService
import com.deukgeun.deukgeunserver.app.mapper.UserMapper
import com.deukgeun.deukgeunserver.app.web.dto.LoginRequestDto
import com.deukgeun.deukgeunserver.app.web.dto.RegisterRequestDto
import com.deukgeun.deukgeunserver.common.config.security.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class UserService(
    private val userMapper: UserMapper,
    private val userRepository: UserRepository,
    private val userRoleService: UserRoleService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {
    companion object {
        val LOG: Logger = Logger.getLogger(UserService::class.java.name)
    }

    fun register(registerRequestDto: RegisterRequestDto) {
        // [1] 회원가입되어있는지 확인
        val foundUser = userRepository.findByUserId(registerRequestDto.userId)

        // [2] 회원가입 되어있다면 에러
        if (foundUser != null) {
            LOG.info("중복된 회원입니다. userId: ${registerRequestDto.userId}")
            throw RuntimeException("중복된 회원입니다")
        }

        // [3] DTO 로 새로운 User Entity 생성
        val user = userMapper.registerRequestDtoToEntity(registerRequestDto.apply {
            this.password = passwordEncoder.encode(registerRequestDto.password)
        })

        // [4] DB 에 저장
        val savedUser = userRepository.save(user)

        // [5] 저장된 유저와 기본 ROLE 을 함께 ROLE Table 에 저장
        userRoleService.addRole(savedUser, RoleName.MEMBER)
    }

    fun login(loginRequestDto: LoginRequestDto): String {
        // [1] 회원가입되어있는지 확인
        val foundUser = userRepository.findByUserId(loginRequestDto.userId)

        // [2] 회원가입 되어있지 않다면 에러
        if (foundUser == null) {
            LOG.info("가입되지않은 회원입니다. userId: ${loginRequestDto.userId}")
            throw RuntimeException("가입되지않은 회원입니다")
        }

        // [3] 비밀번호를 확인
        val isMatched = passwordEncoder.matches(loginRequestDto.password, foundUser.password)

        if (!isMatched) {
            throw RuntimeException("비밀번호를 확인해주세요.")
        }

        // [4] token 을 생성
        val token = jwtTokenProvider.createAppToken(loginRequestDto.userId)

        return token
    }
}