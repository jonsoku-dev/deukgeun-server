package com.deukgeun.deukgeunserver.app.domain.user

import com.deukgeun.deukgeunserver.app.domain.user.userRole.RoleName
import com.deukgeun.deukgeunserver.app.domain.user.userRole.UserRoleService
import com.deukgeun.deukgeunserver.app.mapper.UserMapper
import com.deukgeun.deukgeunserver.app.web.dto.KakaoToken
import com.deukgeun.deukgeunserver.app.web.dto.LoginRequestDto
import com.deukgeun.deukgeunserver.app.web.dto.RegisterRequestDto
import com.deukgeun.deukgeunserver.common.config.security.AppToken
import com.deukgeun.deukgeunserver.common.config.security.JwtTokenProvider
import com.deukgeun.deukgeunserver.common.exception.BizException
import com.deukgeun.deukgeunserver.common.util.kakao.KakaoOAuth
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.logging.Logger
import javax.transaction.Transactional

@Service
class UserService(
    private val userMapper: UserMapper,
    private val userRepository: UserRepository,
    private val userRoleService: UserRoleService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder,
    private var kakaoOAuth: KakaoOAuth,
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

    @Transactional(rollbackOn = [Exception::class])
    fun updateUserToken(user: User, token: KakaoToken) {
        val targetUser = userRepository.findByUserId(user.userId)!!
        targetUser.accessToken = token.access_token

        if(token.refresh_token?.isNotBlank()!!) {
            targetUser.refreshToken = token.refresh_token
        }
        userRepository.save(targetUser)
    }

    @Transactional
    fun saveKakaoToken(kakaoToken: KakaoToken): AppToken {
        println("saveKakaoToken.kakaoToken: $kakaoToken")
        val token = kakaoOAuth.refreshIfTokenExpired(kakaoToken)
        println("token: $token")
        val kakaoId = kakaoOAuth.getKakaoUserProfile(token).id
        println("kakaoId: $kakaoId")

        // [1] kakao 유저 존재 x
        if (kakaoId.isBlank()) {
            LOG.info("unknown kakao token received")
            throw BizException("존재하지 않는 kakao userid입니다", HttpStatus.NOT_FOUND)
        }

        var user: User? = userRepository.findByUserId(kakaoId)

        // [2] 유저 최초 가입시
        if (user == null) {
            user = User().apply {
                this.userId = kakaoId
                this.accessToken = token.access_token
                this.refreshToken = token.refresh_token
            }
            userRepository.save(user)
            userRoleService.addRole(user, RoleName.MEMBER)
        }

        // 토큰이 만료되었을 때
        if (token.access_token != kakaoToken.access_token) {
            LOG.info("[TOKEN EXPIRE] - ${user.userId}의 카카오 토큰이 만료되어 새로 갱신합니다.")
            updateUserToken(user, token)
        }

        user.accessToken = token.access_token
        user.refreshToken = token.refresh_token

        return AppToken(
            user.registered,
            jwtTokenProvider.createAppToken(user.userId)
        )
    }
}